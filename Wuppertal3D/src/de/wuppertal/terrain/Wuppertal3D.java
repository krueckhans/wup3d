package de.wuppertal.terrain;

import java.io.File;
import java.util.Stack;
import java.util.Vector;

import com.jme3.asset.DesktopAssetManager;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Node;

import de.wuppertal.BBox;
import de.wuppertal.Coord;
import de.wuppertal.abstractAndInterfaces.GeometryLoadListener;
import de.wuppertal.abstractAndInterfaces.TextureLoadListener;
import de.wuppertal.terrain.GeometryLoadJob.GEOMETRYJOBTYPE;
public class Wuppertal3D {
	/**********************************************************************************************************************************/
	/*******************************************************SETTINGS*******************************************************************/
	/**********************************************************************************************************************************/
	public static final BBox bboxFullTerrain = new BBox(361395.30000000075,5669917.94,382245.30000000075,5686617.94);

	private final static int iNumberOfGeometryLoadingThreads = 2;
	private final static int iNumberOfTextureLoadingThreads = 2;
	private final static long lControlThreadSleepTime= 200;//250 => max 4 runs per second.
	private final static long lTexturesThreadSleepTime= 250;//100 => max 10 runs per second.
	private final static long lGeometryThreadSleepTime= 250;//100 => max 10 runs per second.
	private final static double camMoveDistanceThresholds[] = new double[]{30,100};//<[0] => Standing  ;  <[1] => Moving ; else Beaming

	/**settings for region terrain creationMode**/
	private final static String wmsExampleQueryRegion ="http://www.wms.nrw.de/geobasis/wms_nw_dop40?REQUEST=GetCapabilities&SERVICE=wms&VERSION=1.3.0?&VERSION=1.1.1&REQUEST=GetMap&BBOX=374327.,5681000,375327.,5682000&WIDTH=512&HEIGHT=512&SRS=EPSG:25832&FORMAT=image/png&LAYERS=WMS_NW_DOP40";
	private final static int iTextureResolutionRegion = 2048;
	private final static int iDGMResolutionRegion = 128;
	private final static File srcFolderGeometryRegion =  new File ("C:\\Temp\\ddddaten\\rohdaten");
	/**settings for region terrain liveMode and creationMode**/
	//	private final static File dstFolderRegion =  new File ("C:\\Temp\\ddddaten\\terrain_region");
	private final static File dstFolderRegion =  new File ("\\\\s102x003\\Wuppertal3D$\\terrain_region");
	private final static float yoffsetRegion = -10;

	/**settings for static terrain creationMode**/
	private final static String wmsExampleQueryStatic ="http://s102w484:8399/arcgis/services/WuNDa-Orthophoto-WUP/MapServer/WMSServer?&VERSION=1.1.1&REQUEST=GetMap&BBOX=374327.,5681000,375327.,5682000&WIDTH=4096&HEIGHT=4096&SRS=EPSG:25832&FORMAT=image/png&LAYERS=7&STYLES=default";
	private final static int iTextureResolutionStatic = 4096;
	private final static int iDGMResolutionStatic = 512;
	private final static File srcFolderGeometryStatic =  new File ("C:\\Temp\\ddddaten\\rohdaten");
	/**settings for static terrain liveMode and creationMode**/
	//	private final static File dstFolderStatic =  new File ("C:\\Temp\\ddddaten\\terrain_static");
	private final static File dstFolderStatic =  new File ("\\\\s102x003\\Wuppertal3D$\\terrain_static");
	private final static float yoffsetStatic = -10;

	/**settings for patched terrain creationMode**/
	private final static String wmsExampleQueryPatched ="http://s102w484:8399/arcgis/services/WuNDa-Orthophoto-WUP/MapServer/WMSServer?&VERSION=1.1.1&REQUEST=GetMap&BBOX=374327.,5681000,375327.,5682000&WIDTH=4096&HEIGHT=4096&SRS=EPSG:25832&FORMAT=image/png&LAYERS=7&STYLES=default";
	private final static int iPatchSizePatched = 200;
	private final static File srcFolderGeometryPatched =  new File ("C:\\Temp\\ddddaten\\rohdaten");
	/**settings for patched terrain liveMode and creationMode**/
	private final static int iTextureResolutionLow = 128;//,128};
	private final static int iTextureResolutionHigh = 2048;//,128};
	//	private final static File dstFolderPatched =  new File ("C:\\Temp\\ddddaten\\terrain_patched");
	private final static File dstFolderPatched =  new File ("\\\\s102x003\\Wuppertal3D$\\terrain_patched");
	private final static Coord offsetPatchedterrain = new Coord(361400.0, 5686600, 0);
	private final static float yoffsetPatched= 0;

