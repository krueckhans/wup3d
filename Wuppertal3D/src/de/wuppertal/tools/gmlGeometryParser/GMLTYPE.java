package de.wuppertal.tools.gmlGeometryParser;

public enum GMLTYPE {
	NAME("name","gml",GMLTYPETYPE.CLASS),
	BOUNDEDBY("bounedby","gml",GMLTYPETYPE.CLASS),
	ENVELOPE("Envelop","gml",GMLTYPETYPE.CLASS),
	LOWERCORNER("lowerCorner","gml",GMLTYPETYPE.CLASS),
	UPPERCORNER("UpperCorner","gml",GMLTYPETYPE.CLASS),

	CITYOBJECTMEMBER("cityObjectMember","core",GMLTYPETYPE.CLASS),
	BUILDING("Building","bldg",GMLTYPETYPE.CLASS),

	WALLSURFACE("WallSurface","bldg",GMLTYPETYPE.CLASS),
	FEATUREMEMBER("featureMember","gml",GMLTYPETYPE.CLASS),
	GROUNDSURFACE("GroundSurface","bldg",GMLTYPETYPE.CLASS),
	
	ROOFSURFACE("RoofSurface","bldg",GMLTYPETYPE.CLASS),
	LOD2MULTISURFACE("lod2MultiSurface","bldg",GMLTYPETYPE.CLASS),
	LOD4GEOMETRY("lod4Geometry","bldg",GMLTYPETYPE.CLASS),
	MULTISURFACE("MultiSurface","gml",GMLTYPETYPE.CLASS),
	SURFACEMEMBER("surfaceMember","gml",GMLTYPETYPE.CLASS),
	SURFACEPROPERTY("surfaceProperty","gml",GMLTYPETYPE.CLASS),
	POLYGON("POLYGON","gml",GMLTYPETYPE.CLASS),
	EXTERIOR("exterior","gml",GMLTYPETYPE.CLASS),
	LINEARRING("LinearRing","gml",GMLTYPETYPE.CLASS),
	MULTISURFACEPROPERTY("multiSurfaceProperty","gml",GMLTYPETYPE.CLASS),
	POSLIST("posList","gml",GMLTYPETYPE.CLASS);
	
	public enum GMLTYPETYPE{CLASS,ATTRIBUTE}
	private String name;
	private int nameHashcode;
	private String nameSpace;
	private String namefull;
	private GMLTYPETYPE type;
	
	private GMLTYPE(String name,String nameSpace, GMLTYPETYPE type){
		this.name = name;
		this.nameHashcode = name.hashCode();
		this.nameSpace = nameSpace;
		this.namefull = this.nameSpace+":"+this.name;
		this.type = type;
	}
	
	public String getName(){
		return name;
	}
	
	public String getNameSpace(){
		return name;
	}
	
	public String get(){
		return namefull;
	}
	
	public GMLTYPETYPE getType(){
		return type;
	}
	
	private boolean matches(String type,int typeHashcode){
		if(typeHashcode==nameHashcode && type.equals(name) ){
			return true;
		}
		return false;
	}
	
	public static GMLTYPE parseTye(String strType){
		int typeHashCode= strType.hashCode();
		for(GMLTYPE type : GMLTYPE.values()){
			if(type.matches(strType, typeHashCode)){
				return type;
			}
		}
		return null;
	}
}
