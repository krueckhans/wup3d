package de.wuppertal.tools.gmlGeometryParser;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Vector;

import org.jdom2.Element;

import com.jme3.asset.AssetManager;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

import de.wuppertal.Coord;
import de.wuppertal.CoordVector;
import de.wuppertal.tools.triangulation.J3DTriangulator;

public class CityGML2J3O extends AbstractGmlParser{
	private double offsetx;
	private double offsety;
	private boolean createpseudoNormals;
	private boolean lightModel;
	private File fileRootDst ;
	private AssetManager assetManager;
	private HashMap<File, HashMap<GMLTYPE,Vector<CoordVector>>> mapGml;
	private Vector<Geometry> geoms;



	public static void main(String[] args) {
		//		CityGML2J3O app = new CityGML2J3O(new File("C:\\Temp\\ddddaten\\rohdaten\\citygml"), new File("C:\\Temp\\ddddaten\\buildings"));
		//		//		   app.setOffsetx(0.03613953E7);
		//		//		   app.setOffsety(5669917.94);
		////		app.setOffsetx(0.03613953E7);
		////		app.setOffsety(5686617.94);
		//		app.startParsingMultiThreaded(1);
	}


	public CityGML2J3O(File fileRootSrc, File fileRootDst,boolean createpseudoNormals, boolean lightModel) {
		super(fileRootSrc);
		this.fileRootDst = fileRootDst;
		this.createpseudoNormals = createpseudoNormals;
		this.lightModel = lightModel;
		assetManager = new DesktopAssetManager(true);
		offsetx = 0;
		offsety = 0;
		mapGml = new HashMap<File, HashMap<GMLTYPE,Vector<CoordVector>>>();
		geoms = new Vector<Geometry>();
	}

	@Override
	protected void handleCoordVector(CoordVector vec, File f, Element e,GMLTYPE gmlType, Iterator<Element> processDescendants) {
		HashMap<GMLTYPE,Vector<CoordVector>> typevecvec = mapGml.get(f);
		if(typevecvec==null){
			typevecvec = new HashMap<GMLTYPE,Vector<CoordVector>>();
			mapGml.put(f, typevecvec);
		}
		Vector<CoordVector> vecvec = 	typevecvec.get(gmlType);
		if(vecvec==null){
			vecvec = new Vector<CoordVector>();
			typevecvec.put(gmlType, vecvec);
		}
		vecvec.add(vec);
	}

	public void setOffsetx(double offsetx){
		this.offsetx = offsetx;
	}

	public void setOffsety(double offsety){
		this.offsety = offsety;
	}



