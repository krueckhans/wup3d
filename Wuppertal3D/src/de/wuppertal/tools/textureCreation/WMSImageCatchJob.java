package de.wuppertal.tools.textureCreation;

import java.awt.image.BufferedImage;

import de.wuppertal.BBox;

public class WMSImageCatchJob {
	private BufferedImage image;
	private BBox boxImage;
	private String strImageId;
	public WMSImageCatchJob(BufferedImage image, BBox boxImage,String strImageId){
		this.boxImage = boxImage;
		this.image = image;
		this.strImageId = strImageId;
	}
	
	public WMSImageCatchJob(BBox boxImage,String strImageId){
		this.boxImage = boxImage;
		this.strImageId = strImageId;
	}
	
	public BufferedImage getImage(){
		return image;
	}
	
	public BBox getBBox(){
		return boxImage;
	}
	public String getId(){
		return strImageId;
	}
	
	public void setImage(BufferedImage image){
		this.image = image;
	}
}
