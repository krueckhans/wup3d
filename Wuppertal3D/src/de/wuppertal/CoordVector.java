package de.wuppertal;

import java.awt.Polygon;
import java.util.Iterator;
import java.util.Vector;

import javax.vecmath.Point3f;

public class CoordVector implements Iterable<Coord>{
	private Vector<Coord> coords;
	private BBox box;
	public CoordVector(){
		coords = new Vector<Coord>();
		box = null;
	}
	
	public void add2d(Coord c){
		if(box == null){
			box = new BBox(c, c);
		}
		box.grow2d(c);
		coords.add(c);
	}
	
	public int getIndexOf(Coord c){
		return coords.indexOf(c);
	}
	
	/**
	 * WARNING No update of BBox!!!
	 * @param c
	 */
	public void add(Coord c){
		coords.add(c);
	}
	
	public void addFirst(Coord c){
		coords.add(0, c);
	}
	
	public BBox getBBox(){
		return box;
	}
	
	public Vector<Coord> getCoords(){
		return coords;
	}
	
	 public int size(){
		 return coords.size();
	 }
	 
	 public Coord getCoord(int index){
		 return coords.get(index);
	 }
	 
	 public Coord getLast(){
		 return coords.get(size()-1);
	 }
	 
	
	public void updateBBox2d(){
		box = new BBox(coords.firstElement(), coords.firstElement());
		for(Coord c: coords){
			box.grow2d(c);
		}
	}
	
	public void updateBBox3d(){
		box = new BBox(coords.firstElement(), coords.firstElement());
		for(Coord c: coords){
			box.grow3d(c);
		}
	}
	
	public Coord remove(int i){
		Coord c = coords.remove(i);
		updateBBox2d();
		return c;
	}
	
	public Coord reduce2Min(){
		double minx = box.getMinX();
		double miny = box.getMinY();
		for(Coord c: coords){
			c.minus(minx,miny);
		}
		updateBBox2d();
		return new Coord(minx, miny, 0);
	}
	
	public Coord reduce2Min3D(){
		double minx = box.getMinX();
		double miny = box.getMinY();
		double minz = box.getMinZ();
		for(Coord c: coords){
			c.minus(minx,miny);
		}
		updateBBox3d();
		return new Coord(minx, miny, minz);
	}
	

	
	public Coord reduce2Min_3Donly(){
		double minz = box.getMinZ();
		for(Coord c: coords){
			c.minus(0, 0, minz);
		}
		updateBBox3d();
		return new Coord(box.getMinX(), box.getMinY(), minz);
	}
	
	@Override
	public Iterator<Coord> iterator() {
		return coords.iterator();
	}
	
	
	public void plusAll(double x,double y,double z,boolean updateBBox2d,boolean updateBBox3d){
		for(Coord c: coords){
			c.plus(x, y, z);
		}
		if(updateBBox3d){
			updateBBox3d();
		}
		else if(updateBBox2d){
			updateBBox2d();
		}
	}
	
	
	
	/**
	 * 
	 * @param outerBorder Punkte außerhalb werden ignoriert, Punkte innerhalb werden weiter verwendet
	 * @param innerBorder Punkte innerhalb der inner Border wird zOffset Höhenwert abgezogen
	 * @param zOffset
	 * @return
	 */
	public CoordVector getFiltered(Polygon outerBorder, Polygon innerBorder, double zOffset){
		CoordVector ret = new CoordVector();
		int iCoordsDone =0;
		for(Coord c: coords){
			if(outerBorder.contains(c.x(), c.y())){
				if(innerBorder.contains(c.x(), c.y())){
					ret.add(new Coord(c.x(), c.y(), c.z()-zOffset));
				}
				else{
					ret.add(new Coord(c.x(), c.y(), c.z()));
				}
				
			}
			iCoordsDone++;
			if(iCoordsDone%1000==0){
				System.out.println(iCoordsDone+"/"+coords.size());
			}
		}
		return ret;
		
	}

	public CoordVector getReversed() {
		CoordVector vec = new CoordVector();
		for(int i= coords.size()-1;i>=0;i--){
			vec.add(coords.get(i));
		}
		return vec;
	}
	
	public Point3f[] getPoints(){
//		boolean doIgnoreLast
//		if(getCoord(0).getDist3d(getLast())<0.001){
//			
//		}
		Point3f []points = new Point3f[size()];
		int i=0;
		for(Coord c :getCoords()){
			points[i] = c.getPoint3f();
			
			i++;
		}
		
		return points;
	}
	
	public CoordVector cloneFull(){
		CoordVector ret = new CoordVector();
		for(Coord c: getCoords()){
			ret.add(new Coord(c));
		}
		return ret;
	}
	
	public CoordVector clone(){
		CoordVector ret = new CoordVector();
		ret.coords = (Vector<Coord>) coords.clone();
		return ret;
	}

	public void add(Point3f point3f) {
		add(new Coord(point3f.x, point3f.y, point3f.z));
		
	}
	
}
