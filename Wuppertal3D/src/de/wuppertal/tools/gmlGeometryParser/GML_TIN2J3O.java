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
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

import de.wuppertal.BBox;
import de.wuppertal.Coord;
import de.wuppertal.CoordVector;
import de.wuppertal.tools.triangulation.J3DTriangulator;

public class GML_TIN2J3O extends AbstractGmlParser{
	private double offsetx;
	private double offsety;
	private File destFile;
	private AssetManager assetManager;
	private HashMap<File, HashMap<GMLTYPE,Vector<CoordVector>>> mapGml;
	private Vector<Geometry> geoms;
	private HashMap<String, BBox> mapBBoxRealByID;
	private volatile int iTrianglesAtAll;
	public static float PATCHSIZE = 200; 

	public void allFilesFinished(){
		System.out.println("TrianglesAtAll = "+iTrianglesAtAll);

	}
	public synchronized HashMap<String, BBox> getAllBBoxesReal(Coord globalOffset){
		HashMap<String, BBox> mapRet = new HashMap<String, BBox>();
		for(Entry<String, BBox> entry:mapBBoxRealByID.entrySet()){
			BBox valBBox = entry.getValue();
			String valId = entry.getKey();
			if(valBBox.getWidth()!= PATCHSIZE || valBBox.getHeight()!= PATCHSIZE){
				BBox bbox = position2RoundedBBox(valBBox.getCenter2d(),globalOffset);
				mapRet.put(valId, bbox);
				if(bbox.getWidth()!= PATCHSIZE && bbox.getHeight()!= PATCHSIZE){
					System.out.println("FEHLER BEI DER BBOX");
				}
			}
			else{
				mapRet.put(valId, valBBox);
			}
		}
		return mapRet;
	}

	private BBox position2RoundedBBox( Coord position, Coord globalOffset){
		double posx = position.x()-globalOffset.x();
		double posy = position.y()-globalOffset.y();
		double factorx = Math.floor(posx/PATCHSIZE);
		double x0 =  factorx*PATCHSIZE + globalOffset.x();
		double x1 =  (factorx+1)*PATCHSIZE + globalOffset.x();
		double factory = Math.floor(posy/PATCHSIZE);
		double y0 =  factory*PATCHSIZE + globalOffset.y();
		double y1 =  (factory+1)*PATCHSIZE + globalOffset.y();;
		return new BBox(x0, y0, x1, y1);
	}


