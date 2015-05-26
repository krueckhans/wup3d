package de.wuppertal.tools.other;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

public class SplitGMLFileByFeature {
	public SplitGMLFileByFeature(File gmlFile,File folderSrc){
		try{

			BufferedReader br = new BufferedReader(new FileReader(gmlFile));
			String line;
//			BufferedWriter bw =null;
//			String id =null;
//			int i=0;
			HashMap<String, BufferedWriter> allWriters = new HashMap<String, BufferedWriter>();
			while((line = br.readLine())!=null) {
								System.out.println(line);
//				if(line.startsWith("<fme:_") ){
//					String id2 = line.substring(7, line.indexOf(" "));
//					//erster Start oder fortsetzen des aktuellen files
//					if(id==null || id2.equals(id)){
//						id = id2;
//						if(bw==null){
//							bw = new BufferedWriter(new FileWriter(new File(folderSrc,"_"+id+".gml")));
//							bw.write("<Tag xmlns:gml=\"http://www.opengis.net/gml\" xmlns:fme=\"http://www.opengis.net/fme\" >");
//							bw.newLine();
//							allWriters.put(id, bw);
//						}
//					}
//					//abschließen vorheriges File
//					//neues file starten
//					if(!id.equals(id2)){
//						id =id2;
//						bw = allWriters.get(id);
//						if(bw==null){
//							System.out.println(i++);
//							bw = new BufferedWriter(new FileWriter(new File(folderSrc,"_"+id+".gml")));
//							bw.write("<Tag xmlns:gml=\"http://www.opengis.net/gml\" xmlns:fme=\"http://www.opengis.net/fme\" >");
//							bw.newLine();
//							allWriters.put(id, bw);
//						}
//					}
//
//					//					bw.write("<xml version=\"1.0\" encoding=\"windows-1252\" standalone=\"yes\"?>");
////					bw.write(line);
//					while(!(line = br.readLine()).startsWith("</fme:_")) {
//						bw.write(line);
//						bw.newLine();
//					}
//					bw.flush();
//					//					bw.write(line);
//				}
				
			}
			br.close();
			for(BufferedWriter bw2 : allWriters.values()){
				bw2.write("</Tag>");
				bw2.close();
			}

		}
		catch(Exception e){
			e.printStackTrace();
		}
	}


	public static void main(String[] args) {
		new SplitGMLFileByFeature(new File("C:\\Temp\\ddddaten\\rohdaten\\DGM5.gml"),new File("C:\\Temp\\ddddaten\\rohdaten\\terrain_patched" ));

	}
}