	/**settings for buildings creationMode**/
	private final static File srcFolderGeometryBuildings =  new File ("C:\\Temp\\ddddaten\\rohdaten\\citygml");
	/**settings for buildings  liveMode and creationMode**/
	private final static int iPatchSizeBuildings = 1000;
	private final static File dstFolderGeometryBuildings =  new File ("\\\\s102x003\\Wuppertal3D$\\buildings");
	private final static Coord offsetPatchedBuildings= new Coord(361395.0, 5686617.94, 0);


	public final float MIN_TERRAIN_HEIGHT = 69.25f;//75.0125f; 
	public final float MAX_TERRAIN_HEIGHT = 351.5f;//349.775f;

	/**********************************************************************************************************************************/
	/**********************************************************************************************************************************/
	/**********************************************************************************************************************************/

	public enum CAMERAMOVEMENTSTATUS{OFF,STANDING,MOVING,BEAMING};
	public enum LOADPRIORITY{HIGH,MEDIUM,LOW};// High-> stack.addFirst, medium -> stack.add(stack.size()/2), low -> stack.addLast 


	private boolean bThreadControllerRunning;
	private boolean bTextureLoadingThreadsRunning;
	private boolean bGeometryLoadingThreadsRunning;


	private Thread threadPatchController;
	private Thread[] threadsTextureLoader;
	private Thread[] threadsGeometryLoader;

	private Stack<GeometryLoadJob> geometryLoadJobs;
	private Stack<TextureLoadJob> textureLoadJobs;

	//Gekacheltes terrain (high resolution)
	private DynamicPatchedTerrain patchedTerrain;

	//Statisches gelände (medium resolution)
	private StaticTerrain staticTerrain;

	//Umgebungs Terrain (low resoultion) 
	private StaticRegionTerrain regionTerrain;

	//Gebäudedarstellungen
	private DynamicPatchedBuildings patchedBuildings;

	private DesktopAssetManager am;

	private Node rootNode;


	private BitmapText statusTextDynamic;
	private BitmapText statusTextStatic;
	float timeSinceLastTextUpdate ;

	public Wuppertal3D(DesktopAssetManager assetManager,  Camera cam, Node rootNode, BitmapText statusTextStatic, BitmapText statusTextDynamic, boolean bCreationMode){
		this.am = assetManager;
		this.rootNode = rootNode;
		this.statusTextDynamic = statusTextDynamic;
		this.statusTextStatic = statusTextStatic;
		timeSinceLastTextUpdate=0;
		//register locator for region and static terrain
		assetManager.registerLocator(dstFolderRegion.getAbsolutePath(), FileLocator.class);
		assetManager.registerLocator(dstFolderStatic.getAbsolutePath(), FileLocator.class);
		assetManager.registerLocator(dstFolderPatched.getAbsolutePath(), FileLocator.class);
		assetManager.registerLocator(dstFolderGeometryBuildings.getAbsolutePath(), FileLocator.class);

		startGeometryLoadingThreads();
		startTextureLoadingThreads();

		//create j3o from height Data
		if(bCreationMode){
			regionTerrain = new StaticRegionTerrain(yoffsetRegion,iDGMResolutionRegion, srcFolderGeometryRegion, dstFolderRegion, iTextureResolutionRegion, wmsExampleQueryRegion,am,cam, this);
			staticTerrain = new StaticTerrain(yoffsetStatic,iDGMResolutionStatic, srcFolderGeometryStatic, dstFolderStatic, iTextureResolutionStatic, wmsExampleQueryStatic, am, cam, this);
			patchedTerrain = new DynamicPatchedTerrain(yoffsetPatched,offsetPatchedterrain, srcFolderGeometryPatched, dstFolderPatched, iPatchSizePatched,wmsExampleQueryPatched, iTextureResolutionLow, iTextureResolutionHigh,assetManager,cam,this);
			patchedBuildings = new DynamicPatchedBuildings(srcFolderGeometryBuildings,dstFolderGeometryBuildings,offsetPatchedBuildings, iPatchSizeBuildings,this,false,true);
		}
		//use existing j3o Files
		else{
			regionTerrain = new StaticRegionTerrain(yoffsetRegion,am,cam);
			staticTerrain = new StaticTerrain(yoffsetStatic,am,cam);
			patchedTerrain = new DynamicPatchedTerrain(yoffsetPatched,offsetPatchedterrain, this, iTextureResolutionLow, iTextureResolutionHigh,iPatchSizePatched);
			patchedBuildings = new DynamicPatchedBuildings(srcFolderGeometryBuildings,dstFolderGeometryBuildings,offsetPatchedBuildings, iPatchSizeBuildings,this,false,true);
		}
		staticTerrain.getStaticTerrainNode().setShadowMode(ShadowMode.Off);
		rootNode.attachChild(staticTerrain.getStaticTerrainNode());
		regionTerrain.getRegionTerrainNode().setShadowMode(ShadowMode.Off);
		rootNode.attachChild(regionTerrain.getRegionTerrainNode());
		patchedTerrain.setShadowMode(ShadowMode.Receive);
		rootNode.attachChild(patchedTerrain);
		patchedBuildings.setShadowMode(ShadowMode.CastAndReceive);
		rootNode.attachChild(patchedBuildings);
		startPatchController(cam);
	}

