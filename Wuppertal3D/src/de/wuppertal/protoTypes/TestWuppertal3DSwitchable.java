package de.wuppertal.protoTypes;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.DepthOfFieldFilter;
import com.jme3.post.filters.FogFilter;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.system.AppSettings;

import de.wuppertal.BBox;
import de.wuppertal.dynamicSky.DynamicSky;
import de.wuppertal.terrain.DynamicPatchedBuildings;
import de.wuppertal.terrain.DynamicPatchedTerrain;
import de.wuppertal.terrain.SingleBuildingPatch;
import de.wuppertal.terrain.SingleTerrainTinPatch;
import de.wuppertal.terrain.StaticRegionTerrain;
import de.wuppertal.terrain.StaticTerrain;
import de.wuppertal.terrain.Wuppertal3D;

public class TestWuppertal3DSwitchable extends SimpleApplication implements ActionListener{
	DynamicSky sky ;
	DirectionalLightShadowRenderer pssmRenderer ;
	DepthOfFieldFilter  dofFilter;
	Wuppertal3D terrainSystem ;
	
	
	public static void main(String[] args) {
		TestWuppertal3DSwitchable app = new TestWuppertal3DSwitchable();
		AppSettings settings = new AppSettings(true);
		settings.setWidth(1920);
		settings.setHeight(1080);
//		settings.setWidth(1200);
//		settings.setHeight(800);
		settings.setFullscreen(true);
		app.setSettings(settings);
		app.setDisplayFps(true);
		app.setDisplayStatView(false);
		app.setShowSettings(true);
		app.start();	
	}


