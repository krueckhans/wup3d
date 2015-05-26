package de.wuppertal;

import java.util.StringTokenizer;
import java.util.Vector;

public class BBox {
	private static double DEFAULTNOZVALUE = -10000;
	private Coord ll;
	private Coord lr;
	private Coord ul;
	private Coord ur;
	private double minz;
	private double maxz;

	public BBox(Coord ll, Coord ur){
		this.ll = new Coord(ll.x(),ll.y(),ll.z());
		this.ur = new Coord(ur.x(), ur.y(), ur.z());
		this.lr = new Coord(ur.x(),ll.y(),ll.z());
		this.ul = new Coord(ll.x(),ur.y(),ur.z());
		this.minz =DEFAULTNOZVALUE;
		this.maxz =DEFAULTNOZVALUE;
	}

	public BBox(double llx, double lly,double urx, double ury){
		this(new Coord(llx, lly, 0), new Coord(urx, ury, 0));
	}

	public static BBox getBBoxByWMSValue(String wmsBBoxRequest){
		StringTokenizer st = new StringTokenizer(wmsBBoxRequest,",");
		double llx = Double.parseDouble(st.nextToken());
		double lly = Double.parseDouble(st.nextToken());
		double urx = Double.parseDouble(st.nextToken());
		double ury = Double.parseDouble(st.nextToken());
		return new BBox(llx,lly,urx,ury);
	}

	public double getWidth(){
		return ur.x()-ll.x();
	}

	public double getHeight(){
		return ur.y()-ll.y();
	}

	public StringBuilder getAsWMSRequestParameter(){
		StringBuilder bboxString = new StringBuilder();
		bboxString.append("BBOX=");
		bboxString.append(ll.x());
		bboxString.append(",");
		bboxString.append(ll.y());
		bboxString.append(",");
		bboxString.append(ur.x());
		bboxString.append(",");
		bboxString.append(ul.y());
		return bboxString;
	}

	public static Vector<BBox> getTiles(BBox box, double deltaX,double deltaY){
		Vector<BBox> vecBBoxTiles = new Vector<BBox>();
		for(double xTemp = box.ll.x();xTemp <box.ur.x();xTemp+=deltaX ){
			for(double yTemp = box.ll.y();yTemp <box.ur.y();yTemp+=deltaY ){
				vecBBoxTiles.add(new BBox(xTemp, yTemp, xTemp+deltaX, yTemp+deltaY));
			}
		}
		return vecBBoxTiles;
	}

	public  Vector<BBox> getTiles(double deltaX,double deltaY){
		return BBox.getTiles(this, deltaX, deltaY);
	}

	public CoordVector createCoordsInBBox(boolean bLinewise,double deltaX,double deltaY){
		Coord coords [][] =null;
		if(bLinewise){
			coords = createCoordsOfBBox2DLinewise(deltaX, deltaY);
		}
		else{
			coords = createCoordsOfBBox2DLinewise(deltaX, deltaY);
		}
		CoordVector ret = new CoordVector();
		for(int i=0; i<coords.length;i++ ){
			for(int j=0; j<coords[0].length;j++ ){
				ret.add2d(coords[i][j]);
			}
		}
		return ret;
	}

	/**
	 * 
	 * @param estimated2NSize
	 * @return 2D Koordinaten innerhalb des Rasters. ACHTUNG
	 */
	public CoordVector createCoordsInBBox(int estimated2NSize, boolean bAddDeltaXYAsFirstCoord){
		double widthX = getWidth()/((double)estimated2NSize);
		double widthY = getHeight()/((double)estimated2NSize);

		CoordVector cvec = createCoordsInBBox(true, widthX, widthY);
		System.out.println(cvec.size()+"  "+(estimated2NSize+1)*(estimated2NSize+1));
		if(bAddDeltaXYAsFirstCoord){
			cvec.addFirst(new Coord(widthX, widthY,1));
		}
		return cvec;
	}


	public Coord[][] createCoordsOfBBox2DLinewise(double deltaX,double deltaY){
		int iCols = (int) (getWidth()/deltaX)+1;
		int iRows = (int) (getHeight()/deltaY)+1;
		Coord ret [][] = new Coord[iRows][iCols];
		int ix,iy;
		ix = iy =0;
		//		for(double yTemp = ll.y();yTemp <=ur.y();yTemp+=deltaY ){
		//			for(double xTemp = ll.x();xTemp <=ur.x();xTemp+=deltaX ){
		//				ret[iy][ix] = new Coord(xTemp, yTemp, 0);
		//				ix++;
		//			}
		//			iy++;
		//			ix=0;
		//		}
		double yTemp = ll.y();
		double xTemp = ll.x();
		for(iy =0;iy<iCols;iy++){
			for(ix =0 ;ix<iRows;ix++){
				ret[iy][ix] = new Coord(xTemp, yTemp, 0);
				xTemp+=deltaX;
			}
			xTemp = ll.x();
			yTemp += deltaY;
		}


		return ret;
	}

