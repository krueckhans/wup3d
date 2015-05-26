package de.wuppertal.tools.textureCreation;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Stack;

import de.wuppertal.BBox;

public class WMSImageCatchThread extends Thread{
	private long lSleepTime;
	private boolean isSleeping;
	private boolean bAlive;
	private WMSImageCatcher catcher;
	private volatile Stack<WMSImageCatchJob> boxesToDo;
	private static volatile int iJobs2Do;
	private WMSImageCatchable receiver;

	public WMSImageCatchThread(WMSImageCatchable receiver, WMSImageCatcher catcher){
		lSleepTime = 300;
		bAlive = true;
		isSleeping = true;
		boxesToDo = new Stack<WMSImageCatchJob>();
		this.catcher = catcher;
		this.receiver = receiver;
		start();
	}

	public void run(){
		try{
			BBox box2Catch;
			while(bAlive){
				if(boxesToDo.isEmpty()){
					isSleeping = true;
					sleep(lSleepTime);
				}
				else{
					isSleeping = false;
					WMSImageCatchJob job =boxesToDo.pop(); 
					box2Catch = job.getBBox();
					catcher.setBBox(box2Catch);
					job.setImage(catcher.getImage(true));
					receiver.fireImageReciveEvent(job);
					iJobs2Do--;
				}
			}
		}
		catch(Exception e){
			System.err.println("");
			e.printStackTrace();
		}
	}

	public void kill(){
		bAlive = false;
	}

	public void addJob(BBox box2Catch, String id){
		boxesToDo.add(new WMSImageCatchJob(box2Catch, id));
	}


	public boolean isSleeping(){
		return isSleeping;
	}

	
	public static void createThreadsAndAddJobsMultiThreaded(String strWmsExampleURL, HashMap<String, BBox> mapBoxesById, int iImageSize, int iThreads, WMSImageCatchable receiver){
		WMSImageCatcher[] catchers = WMSImageCatcher.getInstances(iThreads, strWmsExampleURL);
		WMSImageCatchThread[] threads = new WMSImageCatchThread[iThreads];
		int i=0;
		for(WMSImageCatcher catcher:catchers){
			catcher.setImageHeight(iImageSize);
			catcher.setImageWidth(iImageSize);
			threads[i] = new WMSImageCatchThread(receiver, catcher);
			i++;
		}
		addJobsMuliThreaded(mapBoxesById, threads);
	}

	private static void addJobsMuliThreaded(HashMap<String, BBox>  mapBoxesById,WMSImageCatchThread[] threads){
		int iThreads = threads.length;
		int iBoxCounter = 0;
		iJobs2Do = mapBoxesById.size();
		for(Entry<String, BBox> entry: mapBoxesById.entrySet()){
			BBox box =entry.getValue();
			String id = entry.getKey();
			int index = iBoxCounter%iThreads;
			threads[index].addJob(box,id);
			iBoxCounter++;
		}
		//Wait for jobs
		while(iJobs2Do>0){
			try{
				System.out.println("Image2Do = "+iJobs2Do);
				Thread.sleep(5000);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	


}
