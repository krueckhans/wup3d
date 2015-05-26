package de.wuppertal.tools.terrainCreation;

import java.io.File;

import de.wuppertal.tools.gmlGeometryParser.GML_TIN2J3O;

public class TerrainCreator {

	public static void main(String[] args) {

//		TerrainCreatorHelper inst = TerrainCreatorHelper.getInstance();
		/*************************/
		/**Create terrain region**/
		/*************************/
		//		Polygon umringWTal = inst.readPolygon(new File("C:\\Temp\\ddddaten\\rohdaten\\umringwtal.txt"));
		//		Polygon bboxWTalregion = new Polygon();
		//		bboxWTalregion.addPoint(340545,5653218);//ll
		//		bboxWTalregion.addPoint(403096,5653218);
		//		bboxWTalregion.addPoint(403096,5703317);//ur
		//		bboxWTalregion.addPoint(340545,5703317);
		//
		//		CoordVector vecRasterDE = inst.readRasterPoints(new File("C:\\Temp\\ddddaten\\rohdaten\\dgm50_land.txt"));
		//		vecRasterDE.plusAll(-32000000, 0, 0, true, false);
		//
		//		System.out.println(vecRasterDE.size());
		//		CoordVector vecFiltered = vecRasterDE.getFiltered(bboxWTalregion, umringWTal, 100);
		//		System.out.println("Filtered = "+ vecFiltered.size());
		//		File srcSurround = new File("C:\\Temp\\ddddaten\\rohdaten\\tempHeightMap_terrain_region.txt");
		//		inst.writeCoordVector2File(vecFiltered, srcSurround);
		//
		//
		//		TerrainCreatorHelper.DEFAULTMINDELTA=5000;
		//
		//		int [] sizesDGMRegion = new int[]{128,256,512};
		//		for(int dest2NSize: sizesDGMRegion){
		//			File dstSurround = new File("C:\\Temp\\ddddaten\\rohdaten\\"+dest2NSize+"terrain_region.txt");
		//			inst.convertPointCloud2Raster(srcSurround, dstSurround, dest2NSize);
		//		}
		//		srcSurround.delete();



		/*************************/
		/**Create terrain static**/
		/*************************/
//		File src = new File("C:\\Temp\\ddddaten\\rohdaten\\DGM_10M.csv");
//		//		int [] sizesDGM = new int[]{512};
//		int [] sizesDGM = new int[]{512,1024,2048};
//		int [] distances2Border = new int[]{100,50,25};//wenn in einer Distanz von distanceBorder keinen Höhenwert gefunden wird, wird die Höhe auf -9999 gesetzt 
//		for(int i=0;i<sizesDGM.length;i++){
//			int dest2NSize = sizesDGM[i] ;
//			TerrainCreatorHelper.DEFAULTMINDELTA =distances2Border[i];
//			File dst = new File("C:\\Temp\\ddddaten\\rohdaten\\"+dest2NSize+"terrain_static.txt");
//			inst.convertPointCloud2Raster(src, dst, dest2NSize);
//		}
		
		
		
		/**************************/
		/**Create patched terrain**/
		/**************************/
		
		GML_TIN2J3O app = new GML_TIN2J3O(new File("C:\\Temp\\ddddaten\\rohdaten\\terrain_patched"));
		app.setOffsetx(0.03613953E7);
		app.setOffsety(5686617.94);
		app.setDestFile(new File("C:\\Temp\\ddddaten\\terrain_patched"));
		app.startParsingMultiThreaded(8);
	}

}