	private void startPatchController(final Camera cam) {
		bThreadControllerRunning =true;
		threadPatchController = new Thread(){
			public void run(){
				try{
					setName("CameraControlThread");
					int iPositionsCache =3;
					Vector3f [] lastPositions = new Vector3f[iPositionsCache];
					Vector3f currentCamPosition;
					for(int i=0;i<lastPositions.length;i++){
						lastPositions[i]=new Vector3f(0,0,0);
					}
					int i=0;
					CAMERAMOVEMENTSTATUS camStatus = CAMERAMOVEMENTSTATUS.OFF;
					while(bThreadControllerRunning){
						lastPositions[i] =  cam.getLocation().clone();
						currentCamPosition = lastPositions[i];
						i++;
						if(i == iPositionsCache){
							i=0;
						}
						double distSum = getSumDistance(lastPositions);
						//Case: Standing
						if(distSum<=camMoveDistanceThresholds[0]){
							camStatus = CAMERAMOVEMENTSTATUS.STANDING;
						}
						//Case: Moving
						else if(distSum<=camMoveDistanceThresholds[1]){
							camStatus = CAMERAMOVEMENTSTATUS.MOVING;
						}
						//Case Beaming
						else{
							camStatus = CAMERAMOVEMENTSTATUS.BEAMING;
						}
						patchedTerrain.updateControl(camStatus, currentCamPosition);
						patchedBuildings.updateControl(camStatus, currentCamPosition);
						sleep(lControlThreadSleepTime);
						System.gc();
						am.clearCache();
						System.out.print(camStatus+" GeoJobs = "+geometryLoadJobs.size()+" TexJobs = "+textureLoadJobs.size()+"  ");
					}
				}
				catch(Exception e){
					System.err.println("FATAL ERROR: Controler Thread killed");
					e.printStackTrace();
				}
			}
			public double getSumDistance(Vector3f[] positions){
				double dist=0;
				for(int i=1;i<positions.length;i++){
					dist += positions[i-1].distance(positions[i]);
				}
				return dist;
			}
		};
		threadPatchController.start();
	}


	public void startGeometryLoadingThreads(){
		geometryLoadJobs = new Stack<GeometryLoadJob>();
		bGeometryLoadingThreadsRunning = true;
		final int iJobsPerThread =10;
		threadsGeometryLoader = new Thread[iNumberOfGeometryLoadingThreads];
		for(int iThread =0; iThread<iNumberOfGeometryLoadingThreads;iThread++){
			final int iThreadId = iThread;
			threadsGeometryLoader[iThread] = new Thread(){
				public void run(){
					try{
						setName("GeometryLoadingThread_"+iThreadId+"_of_"+iNumberOfGeometryLoadingThreads);
						Vector<GeometryLoadJob> jobs = new Vector<GeometryLoadJob>();
						while(bGeometryLoadingThreadsRunning){
							synchronized (geometryLoadJobs) {
								while(geometryLoadJobs.size()>0 && jobs.size()<iJobsPerThread){
									jobs.add(geometryLoadJobs.pop());
								}
							}
							if(jobs.size()==0){
								sleep(lGeometryThreadSleepTime);
							}
							else{
								for(GeometryLoadJob job: jobs){
									job.doJob(am);
								}
								jobs.clear();
							}
						}
					}
					catch(Exception e){
						System.err.println("FATAL ERROR: GeometryLoadingThread killed");
						e.printStackTrace();
					}
				}

			};
			threadsGeometryLoader[iThread].start();
		}
	}

