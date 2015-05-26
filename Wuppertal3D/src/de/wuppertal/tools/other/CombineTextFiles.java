package de.wuppertal.tools.other;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class CombineTextFiles {

	public static void main(String[] args) {
//		File folderSrc = new File("\\\\s102gs\\_102-hoehendaten\\DATEN\\LASERDATEN-PUNKTWOLKE\\2009\\BODENPUNKTE\\ASCII\\ETRS89");
//		File fileDest = new File("C:\\temp\\","ls_points2009.txt");
//		
		File folderSrc = new File("C:\\Temp\\ddddaten\\DGM5_land");
		File fileDest = new File("C:\\Temp\\ddddaten\\","dgm50_land.txt");
		
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(fileDest));
			int iLines=0;
			int iFile =0;
			String strFileName;
			for(File f:folderSrc.listFiles()){
				strFileName = f.getName();
				System.out.println("reading File "+strFileName+ " File "+iFile+++" of "+folderSrc.listFiles().length);
//				if(strFileName.contains("_LpB.txt") && !strFileName.equals("inhalt_LpB.txt")){
					BufferedReader br = new BufferedReader(new FileReader(f));
					String line =null;
					while((line= br.readLine()) !=null){
						bw.write(line);
						bw.newLine();
						if(iLines%10000==0){
							bw.flush();
						}
						iLines++;
					}
					br.close();
//				}
			}
			bw.close();
			System.out.println(iLines+" lines copied. ");
		}
		catch(Exception e){
			e.printStackTrace();
		}

	}

}
