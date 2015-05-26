package de.wuppertal.terrain;

import java.io.File;
import java.util.HashMap;
import java.util.Vector;

import javax.imageio.ImageIO;

import com.jme3.asset.AssetManager;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

import de.wuppertal.BBox;
import de.wuppertal.Coord;
import de.wuppertal.abstractAndInterfaces.AbstractPatchedNode;
import de.wuppertal.abstractAndInterfaces.AbstractSinglePatch;
import de.wuppertal.terrain.GeometryLoadJob.GEOMETRYJOBTYPE;
import de.wuppertal.terrain.Wuppertal3D.CAMERAMOVEMENTSTATUS;
import de.wuppertal.terrain.Wuppertal3D.LOADPRIORITY;
import de.wuppertal.tools.gmlGeometryParser.GML_TIN2J3O;
import de.wuppertal.tools.textureCreation.WMSImageCatchJob;
import de.wuppertal.tools.textureCreation.WMSImageCatchThread;
import de.wuppertal.tools.textureCreation.WMSImageCatchable;

public class DynamicPatchedTerrain extends AbstractPatchedNode{
	private static final int iNeighborhoodLow = 4;
	private static final int iNeighborhoodHigh = 1;
	public static final String sceneGraphID= "terrain_patched";
	private Vector2f[] positionsCacheHighRes;
	private int iTextureSizeLow;
	private int iTextureSizeHigh;
	public static boolean bWireframe = false;

	public DynamicPatchedTerrain(float yOffset,Coord offset,Wuppertal3D wup3d, int iTextureSizeLow , int iTextureSizeHigh,int iPatchSize){
		super(sceneGraphID,iPatchSize,wup3d, offset);
		this.iTextureSizeLow = iTextureSizeLow;
		this.iTextureSizeHigh = iTextureSizeHigh;
		this.fPatchSize = iPatchSize;
		this.setLocalTranslation(0, -wup3d.MIN_TERRAIN_HEIGHT+yOffset, 0);
		this.positionsCache = createPositionsVector(iNeighborhoodLow, iPatchSize);
		this.positionsCacheHighRes = createPositionsVector(iNeighborhoodHigh, iPatchSize);
	}

	/**
	 * 
	 * @param offset
	 * @param folderSrc
	 * @param folderDst
	 * @param iPatchSize
	 * @param wmsExampleQuery
	 * @param iTexturePatchSizes
	 * @param assetManager
	 * @param cam
	 * @param ts
	 */
	public DynamicPatchedTerrain(float yOffset,Coord offset, File folderSrc,final File folderDst,int iPatchSize,String wmsExampleQuery, int iTextureSizeLow , int iTextureSizeHigh,  AssetManager assetManager,Camera cam,Wuppertal3D ts){
		super(sceneGraphID,iPatchSize,ts, offset);
		this.iTextureSizeLow = iTextureSizeLow;
		this.iTextureSizeHigh = iTextureSizeHigh;
		this.offsetGlobal = offset;
		this.positionsCache = createPositionsVector(iNeighborhoodLow, iPatchSize);
		this.positionsCacheHighRes = createPositionsVector(iNeighborhoodHigh, iPatchSize);
		//TIN j30 Creation
		GML_TIN2J3O.PATCHSIZE = iPatchSize;
		GML_TIN2J3O gml2TinCreator = new GML_TIN2J3O(new File(folderSrc,sceneGraphID));
		gml2TinCreator.setOffsetx(offset.x());
		gml2TinCreator.setOffsety(offset.y());
		gml2TinCreator.setDestFile(folderDst);
		gml2TinCreator.startParsingMultiThreaded(32);
		//TextureCreation for all resolutions
		final HashMap<String, BBox> allBoxesRealById = gml2TinCreator.getAllBBoxesReal(offset);
		int []iTexturePatchSizes = new int[]{iTextureSizeLow,iTextureSizeHigh};
		for(final int iTextureSize : iTexturePatchSizes){
			WMSImageCatchThread.createThreadsAndAddJobsMultiThreaded(wmsExampleQuery, allBoxesRealById, iTextureSize, 8, new WMSImageCatchable() {
				@Override
				public void fireImageReciveEvent(WMSImageCatchJob imageEvent) {
					try{
						ImageIO.write(imageEvent.getImage(), "png", new File(folderDst,iTextureSize+imageEvent.getId()+".png"));
					}
					catch(Exception e){
						e.printStackTrace();
					}
				}
			});
		}
		this.setLocalTranslation(0, -ts.MIN_TERRAIN_HEIGHT+yOffset, 0);
	}