	public void startTextureLoadingThreads(){
		textureLoadJobs = new Stack<TextureLoadJob>();
		final int iJobsPerThread = 4;
		bTextureLoadingThreadsRunning =true;
		threadsTextureLoader = new Thread[iNumberOfTextureLoadingThreads];
		for(int iThread =0; iThread<iNumberOfTextureLoadingThreads;iThread++){
			final int iThreadId = iThread;
			threadsTextureLoader[iThread] = new Thread(){
				public void run(){
					try{
						setName("TextureLoadingThread_"+iThreadId+"_of_"+iNumberOfTextureLoadingThreads);
						Vector<TextureLoadJob> jobs = new Vector<TextureLoadJob>();
						while(bTextureLoadingThreadsRunning){
							synchronized (textureLoadJobs) {
								while(textureLoadJobs.size()>0 && jobs.size()<iJobsPerThread){
									jobs.add(textureLoadJobs.pop());
								}
							}
							if(jobs.size()==0){
								sleep(lTexturesThreadSleepTime);
							}
							else{
								for(TextureLoadJob job : jobs){
									job.doJob(am);
								}
								jobs.clear();
							}
						}
					}
					catch(Exception e){
						System.err.println("FATAL ERROR: TextureLoadingThread killed");
						e.printStackTrace();
					}
				}
			};
			threadsTextureLoader[iThread].start();
		}
	}

	public void addGeometry2BeLoaded(Integer id, GeometryLoadListener listener, LOADPRIORITY prio,GEOMETRYJOBTYPE type){
		synchronized (geometryLoadJobs) {
			GeometryLoadJob job = new GeometryLoadJob(id,prio,listener,type);
			//			if(!geometryLoadJobs.contains(job)){
			int size = geometryLoadJobs.size();
			switch (prio){
			case LOW:{
				geometryLoadJobs.add(size,job);
				break;
			}
			case MEDIUM:{
				geometryLoadJobs.add(size/2,job);
				break;
			}
			case HIGH:{
				geometryLoadJobs.add(job);
				break;
			}
			}
			//			}
		}
	}

	public void addTexture2BeLoaded(Integer id,int resolution, TextureLoadListener listener, LOADPRIORITY prio){
		synchronized (textureLoadJobs) {
			TextureLoadJob job = new TextureLoadJob(id,resolution,prio,listener);
			//			if(!textureLoadJobs.contains(job)){
			int size = textureLoadJobs.size();
			switch (prio){
			case LOW:{
				textureLoadJobs.add(size,job);
				break;
			}
			case MEDIUM:{
				textureLoadJobs.add(size/2,job);
				break;
			}
			case HIGH:{
				textureLoadJobs.add(job);
				break;
			}
			}
			//			}
		}
	}

	public void killAllThreads(){
		bThreadControllerRunning = false;
		bTextureLoadingThreadsRunning = false;
		bGeometryLoadingThreadsRunning = false;
	}




	//		private BBox position2RoundedBBox( Vector3d position){
	//			double factorx = Math.floor(position.x/200);
	//			double  x0 =  factorx*200;
	//			double  x1 =  (factorx+1)*200;
	//
	//			double factory = Math.floor(position.z/200);
	//			double  y0 =  factory*200;
	//			double  y1 =  (factory+1)*200;
	//			return new BBox(x0, y0, x1, y1);
	//		}

