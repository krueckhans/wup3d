package de.wuppertal;

import javax.vecmath.Point3f;
import com.infomatiq.jsi.Rectangle;
import com.jme3.math.Vector3f;

public class Coord {
	private double x;
	private double y;
	private double z;
	
	public Coord(double x,double y,double z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Coord(String x,String y,String z){
		this.x = Double.parseDouble(x);
		this.y = Double.parseDouble(y);
		this.z = Double.parseDouble(z);
	}
	
	public Coord(Coord c) {
		this.x = c.x;
		this.y = c.y;
		this.z = c.z;
	}

	public double x(){
		return x;
	}
	
	public double y(){
		return y;
	}
	
	public double z(){
		return z;
	}
	
	
	public void x(double x){
		this.x = x;
	}
	
	
	public void y(double y){
		this.y = y;
	}
	
	
	public void z(double z){
		this.z = z;
	}
	
	public double getDist3d(Coord c){
		return Math.sqrt(Math.pow(c.x-x, 2) + Math.pow(c.y-y, 2) + Math.pow(c.z-z, 2));
	}
	
	public double getDist2d(Coord c){
		return Math.sqrt(Math.pow(c.x-x, 2) + Math.pow(c.y-y, 2));
	}
	
	public String toString(){
		return "["+x+";"+y+";"+z+"]";
	}
	
	public void minus(Coord c){
		x-=c.x;
		y-=c.y;
		z-=c.z;
	}
	
	public void plus(Coord c){
		x+=c.x;
		y+=c.y;
		z+=c.z;
	}
	
	public void minus(double _x,double _y,double _z){
		x-=_x;
		y-=_y;
		z-=_z;
	}
	
	public void plus(double _x,double _y,double _z){
		x+=_x;
		y+=_y;
		z+=_z;
	}
	
	public void minus(double _x,double _y){
		minus(_x,_y,0);
	}
	
	public void plus(double _x,double _y){
		plus(_x,_y,0);
	}
	
	public Coord plusNew(double _x,double _y){
		return new Coord(x+_x, y+_y, 0);
	}
	
	public Rectangle getAsRectangle(){
		return new Rectangle((float) x, (float) y, (float) x, (float) y);
	}

	public Coord round() {
		return new Coord(Math.round(x()),Math.round( y()), Math.round(z()));
	}

	public Vector3f getVec3f() {
		return new Vector3f((float)x,(float) y,(float) z);
	}

	public Point3f getPoint3f() {
		return new Point3f((float)x,(float) y,(float) z);
	}
	
	
	
}
