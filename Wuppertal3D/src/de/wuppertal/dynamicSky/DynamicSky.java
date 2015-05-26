package de.wuppertal.dynamicSky;

import com.jme3.asset.AssetManager;
import com.jme3.light.DirectionalLight;
import com.jme3.math.Vector3f;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;

public class DynamicSky extends Node {
    private DynamicSun dynamicSun ;
    private DynamicSkyBackground dynamicBackground ;
    
    
    public DynamicSky(AssetManager assetManager, ViewPort viewPort, Node rootNode,float distanceScaling) {
        super("Sky");
        dynamicSun = new DynamicSun(assetManager, viewPort, rootNode, distanceScaling);
        rootNode.attachChild(dynamicSun);
        
        
        dynamicBackground = new DynamicSkyBackground(assetManager, viewPort, rootNode);
    }
    
    public Vector3f getSunDirection(){
        return dynamicSun.getSunDirection();
    }
        
    public void updateTimeIncrement(Vector3f vecCamPosition){
        dynamicSun.updateTimeIncrement(vecCamPosition);
        dynamicBackground.updateLightPosition(dynamicSun.getSunSystem().getPosition());
    }
    
    public void updateTime(Vector3f vecCamPosition){
        dynamicSun.updateTime(vecCamPosition);
        dynamicBackground.updateLightPosition(dynamicSun.getSunSystem().getPosition());
    }
    
    public DirectionalLight getSunLight(){
    	return dynamicSun.getSunLight();
    }
    
    public SunSystem getSunSystem(){
    	return dynamicSun.getSunSystem();
    }
}