	public Coord[][] createCoordsOfBBox2DColumnwise(double deltaX,double deltaY){
		int iCols = (int) (getWidth()/deltaX)+1;
		int iRows = (int) (getHeight()/deltaY)+1;
		Coord ret [][] = new Coord[iCols][iRows];
		int ix,iy;
		ix = iy =0;
		//		for(double xTemp = ll.x();xTemp <=ur.x();xTemp+=deltaX ){
		//			for(double yTemp = ll.y();yTemp <=ur.y();yTemp+=deltaY ){
		//				ret[ix][iy] = new Coord(xTemp, yTemp, 0);
		//				ix++;
		//			}
		//			iy++;
		//			ix=0;
		//		}
		double yTemp = ll.y();
		double xTemp = ll.x();
		for(ix =0 ;ix<iRows;ix++){
			for(iy =0;iy<iCols;iy++){
				ret[ix][iy] = new Coord(xTemp, yTemp, 0);
				yTemp+=deltaY;
			}
			yTemp = ll.y();
			xTemp += deltaX;
		}


		return ret;
	}

	public boolean contains2d(Coord c){
		return c.x()> ll.x() && c.x()<ur.x() && c.y()> ll.y() && c.y()<ur.y();
	}

	public boolean containsOrTouch2d(Coord c){
		return contains2d(c) || touch2d(c);
	}

	public boolean touch2d(Coord c){
		boolean bTouchLeft  = c.x() == ll.x() && c.y()>= ll.y() && c.y()<=ur.y();
		boolean bTouchRight = c.x() == ur.x() && c.y()>= ll.y() && c.y()<=ur.y();
		boolean bTouchUpper = c.y() == ll.y() && c.x()>= ll.x() && c.x()<=ur.x();
		boolean bTouchBotom = c.y() == ur.y() && c.x()>= ll.x() && c.x()<=ur.x();
		return bTouchLeft || bTouchRight || bTouchUpper || bTouchBotom;
	}

	public void grow2d(Coord c){
		if(!containsOrTouch2d(c)){
			if(c.x()<=ll.x()){
				ll.x(c.x());
				ul.x(c.x());
			}
			else if(c.x()>=ur.x()){
				ur.x(c.x());
				lr.x(c.x());
			}
			if(c.y()<=ll.y()){
				ll.y(c.y());
				lr.y(c.y());
			}
			else if(c.y()>=ur.y()){
				ur.y(c.y());
				ul.y(c.y());
			}
		}
	}
	
	public void grow2d(float timesSize){
		double width = getWidth();
		double height = getHeight();
	
		ll.x(ll.x()-width*timesSize);
		ul.x(ul.x()-width*timesSize);
				
		ur.x(ur.x()+width*timesSize);
		lr.x(lr.x()+width*timesSize);
		
		ll.y(ll.y()-height*timesSize);
		lr.y(lr.y()-height*timesSize);
		ur.y(ur.y()+height*timesSize);
		ul.y(ul.y()+height*timesSize);
	}

	public void grow3d(Coord c){
		grow2d(c);
		if(this.minz == DEFAULTNOZVALUE){
			this.minz = c.z();
		}
		if(this.maxz == DEFAULTNOZVALUE){
			this.maxz = c.z();
		}
		if(this.minz>c.z()){
			this.minz = c.z();
		}
		if(this.maxz<c.z()){
			this.maxz = c.z();
		}
	}

	public boolean is3d(){
		return this.minz != DEFAULTNOZVALUE && this.maxz != DEFAULTNOZVALUE;
	}

	public double getMaxZ(){
		if(!is3d()){
			throw new RuntimeException();
		}
		return maxz;
	}

	public double getMinZ(){
		if(!is3d()){
			throw new RuntimeException();
		}
		return minz;
	}

	public double getElevation(){
		return getMaxZ()-getMinZ();
	}


	public String getAsString(){
		return "width="+getWidth()+", height="+getHeight()+", ll="+ll.toString()+", ur="+ur.toString();
	}

	/**
	 * 
	 * @param roundFactor 
	 */
	public void growAndRound2d(double roundFactor){
		double minx = ll.x();
		if(minx%roundFactor!=0){
			minx = Math.floor(minx/roundFactor)*roundFactor;
		}
		double maxx = ur.x();
		if(maxx%roundFactor!=0){
			maxx = Math.floor(maxx/roundFactor)*roundFactor;
			maxx+=roundFactor;
		}
		double miny = ll.y();
		if(miny%roundFactor!=0){
			miny = Math.floor(miny/roundFactor)*roundFactor;
		}
		double maxy = ur.y();
		if(maxy%roundFactor!=0){
			maxy = Math.floor(maxy/roundFactor)*roundFactor;
			maxy+=roundFactor;
		}
		grow2d(new Coord(minx, miny, 0));
		grow2d(new Coord(maxx, maxy, 0));
	}

	public double getMinX(){
		return ll.x();
	}

	public double getMinY(){
		return ll.y();
	}

	public double getMaxX(){
		return ur.x();
	}

	public double getMaxY(){
		return ur.y();
	}

	public Coord getCenter2d(){
		return new Coord(getMinX()+getWidth()/2,getMinY()+getHeight()/2,0);
	}

	public void grow2d(BBox bBox) {
		grow2d(bBox.ll);
		grow2d(bBox.lr);
		grow2d(bBox.ul);
		grow2d(bBox.ur);
	}
	public BBox clone(){
		return new BBox(getMinX(), getMinY(), getMaxX(), getMaxY());
	}


}
