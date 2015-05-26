package de.wuppertal.tools.textureCreation;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import de.wuppertal.BBox;

public class WMSImageCatcher {
	private static WMSImageCatcher instance;
	private static StringBuilder AND = new StringBuilder("&");
	private StringBuilder urlSaticPart;
	private StringBuilder urlDynamicPart;
	private StringBuilder imageWidth;
	private StringBuilder imageHeight;
	private StringBuilder styles;
	private StringBuilder layers;
	private StringBuilder request;
	private StringBuilder bbox;
	private StringBuilder srs;
	private ImageType imageType;
	enum ImageType{
		JPG(new StringBuilder("FORMAT=image/jpeg")),
		PNG(new StringBuilder("FORMAT=image/png")),
		TIFF(new StringBuilder("FORMAT=image/tiff"));
		private StringBuilder strType;
		ImageType(StringBuilder strType){
			this.strType = strType;
		}
		public StringBuilder getAsStringBuilder(){
			return this.strType;
		}
		public static ImageType parseImageType(String s){
			switch(s){
			case "image/jpg": case "image/jpeg":{
				return JPG; 
			}
			case "image/png":{
				return PNG;
			}
			case "image/tiff":{
				return TIFF;
			}
			}
			return null;
		}
	};

	/**
	 * get WMSCatcher instance
	 * @param getAsSingleton true-> uses static instance (singleton) false -> creates new instance
	 * @return
	 */
	public static WMSImageCatcher getInstance(boolean getAsSingleton){
		if(getAsSingleton){
			return new WMSImageCatcher();
		}
		else{
			if(instance==null){
				instance = new WMSImageCatcher();
			}
			return instance;
		}
	}

	public void setImageType(ImageType imageType){
		this.imageType = imageType;
	}

	public void setImageType(String strImageType){
		this.imageType = ImageType.parseImageType(strImageType);
	}

	public void setImageWidth(int iWidth){
		this.imageWidth = new StringBuilder("WIDTH=");
		imageWidth.append(iWidth);
	}

	public void setImageHeight(int iHeight){
		this.imageHeight = new StringBuilder("HEIGHT=");
		imageHeight.append(iHeight);
	}

	/**
	 * URL must start with "http://" and end width "GetMap" like
	 * @param url
	 */
	public void setUrl(StringBuilder url){
		this.urlSaticPart = url;
	}

	public void setStyle(StringBuilder style){
		this.styles = new StringBuilder("STYLES=");
		this.styles.append(style);
	}

	public void setLayers(StringBuilder layers){
		this.layers = new StringBuilder("LAYERS=");
		this.layers.append(layers);
	}

	public void setBBox(BBox bbox){
		this.bbox = bbox.getAsWMSRequestParameter();
	}

	public StringBuilder getRequest(){
		updateRequest();
		return request;
	}
	
	public StringBuilder getRequestStaticPart(){
		return urlSaticPart;
	}
	
	
	public StringBuilder getDynamicPart(){
		updateRequest();
		return urlDynamicPart;
	}

	public void setEPSG(int epsg){
		this.srs = new StringBuilder("SRS=EPSG:");
		srs.append(epsg);
	}

	public void setParameter(StringBuilder url, BBox bbox,int iWidth, int iHeight, int epsg, StringBuilder layers, StringBuilder styles){
		setUrl(url);
		setBBox(bbox);
		setImageWidth(iWidth);
		setImageHeight(iHeight);
		setEPSG(epsg);
		setLayers(layers);
		setStyle(styles);
		setImageType(imageType);
	}

	public void setParameterByExampleGetMapRequest(String exampleRequest){
		//			http://s102w384:8399/arcgis/services/WuNDa-Orthophoto-NRW/MapServer/WMSServer?&VERSION=1.1.1&REQUEST=GetMap&BBOX=374327.,5681000,375327.,5682000&WIDTH=4096&HEIGHT=4096&SRS=EPSG:25832&FORMAT=image/png&LAYERS=1&STYLES=default
		if (exampleRequest.startsWith("http://")){
			setUrl(new StringBuilder(exampleRequest.substring(0, exampleRequest.indexOf("GetMap")+6)));
			exampleRequest = exampleRequest.substring(exampleRequest.indexOf("GetMap")+6 , exampleRequest.length());
		}
		else{
			new RuntimeException("unknown WMS example URL");
		}
		StringTokenizer st = new StringTokenizer(exampleRequest,"&");
		while(st.hasMoreTokens()){
			String token = st.nextToken();
			String key = token.substring(0, token.indexOf("="));
			String value = token.substring(token.indexOf("=")+1, token.length());
			switch (key){
			case "BBOX":{
				setBBox(BBox.getBBoxByWMSValue(value));
				break;
			}
			case "WIDTH":{
				setImageWidth(Integer.parseInt(value));
				break;
			}
			case "HEIGHT":{
				setImageHeight(Integer.parseInt(value));
				break;
			}
			case "LAYERS":{
				setLayers(new StringBuilder(value));
				break;
			}
			case "STYLES":{
				setStyle(new StringBuilder(value));
				break;
			}
			case "FORMAT":{
				setImageType(value);
				break;
			}
			case "SRS":{
				int iIndex = value.indexOf(":");
				value = value.substring(iIndex+1,value.length());
				setEPSG(Integer.parseInt(value));
				break;
			}
			}
		}
	}

	public void updateRequest(){
		request = new StringBuilder();
		request.append(urlSaticPart);
		urlDynamicPart = new StringBuilder();
		urlDynamicPart.append(AND);
		urlDynamicPart.append(bbox);
		urlDynamicPart.append(AND);
		urlDynamicPart.append(imageWidth);
		urlDynamicPart.append(AND);
		urlDynamicPart.append(imageHeight);
		urlDynamicPart.append(AND);
		urlDynamicPart.append(srs);
		urlDynamicPart.append(AND);
		urlDynamicPart.append(imageType.getAsStringBuilder());
		urlDynamicPart.append(AND);
		urlDynamicPart.append(layers);
		urlDynamicPart.append(AND);
		urlDynamicPart.append(styles);
		request.append(urlDynamicPart);
	}

	public BufferedImage getImage(boolean doUpdateRequest){
		if(doUpdateRequest){
			updateRequest();
		}
//		System.out.println("Starting request:"+getRequest());
		try {
			return ImageIO.read(new URL(getRequest().toString()));
		} catch (Exception e) {
			System.err.println(getRequest()+" \n request failed");
			e.printStackTrace();
		}
		return null;
	}
	
	public static WMSImageCatcher[] getInstances(int iInstances, String wmsExampleRequest){
		WMSImageCatcher [] retInstances = new WMSImageCatcher[iInstances];
		for(int i=0;i<iInstances;i++){
			retInstances[i] = new WMSImageCatcher();
			retInstances[i].setParameterByExampleGetMapRequest(wmsExampleRequest);
		}
		return retInstances;
	}
	


	//	http://s102w384:8399/arcgis/services/WuNDa-Orthophoto-NRW/MapServer/WMSServer?&VERSION=1.1.1&REQUEST=GetMap&BBOX=374327.,5681000,375327.,5682000&WIDTH=4096&HEIGHT=4096&SRS=EPSG:25832&FORMAT=image/png&LAYERS=1&STYLES=default
}