	@Override
	public void simpleInitApp() {

		inputManager.addMapping("m",  new KeyTrigger(KeyInput.KEY_M));
		inputManager.addMapping("k",  new KeyTrigger(KeyInput.KEY_K));
		inputManager.addMapping("n",  new KeyTrigger(KeyInput.KEY_N));
		inputManager.addMapping("j",  new KeyTrigger(KeyInput.KEY_J));
		inputManager.addMapping("b",  new KeyTrigger(KeyInput.KEY_B));
		inputManager.addMapping("h",  new KeyTrigger(KeyInput.KEY_H));
		inputManager.addMapping("v",  new KeyTrigger(KeyInput.KEY_V));
		inputManager.addMapping("g",  new KeyTrigger(KeyInput.KEY_G));
		inputManager.addListener(this, "m");
		inputManager.addListener(this, "k");
		inputManager.addListener(this, "n");
		inputManager.addListener(this, "j");
		inputManager.addListener(this, "b");
		inputManager.addListener(this, "h");
		inputManager.addListener(this, "v");
		inputManager.addListener(this, "g");
		assetManager = new DesktopAssetManager(true);
		//		assetManager.registerLocator("C:\\Temp\\ddddaten\\sky", FileLocator.class);
		assetManager.registerLocator("\\\\s102x003\\Wuppertal3D$\\sky", FileLocator.class);
		
		//Globale Fluggeschwindigkeit
		flyCam.setMoveSpeed(1000);
		//Globale Sichtdistanz
		cam.setFrustumFar(30000);
		sky = new DynamicSky(assetManager, viewPort, rootNode,cam.getFrustumFar()*0.9f);
		
		

		BitmapText statusTextStatic = new BitmapText(guiFont, false);          
		statusTextStatic.setSize(guiFont.getCharSet().getRenderedSize());      // font size
		statusTextStatic.setColor(ColorRGBA.Black);                             // font color
		statusTextStatic.setText("Zu Ladende Kacheln (Geometrie):\nZu Ladende Kacheln (Texturen):");             // the text
		statusTextStatic.setLocalTranslation(0, settings.getHeight(), 0); // position
		guiNode.attachChild(statusTextStatic);


		BitmapText statusTextDynamic = new BitmapText(guiFont, false);          
		statusTextDynamic.setSize(guiFont.getCharSet().getRenderedSize());      // font size
		statusTextDynamic.setColor(ColorRGBA.Black);     
		statusTextDynamic.setText("");// font color
		statusTextDynamic.setLocalTranslation(0, settings.getHeight(), 0); // position
		guiNode.attachChild(statusTextDynamic);

		terrainSystem = new Wuppertal3D((DesktopAssetManager) assetManager, cam, rootNode,statusTextStatic,statusTextDynamic,false);
		BBox box = terrainSystem.getBBoxFullTerrain();
		cam.setLocation(new Vector3f((float)(box.getWidth()/2), 1000f,(float) box.getHeight()));
		cam.lookAt(new Vector3f((float)(box.getWidth()/2),0,0), new Vector3f(0, 1, 0));
		rootNode.setShadowMode(ShadowMode.CastAndReceive);

		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
		String dateInString = "23-06-2014 07:00:00";
		Date date;
		try {
			date = sdf.parse(dateInString);
			sky.getSunSystem().updateByDate(date);;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		sky.updateTime(cam.getLocation());
		sky.setShadowMode(ShadowMode.Off);
		
		
		//Filter and Special Effects
		//Shadows
		pssmRenderer = new DirectionalLightShadowRenderer(assetManager, 2*2048, 2);
		pssmRenderer.setShadowZExtend(300);
		pssmRenderer.setLight(sky.getSunLight());
		pssmRenderer.setShadowIntensity(0.6f);
		viewPort.addProcessor(pssmRenderer);
		
		FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
		//nice but too expensive
		//	        SSAOFilter ssao = new SSAOFilter();//0.49997783f, 42.598858f, 35.999966f, 0.39299846f
		//	        fpp.addFilter(ssao);
		//Tiefenschhärfe
		dofFilter = new DepthOfFieldFilter();
		dofFilter.setFocusDistance(100);
		dofFilter.setFocusRange(1800);
		dofFilter.setBlurScale(2.f);
		fpp.addFilter(dofFilter);
		//Nebel
		FogFilter  fog=new FogFilter();
		fog.setFogColor(ColorRGBA.LightGray);
		fog.setFogDistance(cam.getFrustumFar()*4/5);
		fog.setFogDensity(1.2f);
		fpp.addFilter(fog);
		viewPort.addProcessor(fpp);
	}


	@Override
	public void simpleUpdate(float tpf) {
		terrainSystem.update(tpf);
		sky.updateTime(cam.getLocation());
		pssmRenderer.setLight(sky.getSunLight());
//		sky.updateTimeIncrement(cam.getLocation());
		Vector3f origin = cam.getWorldCoordinates(new Vector2f(settings.getWidth() / 2, settings.getHeight() / 2), 0.0f);
		Vector3f direction = cam.getWorldCoordinates(new Vector2f(settings.getWidth() / 2, settings.getHeight() / 2), 0.3f);
		direction.subtractLocal(origin).normalizeLocal();
		Ray ray = new Ray(origin, direction);
		CollisionResults results = new CollisionResults();
		int numCollisions = rootNode.collideWith(ray, results);
		if (numCollisions > 0) {
			CollisionResult hit = results.getClosestCollision();
			dofFilter.setFocusDistance(hit.getDistance()/10.0f);
		}
	}


	public void onAction(String name, boolean value, float tpf) {
		if(!value){
			return;
		}
		System.out.println("switch "+name);
		switch(name){
		case "m":{
			SingleBuildingPatch.bWireframe = ! SingleBuildingPatch.bWireframe;
			break;
		}
		case "k":{
			terrainSystem.switchVisibility(DynamicPatchedBuildings.sceneGraphID);
			break;
		}
		case "n":{
			SingleTerrainTinPatch.bWireframe = ! SingleTerrainTinPatch.bWireframe;
			break;
		}
		case "j":{
			terrainSystem.switchVisibility(DynamicPatchedTerrain.sceneGraphID);
			break;
		}
		case "b":{
			terrainSystem.switchWireFrameStaticTerrain();
			break;
		}
		case "h":{
			terrainSystem.switchVisibility(StaticTerrain.sceneGraphID);
			break;
		}
		case "v":{
			terrainSystem.switchWireFrameRegionTerrain();
			break;
		}
		case "g":{
			terrainSystem.switchVisibility(StaticRegionTerrain.sceneGraphID);
			break;
		}
		}
	}







}
