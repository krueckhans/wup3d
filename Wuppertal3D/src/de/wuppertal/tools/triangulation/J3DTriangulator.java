package de.wuppertal.tools.triangulation;

import java.util.Vector;

import javax.vecmath.Point3f;

import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.Triangulator;

import de.wuppertal.CoordVector;
public class J3DTriangulator {
	
	
	public static Vector<CoordVector> triangulate(CoordVector polygon){
		Vector<CoordVector> vecTriangles = new Vector<CoordVector>();
		Triangulator tr = new Triangulator();
		GeometryInfo gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
		gi.setCoordinates(polygon.getPoints());
		int[] index = new int[polygon.size()];
		for(int i=0;i<polygon.size();i++){
			index[i] =i;
		}
		
	    gi.setContourCounts(new int[]{1});
	    gi.setStripCounts(new int[]{polygon.size()});
		gi.setCoordinateIndices(index);
		
		
		tr.triangulate(gi); // ginfo contains the geometry.
		gi.convertToIndexedTriangles();
		Point3f[] cordinates  = gi.getCoordinates();
		int[]  indis = gi.getCoordinateIndices();
		
		CoordVector vecTemp = new CoordVector();
		for(int i :indis){
			vecTemp.add(cordinates[i]);
			if(vecTemp.size()==3){
				vecTriangles.add(vecTemp.clone());
				vecTemp = new CoordVector();
			}
		}
		if(vecTemp.size()>0){
			System.out.println("LOST VERTICES DURING TRIANGULATION!!!!!");
			new RuntimeException();
		}
//		System.out.println(polygon.size()+"  => #triangles="+vecTriangles.size());
		
		return vecTriangles;
		 
	}
}