	//		private String getIdByCamPosition(Vector3f currentCamPosition){
	//			float xGlobal = currentCamPosition.x+vecGlobalOffset.x;
	//			float zGlobal = vecGlobalOffset.z-currentCamPosition.z;
	//			xGlobal-=300000;//
	//			int kmx = (int)FastMath.floor(xGlobal/1000);
	//			zGlobal-=5600000;
	//			int kmz = (int)FastMath.floor(zGlobal/1000);
	//			float restx = xGlobal%(kmx*1000);
	//			float restz = zGlobal%(kmz*1000);
	//			int irx =  (int)FastMath.floor(restx/200)+1;
	//			int irz =  (int)FastMath.floor(restz/200);
	//			int iPart = irx+irz*5;
	//			if(iPart<10){
	//				return kmx+""+kmz+"0"+iPart;
	//			}
	//			else{
	//				return kmx+""+kmz+""+iPart;
	//			}
	//
	//		}
	//
	//		public Vector3d getGlobalPositionByCam(Vector3f currentCamPosition,boolean bRemoveUTMZone){
	//			float xGlobal = currentCamPosition.x+vecGlobalOffset.x;
	//			if(bRemoveUTMZone&& xGlobal>999999){
	//				xGlobal-=32000000;
	//			}
	//			float zGlobal = vecGlobalOffset.z-currentCamPosition.z;
	//			Vector3d vecGlobal = new Vector3d(xGlobal, currentCamPosition.y, zGlobal);
	//			return vecGlobal;
	//
	//		}


	//		private boolean shouldCheckCamPosition(){
	//			if(currentCamPosition.distance(cam.getLocation())>cameraMoveDistThres){
	//				return true;
	//			}
	//			return false;
	//		}
	//

	public void update(float tpf){
		patchedTerrain.update(tpf);
		patchedBuildings.update(tpf);
		timeSinceLastTextUpdate+=tpf;
		if(timeSinceLastTextUpdate>0.3){
			int iSizeTexTiles = textureLoadJobs.size();
			int iSizeGeoTiles = geometryLoadJobs.size();
			if(iSizeTexTiles>0 && iSizeGeoTiles>0){
				statusTextDynamic.setColor(ColorRGBA.Red);
				statusTextStatic.setColor(ColorRGBA.Red);
			}
			else{
				statusTextDynamic.setColor(ColorRGBA.Black);
				statusTextStatic.setColor(ColorRGBA.Black);
			}
			statusTextDynamic.setText("                                                            "+String.valueOf(iSizeGeoTiles)+"\n"+"                                                            "+String.valueOf(iSizeTexTiles));
			timeSinceLastTextUpdate =0;

		}
	}


	//		private Texture getTexturePart() {
	//			double factorx0 = (boxReal.getMinX()-root.boxFullTerrain.getMinX())/root.boxFullTerrain.getWidth();
	//			double factorx1 = (boxReal.getMaxX()-root.boxFullTerrain.getMinX())/root.boxFullTerrain.getWidth();
	//
	//			double factory0 = (root.boxFullTerrain.getMaxY()-boxReal.getMaxY())/root.boxFullTerrain.getHeight();
	//			double factory1 = (root.boxFullTerrain.getMaxY()-boxReal.getMinY())/root.boxFullTerrain.getHeight();
	//
	//			int x0 = (int) (factorx0 * ((float)root.buffFullTerrain.getWidth()));
	//			int y0 = (int) (factory0 * ((float)root.buffFullTerrain.getHeight()));
	//
	//			int x1 = (int) (factorx1 * ((float)root.buffFullTerrain.getWidth()));
	//			int y1 = (int) (factory1 * ((float)root.buffFullTerrain.getHeight()));
	//
	//			System.out.println(factorx0+"  "+factory0);
	//			BufferedImage buffImagePart = root.buffFullTerrain.getSubimage(x0, y0, x1-x0, y1-y0);
	//
	//			buffImagePart = copyImage(buffImagePart);
	//			Texture t = new Texture2D(new AWTLoader().load(buffImagePart, true));
	//			return t;
	//		}
	//	

	public void switchVisibility(String name){
		Node n = (Node) rootNode.getChild(name);
		if(n!=null){
			Vector3f vec = n.getLocalTranslation();
			if(vec.y>=-50000){
				n.setLocalTranslation(vec.x, vec.y-100000, vec.z);
			}
			else{
				n.setLocalTranslation(vec.x, vec.y+100000, vec.z);
			}
		}
	}

	public void switchWireFrameStaticTerrain(){
		staticTerrain.switchWireFrame();
	}


	public void switchWireFrameRegionTerrain(){
		regionTerrain.switchWireFrame();
	}


	public BBox getBBoxFullTerrain(){
		return bboxFullTerrain;
	}










}