	@Override
	protected void finishedParsing(File f) {
		Node n = new Node(f.getName());
		HashMap<GMLTYPE,Vector<CoordVector>> map = mapGml.remove(f);
		for(Entry<GMLTYPE, Vector<CoordVector>> entry:map.entrySet()){
			GMLTYPE gmltype = entry.getKey();
			Vector<CoordVector> vecvecTriangles = polygons2GLTriangles(entry.getValue());
			Vector3f [] vertices = new Vector3f[vecvecTriangles.size()*3];
			Vector3f [] normals = new Vector3f[vertices.length];
			int [] indexes = new  int [vertices.length];//{ 2,0,1, 1,3,2 };
			int iVertices=0;
			for(CoordVector vec:vecvecTriangles){
				for(Coord c: vec.getCoords()){
					vertices[iVertices] = new Vector3f((float)c.x(), ((float)c.z()),  (float)c.y());
					indexes[iVertices] = iVertices;
					iVertices++;
				}
				Vector3f normal = null;

				if(createpseudoNormals){
					normal = getNormal(vertices[iVertices-1] , vertices[iVertices-2], vertices[iVertices-3]);
					normal.multLocal(-1, 1, -1);
					normal.setY(1);
					normal.normalizeLocal();
				}

				normals[iVertices-1] = normal;
				normals[iVertices-2] = normal;
				normals[iVertices-3] = normal;
			}
			iVertices=0;
			Mesh mesh = new Mesh();
			Material mat =null;
			if(lightModel){
				mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
				mesh.setBuffer(Type.Index,    3, BufferUtils.createIntBuffer(indexes));
				mesh.setBuffer(Type.Normal,    3, BufferUtils.createFloatBuffer(normals));
				mesh.updateBound();
				mat = new Material(assetManager,  "Common/MatDefs/Light/Lighting.j3md");
				mat.setBoolean("UseMaterialColors",true);  
				if(gmltype.equals(GMLTYPE.WALLSURFACE)){
					mat.setColor("Diffuse",  ColorRGBA.Gray);
					mat.setColor("Specular", ColorRGBA.Gray);
					mat.setFloat("Shininess", 5f);				
				}
				else if(gmltype.equals(GMLTYPE.ROOFSURFACE)){
					mat.setColor("Diffuse",  ColorRGBA.Red.mult(0.8f));
					mat.setColor("Specular", ColorRGBA.Red.mult(0.8f));
					mat.setFloat("Shininess", 3f);
				}
				mat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Back);
			}
			else{
				mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
				mesh.setBuffer(Type.Index,    3, BufferUtils.createIntBuffer(indexes));
//				mesh.setBuffer(Type.Normal,    3, BufferUtils.createFloatBuffer(normals));
				mesh.updateBound();
				mat = new Material(assetManager,  "Common/MatDefs/Misc/Unshaded.j3md");
				if(gmltype.equals(GMLTYPE.WALLSURFACE)){
					mat.setColor("Color",  ColorRGBA.Gray.mult(0.7f));
				}
				else if(gmltype.equals(GMLTYPE.ROOFSURFACE)){
					mat.setColor("Color",  ColorRGBA.Red.mult(0.4f));
				}
				mat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Back);
			}
			Geometry geo = new Geometry("mymesh", mesh); // using our custom mesh object
		
			
			geo.setMaterial(mat);
			geo.updateModelBound();
			n.attachChild(geo);
			System.out.println(n.getWorldBound().toString());

		}
		//Expoprt to j3o
		BinaryExporter exporter = new BinaryExporter();
		try {
			String name = f.getName();
			String x = name.substring(2, 5);
			String y = name.substring(7, 10);
			while(x.length()<3){
				x = "0"+x;
			}
			while(y.length()<3){
				y = "0"+y;
			}

			exporter.save(n, new File(fileRootDst,x+y+".j3o"));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private Vector<CoordVector> polygons2GLTriangles(Vector<CoordVector> vecvec){
		Vector<CoordVector> vecvecret = new Vector<CoordVector>();
		for(CoordVector vec : vecvec){
			CoordVector vecTemp = new CoordVector();
			for(Coord c:vec.getCoords()){
				Coord cret = new Coord(c.x()-offsetx, c.y()-offsety, c.z()); 
				vecTemp.add(cret);
			}
			vecTemp.remove(vecTemp.size()-1);
			for(CoordVector vecret2: J3DTriangulator.triangulate(vecTemp)){
				//				if(getNormal(vecret2).z<0){
				//					vecvecret.add(vecret2.getReversed());
				//				}
				//				else{
				vecvecret.add(vecret2);
				//				}
			}

			//			CoordVector vecret = new CoordVector();
			//			int i=0;
			//			int size = vec.size();
			//			System.out.println(size);
			//			switch (size){
			//			//triangle
			//			case 4:{
			//				for(Coord c:vec.getCoords()){
			//					Coord cret = new Coord(c.x()-offsetx, c.y()-offsety, c.z()); 
			//					vecret.add(cret);
			//					if(i==2){
			//						break;
			//					}
			//					i++;
			//				}
			//				vecvecret.add(vecret);
			//				break;
			//			}
			//			//Quad
			//			case 5:{
			//				for(Coord c:vec.getCoords()){
			//					Coord cret = new Coord(c.x()-offsetx, c.y()-offsety, c.z()); 
			//					vecret.add(cret);
			//					i++;
			//				}
			//				CoordVector vecret2 = new CoordVector();
			//				vecret2.add(vecret.getCoord(0));
			//				vecret2.add(vecret.getCoord(1));
			//				vecret2.add(vecret.getCoord(2));
			//				vecvecret.add(vecret2);
			//				CoordVector vecret3 = new CoordVector();
			//				vecret3.add(vecret.getCoord(0));
			//				vecret3.add(vecret.getCoord(2));
			//				vecret3.add(vecret.getCoord(3));
			//				vecvecret.add(vecret3);
			//				break;
			//			}
			//			//Quick And Dirty triangulation => NO ISLANDS! FOR CONVEX POLYGONS ONLY!!!
			//			default:{
			//				for(Coord c:vec.getCoords()){
			//					Coord cret = new Coord(c.x()-offsetx, c.y()-offsety, c.z()); 
			//					vecret.add(cret);
			//					i++;
			//				}
			//				for(int j=0;j<size-3;j++){
			//					CoordVector vecret2 = new CoordVector();
			//					vecret2.add(vecret.getCoord(0));
			//					vecret2.add(vecret.getCoord(j+1));
			//					vecret2.add(vecret.getCoord(j+2));
			//					vecvecret.add(vecret2);
			//				}
			//				break;
			//			}
			//			}


		}
		return vecvecret;
	}

	public void setAssetManager(AssetManager am){
		this.assetManager = am;
	}

	public Vector<Geometry> getGeoms(){
		return geoms;
	}


	public Vector3f getNormal(Vector3f p00, Vector3f p10, Vector3f p01){
		Vector3f v0 = p10.subtract(p00);
		Vector3f v1 = p01.subtract(p00);
		return v0.cross(v1).normalizeLocal().multLocal(-1);
	}


	@Override
	protected void parseGMLClass(String currentName, File f, Element e,Iterator<Element> processDescendants) {
		GMLTYPE type =null;
		switch (currentName){
		case "WallSurface":{
			type= GMLTYPE.WALLSURFACE;
			break;
		}
		case "RoofSurface":{
			type= GMLTYPE.ROOFSURFACE;
			break;
		}
		case "lod4Geometry":{
			type= GMLTYPE.LOD4GEOMETRY;
			break;
		}  
		default:{
			return;
		}
		}

		while(!(e = processDescendants.next()).getName().equals(GMLTYPE.POSLIST.getName())) {
			//skip Elements
		}
		parsePosList(f, e, type, processDescendants);

	}

}
