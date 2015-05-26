package de.wuppertal.tools.other;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Vector;
import javax.imageio.ImageIO;

public class ConvertJPG2PNG {
	
	
	public static void main(String[] args) {
		final File folder = new File("C:\\Temp\\ddddaten\\terrain_patched");
//		final File folder = new File("\\\\s102x003\\Wuppertal3D$\\terrain_patched");
		int iThreads =8;
		
		final Vector<File>[] filesPerThread = new Vector[iThreads];
		
		int iCounter=0;
		for(File f: folder.listFiles()){
			if(f.getName().endsWith(".png")){
				if(filesPerThread[iCounter%iThreads]==null){
					filesPerThread[iCounter%iThreads] = new Vector<File>();
				}
				filesPerThread[iCounter%iThreads].add(f);
				iCounter++;
			}
		}
		
		Thread[] threads = new Thread[iThreads];
		for(int i=0; i<iThreads;i++){
			final int j =i;
			threads[i] = new Thread(){
				public void run(){
					try{
						int iFiles = 0;
						for(File f : filesPerThread[j]){
							BufferedImage buff = ImageIO.read(f);
							ImageIO.write(buff, "JPG", new File(folder,f.getName().substring(0, f.getName().lastIndexOf("."))+".jpg"));
							System.out.println("Thread "+ j+": "+iFiles+" of "+filesPerThread[j].size());
							iFiles++;
						}
					}
					catch(Exception e){
						e.printStackTrace();
					}
				}
			};
			threads[i].start();
		}
		
		
		
		
		
	}
	
}