	private  Vector<Integer> getShouldIdsHigh(CAMERAMOVEMENTSTATUS camStatus,Vector3f currentCamPosition){
		float xGlobal = currentCamPosition.x+((float)offsetGlobal.x());
		float zGlobal = ((float)offsetGlobal.y())-currentCamPosition.z;
		Vector<Integer> shouldIdsHigh = new Vector<Integer>();
		for(Vector2f pos : positionsCacheHighRes){
			shouldIdsHigh.add(0,getIdByCamPosition(xGlobal+pos.x, zGlobal+pos.y));
		}
		return shouldIdsHigh;
	}

	public void updateControl(CAMERAMOVEMENTSTATUS camStatus,Vector3f currentCamPosition) {
		//die aktuellen IDs die sichtbar sein sollten berechnen
		Vector<Integer> currentShouldIdsLow = getShouldIds(camStatus,currentCamPosition);
		//die aktuellen IDs die eine hohe texturauflösung verwenden sollten, berechnen
		Vector<Integer> currentShouldIdsHigh = getShouldIdsHigh(camStatus,currentCamPosition);

		Vector<AbstractSinglePatch> patches2dettachCache = new Vector<AbstractSinglePatch>();
		//existierende patches updaten
		for(AbstractSinglePatch patchAbstract:mapPatches){
			SingleTerrainTinPatch patch = (SingleTerrainTinPatch)patchAbstract;//bad code
			patch.updateControl(patches2dettachCache,currentShouldIdsLow,currentShouldIdsHigh,camStatus);
			currentShouldIdsLow.remove(patch.getId());
			//GGF hier das Nachladen der hohen Auflösung anstoßen
			if(camStatus==CAMERAMOVEMENTSTATUS.STANDING && patch.isAttached2GL() && !patch.isDetachingFromGL() && !patch.isTextureHighFileLoading() && currentShouldIdsHigh.contains(patch.getId())){
				wup3d.addTexture2BeLoaded(patch.getId(), iTextureSizeHigh,patch, LOADPRIORITY.LOW);
				patch.setIsTextureHighFileLoading();
			}
		}
		//alte die nicht mehr verwendet werden sollen detachen
		for(AbstractSinglePatch patch:patches2dettachCache){
			detachPatchFromGL(patch);
			mapPatches.remove(patch);
		}
		//wenn die kamera sich gerade schnell bewegt => keine neuen hinzufügen
		if(camStatus==CAMERAMOVEMENTSTATUS.BEAMING){
			//abbrechen: keine neuen hinzufügen
			return;
		}
		//neue patches hinzufügen
		for(Integer id2Add:currentShouldIdsLow){
			SingleTerrainTinPatch patch = new SingleTerrainTinPatch(id2Add, this);
			wup3d.addGeometry2BeLoaded(id2Add, patch, LOADPRIORITY.HIGH,GEOMETRYJOBTYPE.TIN);
			wup3d.addTexture2BeLoaded(id2Add, iTextureSizeLow,patch, LOADPRIORITY.HIGH);
			mapPatches.add(patch);
		}
	}
	
	protected Integer getIdByCamPosition(float xGlobal,float zGlobal){
		xGlobal-=300000;//
		int kmx = (int)FastMath.floor(xGlobal/1000);
		zGlobal-=5600000;
		int kmz = (int)FastMath.floor(zGlobal/1000);
		float restx = xGlobal % (kmx*1000);
		float restz = zGlobal % (kmz*1000);
		int irx =  (int)FastMath.floor(restx/fPatchSize)+1;
		int irz =  (int)FastMath.floor(restz/fPatchSize);
		int iPart = irx+irz*5;
		kmz*=100;
		kmx*=10000;
		//		System.out.println(kmz+kmx+iPart);
		return kmz+kmx+iPart;
	}
}
