package de.wuppertal.dynamicSky;

import com.jme3.asset.AssetManager;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Node;
import java.util.Date;

public class DynamicSun extends Node {
    private SunSystem sunSystem ;
    private SkyBillboardItem sun;
    
    private DirectionalLight sunLight = null;
    private Vector3f lightDir;
    private Vector3f lightPosition = new Vector3f();
    
    public DynamicSun(AssetManager assetManager, ViewPort viewPort, Node rootNode, float scaling) {
        sunSystem = new SunSystem(new Date(), 0, 0, 0,scaling);
        lightDir = sunSystem.getPosition();
        sunLight = getSunLight();
        rootNode.addLight(sunLight);
                
        sunSystem.setSiteLatitude(51f);
        sunSystem.setSiteLongitude(7f);
        updateLightPosition();
        
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", assetManager.loadTexture("Textures/sun.png"));
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        mat.getAdditionalRenderState().setDepthWrite(false);
        
        sun = new SkyBillboardItem("sun", scaling/10);
        sun.setMaterial(mat);
        attachChild(sun);
        
        setQueueBucket(Bucket.Sky);
        setCullHint(CullHint.Never);
    }
    
    public SunSystem getSunSystem(){
        return sunSystem;
    }
    
    protected DirectionalLight getSunLight(){
        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(lightDir);
        dl.setColor(ColorRGBA.White.mult(1.5f));
        return dl;
    }
        
    protected void updateLightPosition(){
        lightDir = sunSystem.getDirection();
        lightPosition = sunSystem.getPosition();
    }
    
    public Vector3f getSunDirection(){
        return sunSystem.getPosition();
    }

    public void updateTimeIncrement(Vector3f vecCamLocation) {
        // make everything follow the camera
        setLocalTranslation(vecCamLocation);
        sunSystem.updateSunPosition(0, 0, 10); // increment by 30 seconds
        updateLightPosition();
        sunLight.setDirection(lightDir);
        sun.setLocalTranslation(lightPosition.mult(0.95f));
    }
    
    public void updateTime(Vector3f vecCamLocation) {
    	 // make everything follow the camera
        setLocalTranslation(vecCamLocation);
        sunSystem.updateSunPosition(0, 0, 0); // increment by 30 seconds
        updateLightPosition();
        sunLight.setDirection(lightDir);
        sun.setLocalTranslation(lightPosition.mult(1f));
    }
}
