package de.wuppertal.terrain;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.jme3.asset.AssetManager;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;

import de.wuppertal.BBox;
import de.wuppertal.Coord;
import de.wuppertal.tools.terrainCreation.TerrainCreatorHelper;
import de.wuppertal.tools.textureCreation.WMSImageCatcher;

/**
 * 
 * @author KrueckhansM
 *
 */
public class StaticRegionTerrain{
	public final static String sceneGraphID= "terrain_region";
	private Material mat;
	private TerrainQuad regionTerrainNode;
	
	/**
	 * regular constructor
	 * @param am
	 */
	public StaticRegionTerrain(float yOffset,AssetManager am,Camera cam){
		regionTerrainNode = (TerrainQuad) am.loadModel(sceneGraphID+".j3o");
		TerrainLodControl controlSurround = new TerrainLodControl(regionTerrainNode, cam);
		regionTerrainNode.addControl(controlSurround);
		regionTerrainNode.setShadowMode(ShadowMode.Off);
		regionTerrainNode.setLocalTranslation(regionTerrainNode.getLocalTranslation().add(0, yOffset, 0));
		//refrenced textures within the j3o file should be load automatically
		mat = regionTerrainNode.getMaterial();
	}
	
	/**
	 * for test cases, admins or first launch  only
	 * use this constructor to create a j3o File (creationMode==true) and a jpg texture File. forces TextureLoading from WMS and new calculation of j30 File
	 * @param dgmResolution
	 * @param srcFolder
	 * @param dstFolder
	 * @param iTextureResolution
	 * @param wmsExampleQuery
	 * @param am
	 * @param cam
	 * @param ts
	 */
	public StaticRegionTerrain(float yOffset,int dgmResolution, File srcFolder, File dstFolder,int iTextureResolution,String wmsExampleQuery,AssetManager am, Camera cam, Wuppertal3D ts){
		long t0 = System.currentTimeMillis();
		TerrainCreatorHelper inst = TerrainCreatorHelper.getInstance();
		File worldFile = new File(srcFolder,dgmResolution+"terrain_region.wld");
		File heightDataFile = new File(srcFolder,dgmResolution+"terrain_region.txt"); 
		Coord cDeltasSurround = inst.getDeltasFromWorldFile(worldFile);
//		Coord cllSurround = inst.getLowerLeftFromWorldFile(worldFile);
//		Coord urSurround = cllSurround.plusNew(dgmResolution*cDeltasSurround.x(), dgmResolution*cDeltasSurround.y());
		//Load Heightdata
		//Replace all values<"minValue" with minReplaceValue. Example: replace all heights<0 with -1000
		float[] mapSurround = inst.getJMEHeightDataFromRasterFile(true,  heightDataFile,cDeltasSurround.x(),cDeltasSurround.y(),0,-1000);
		//create Jmonkey TerrainQuad
		regionTerrainNode = new TerrainQuad(sceneGraphID, dgmResolution/16+1, dgmResolution+1, mapSurround);
		regionTerrainNode.setLocalTranslation((float)(cDeltasSurround.x()*dgmResolution)/2 / 3, -ts.MIN_TERRAIN_HEIGHT, (float)(cDeltasSurround.y()*dgmResolution)/2 /3);
		regionTerrainNode.setLocalScale((float)cDeltasSurround.x(), 1, (float)cDeltasSurround.y());
		//load Texture from WMS 
		WMSImageCatcher catcher = WMSImageCatcher.getInstance(false);
		catcher.setParameterByExampleGetMapRequest(wmsExampleQuery);
		//set Box for region
		BBox boxSurround = ts.getBBoxFullTerrain().clone();
		boxSurround.grow2d(1);
		catcher.setBBox(boxSurround);
		//set iomage size
		catcher.setImageHeight(iTextureResolution);
		catcher.setImageWidth(iTextureResolution);
		BufferedImage img = catcher.getImage(true);
		//try saving the image
		try{
			ImageIO.write(img, "jpeg", new File(dstFolder,sceneGraphID+".jpg"));
		}
		catch(Exception e){
			e.printStackTrace();
			System.err.println("could not write image for region terrain from WMS");
		}
		//Texture texture = new Texture2D(new AWTLoader().load(img, true));
		Texture texture = am.loadTexture(sceneGraphID+".jpg");
		texture.setWrap(WrapMode.Clamp);
		//set material
		mat =  new Material(am,    "Common/MatDefs/Light/Lighting.j3md");  // ... specify .j3md file to use 
		mat.setTexture("DiffuseMap", texture);
		mat.setBoolean("UseMaterialColors",true);
		mat.setColor("Diffuse",  ColorRGBA.White);
		mat.setColor("Specular",  ColorRGBA.DarkGray.mult(2));
		mat.setFloat("Shininess", 12);
		mat.getAdditionalRenderState().setWireframe(false);
		regionTerrainNode.setMaterial(mat);
		//enable LOD
		TerrainLodControl controlSurround = new TerrainLodControl(regionTerrainNode, cam);
		regionTerrainNode.addControl(controlSurround);
		regionTerrainNode.setShadowMode(ShadowMode.Off);
		//Expoprt to j3o
		BinaryExporter exporter = new BinaryExporter();
		try {
			exporter.save(regionTerrainNode, new File(dstFolder,sceneGraphID+".j3o"));
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("could not write j3o for regionDGM");
		}
		long t1 = System.currentTimeMillis();
		System.out.println("StaticRegionTerrain creation finished in "+(t1-t0)+" ms");
		//nach dem Speichern
		regionTerrainNode.setLocalTranslation(regionTerrainNode.getLocalTranslation().add(0, yOffset, 0));
	}
	

	public String getSceneGraphID() {
		return sceneGraphID;
	}

	public TerrainQuad getRegionTerrainNode() {
		return regionTerrainNode;
	}
	
	public void switchWireFrame(){
		mat.getAdditionalRenderState().setWireframe(!mat.getAdditionalRenderState().isWireframe());
		regionTerrainNode.setMaterial(mat);
	}
}