	public GML_TIN2J3O(File fileRoot) {
		super(fileRoot);
		mapBBoxRealByID = new HashMap<String, BBox>();
		assetManager = new DesktopAssetManager(true);
		offsetx = 0;
		iTrianglesAtAll=0;
		offsety =0;
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

	public void setDestFile(File j3oDestFile){
		this.destFile = j3oDestFile;
	}

	@Override
	protected void finishedParsing(File f) {
		Node n = new Node(f.getName());
		BBox bboxReal  = null;
		HashMap<GMLTYPE,Vector<CoordVector>> map = mapGml.remove(f);
		for(Entry<GMLTYPE, Vector<CoordVector>> entry:map.entrySet()){
			Vector<CoordVector> vecvecTriangles = entry.getValue();
			if(bboxReal==null){
				bboxReal = getBBox(vecvecTriangles);
			}
			else{
				bboxReal.grow2d(getBBox(vecvecTriangles));
			}
			vecvecTriangles = polygons2GLTriangles(vecvecTriangles);
			System.out.println("#triangles = "+vecvecTriangles.size());
			iTrianglesAtAll+=vecvecTriangles.size();
			Vector3f [] vertices = new Vector3f[vecvecTriangles.size()*3];
			Vector3f [] normals = new Vector3f[vertices.length];
			Vector2f [] tx = new Vector2f[vertices.length];
			int [] indexes = new  int [vertices.length];//{ 2,0,1, 1,3,2 };
			BBox boxTile = getBBox(vecvecTriangles);
			int iVertices=0;
			for(CoordVector vec:vecvecTriangles){
				for(Coord c: vec.getCoords()){
					vertices[iVertices] = new Vector3f((float)c.x(), ((float)c.z()), (float)c.y());
					indexes[iVertices] = iVertices;
					tx[iVertices] = calcTextureCoord(boxTile.getMinX(),boxTile.getMinY(),PATCHSIZE,vertices[iVertices].x,vertices[iVertices].z);
					iVertices++;
				}
				Vector3f normal;// = getNormal(vertices[iVertices-1] , vertices[iVertices-2], vertices[iVertices-3]);
				//PSEUDO NORMAL
				normal = new Vector3f(0, 1, 0);
				normals[iVertices-1] = normal;
				normals[iVertices-2] = normal;
				normals[iVertices-3] = normal;
			}
			iVertices=0;

			Mesh mesh = new Mesh();
			mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
			mesh.setBuffer(Type.Index,    3, BufferUtils.createIntBuffer(indexes));
			mesh.setBuffer(Type.Normal,    3, BufferUtils.createFloatBuffer(normals));
			mesh.setBuffer(Type.TexCoord,    2, BufferUtils.createFloatBuffer(tx));
			mesh.updateBound();

			Material mat = new Material(assetManager,  "Common/MatDefs/Light/Lighting.j3md");
			mat.setBoolean("UseMaterialColors",false);  
			//			mat.setColor("Diffuse",  ColorRGBA.Red.mult(0.8f));
			//			mat.setColor("Specular", ColorRGBA.Red.mult(0.8f));
			//			mat.setFloat("Shininess", 3f);
			mat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
			//	mat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Back);
			Geometry geo = new Geometry("geo", mesh); // using our custom mesh object
			geo.setMaterial(mat);
			geo.updateModelBound();
			//						System.out.println("Start LOD creatiobn");
			//						LodGenerator lODGenerator = new LodGenerator(geo);
			//						lODGenerator.bakeLods(TriangleReductionMethod.PROPORTIONAL, 0.1f,0.5f);
			//						System.out.println("finished LOD creatiobn");
			n.attachChild(geo);

		}
		String id = f.getName().substring(0, f.getName().lastIndexOf("."));
		mapBBoxRealByID.put(id, bboxReal);

		//Expoprt to j3o
		BinaryExporter exporter = new BinaryExporter();
		try {

			exporter.save(n, new File(destFile,id+".j3o"));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private Vector2f calcTextureCoord(double minX, double minY,double tilesize, float x, float y) {
		float txx =  (float) ((x-minX)/tilesize);
		float txy =  (float) ((y-minY)/tilesize);
		return new Vector2f(txx, txy);

	}

	private BBox getBBox(Vector<CoordVector> vecvecTriangles) {
		BBox box = null;
		for(CoordVector vec :vecvecTriangles){
			if(vec.getBBox()==null){
				vec.updateBBox2d();
			}
			if(box==null){
				box = vec.getBBox();
			}
			else{
				box.grow2d(vec.getBBox());
			}
		}
		return box;
	}

	private Vector<CoordVector> polygons2GLTriangles(Vector<CoordVector> vecvec){
		Vector<CoordVector> vecvecret = new Vector<CoordVector>();
		for(CoordVector vec : vecvec){
			CoordVector vecTemp = new CoordVector();
//			int i=0;
			int size = vec.size();
			//			System.out.println(size);
			switch (size){
			//triangle
			case 4:{
				//				for(Coord c:vec.getCoords()){
				//					Coord cret = new Coord(c.x()-offsetx, c.y()-offsety, c.z()); 
				//					vecTemp.add(cret);
				//					if(i==2){
				//						break;
				//					}
				//					i++;
				//				}
				//				if(getNormal(vecTemp).z<0){
				//					vecvecret.add(vecTemp.getReversed());
				//				}
				//				else{
				//					vecvecret.add(vecTemp);
				//				}
				//				break;
			}
			//Quad
			case 5:{
				//				for(Coord c:vec.getCoords()){
				//					Coord cret = new Coord(c.x()-offsetx, c.y()-offsety, c.z()); 
				//					vecTemp.add(cret);
				//					i++;
				//				}
				//				CoordVector vecret2 = new CoordVector();
				//				vecret2.add(vecTemp.getCoord(0));
				//				vecret2.add(vecTemp.getCoord(1));
				//				vecret2.add(vecTemp.getCoord(2));
				//				if(getNormal(vecret2).z<0){
				//					vecvecret.add(vecret2.getReversed());
				//				}
				//				else{
				//					vecvecret.add(vecret2);
				//				}
				//				CoordVector vecret3 = new CoordVector();
				//				vecret3.add(vecTemp.getCoord(0));
				//				vecret3.add(vecTemp.getCoord(2));
				//				vecret3.add(vecTemp.getCoord(3));
				//				if(getNormal(vecret3).z<0){
				//					vecvecret.add(vecret3.getReversed());
				//				}
				//				else{
				//					vecvecret.add(vecret3);
				//				}
				//				break;
			}
			default:{
				for(Coord c:vec.getCoords()){
					Coord cret = new Coord(c.x()-offsetx, c.y()-offsety, c.z()); 
					vecTemp.add(cret);
				}
				vecTemp.remove(vecTemp.size()-1);
				for(CoordVector vecret2: J3DTriangulator.triangulate(vecTemp)){
					if(getNormal(vecret2).z<0){
						vecvecret.add(vecret2.getReversed());
					}
					else{
						vecvecret.add(vecret2);
					}
				}
				break;
			}
			}


		}
		return vecvecret;
	}

	public void setAssetManager(AssetManager am){
		this.assetManager = am;
	}

	public Vector<Geometry> getGeoms(){
		return geoms;
	}




	@Override
	protected void parseGMLClass(String currentName, File f, Element e,	Iterator<Element> processDescendants) {
		GMLTYPE type = GMLTYPE.parseTye(currentName);
		//		if(type!= GMLTYPE.FEATUREMEMBER){
		//			return;
		//		}
		if(type!= GMLTYPE.SURFACEPROPERTY){
			return;
		}
		while(!(e = processDescendants.next()).getName().equals(GMLTYPE.POSLIST.getName())) {
			//skip Elements
		}
		parsePosList(f, e, type, processDescendants);

	}


	@SuppressWarnings("unused")
	private Vector3f[] normalizeNormals(Vector3f[] coords, Vector3f[] normals) {
		//		HashMap<String, Vector3f> mapCoordsByID = new HashMap<String, Vector3f>();
		HashMap<String, Vector<Vector3f>> mapNormalsByID = new HashMap<String, Vector<Vector3f>>(); 
		int i =0;
		for(Vector3f vec:coords){
			String id = getCoordID(vec);
			if(mapNormalsByID.containsKey(id)){
				mapNormalsByID.get(id).add(normals[i]);
			}
			else{
				Vector<Vector3f> vecAllNormals = new Vector<Vector3f>();
				vecAllNormals.add(normals[i]);
				mapNormalsByID.put(id, vecAllNormals);
			}
			i++;
		}
		i=0;
		for(Vector3f vec:coords){
			String id = getCoordID(vec);
			Vector3f newNormal = null;;
			Vector<Vector3f> allNormalsAtThisPoint = mapNormalsByID.get(id);
			int iNormalsAtThisPoint =0;
			for (Vector3f oldNormal: allNormalsAtThisPoint){
				if(newNormal == null){
					newNormal=oldNormal;
					iNormalsAtThisPoint++;
				}
				else{
					newNormal = newNormal.add(oldNormal);
					iNormalsAtThisPoint++;
				}
			};
			newNormal.divide((float)iNormalsAtThisPoint);
			normals[i] = newNormal.multLocal(1);
			i++;
		}
		return normals;
	}


	private String getCoordID(Vector3f vec){
		int dx = (int)(Math.round(vec.x*100));
		int dy = (int)(Math.round(vec.y*100));
		int dz = (int)(Math.round(vec.z*100));
		return String.valueOf(dx)+"_"+String.valueOf(dy)+"_"+String.valueOf(dz);
	}


}
