package de.wuppertal.terrain;

import java.io.File;
import java.util.Vector;

import com.jme3.math.Vector3f;

import de.wuppertal.Coord;
import de.wuppertal.abstractAndInterfaces.AbstractPatchedNode;
import de.wuppertal.abstractAndInterfaces.AbstractSinglePatch;
import de.wuppertal.terrain.GeometryLoadJob.GEOMETRYJOBTYPE;
import de.wuppertal.terrain.Wuppertal3D.CAMERAMOVEMENTSTATUS;
import de.wuppertal.terrain.Wuppertal3D.LOADPRIORITY;
import de.wuppertal.tools.gmlGeometryParser.CityGML2J3O;

public class DynamicPatchedBuildings extends AbstractPatchedNode{
	private static final int iNeighborhoodLow = 1;
	public static final String sceneGraphID= "buildings_patched";
	private boolean bGeometryOnly;

	public DynamicPatchedBuildings(File folderSrc,File folderDst, Coord offsetGlobal,int fPatchSize,Wuppertal3D ts,boolean bCreationMode,boolean bGeometryOnly) {
		super(sceneGraphID,fPatchSize,ts,offsetGlobal);
		this.bGeometryOnly = bGeometryOnly;
		if(bCreationMode){
			CityGML2J3O app = new CityGML2J3O(folderSrc,folderDst,true,false);
			app.setOffsetx(offsetGlobal.x());
			app.setOffsety(offsetGlobal.y());
			app.startParsingMultiThreaded(4);
		}
		this.setLocalTranslation(0, -ts.MIN_TERRAIN_HEIGHT, 0);
		this.positionsCache = createPositionsVector(iNeighborhoodLow, fPatchSize);
	}

	public void updateControl(CAMERAMOVEMENTSTATUS camStatus,Vector3f currentCamPosition) {
		//die aktuellen IDs die sichtbar sein sollten berechnen
		Vector<Integer> currentShouldIds = getShouldIds(camStatus,currentCamPosition);
		Vector<AbstractSinglePatch> patched2dettach = new Vector<AbstractSinglePatch>();
		//existierende patches updaten
		for(AbstractSinglePatch patchAbstract:mapPatches){
			SingleBuildingPatch patch = (SingleBuildingPatch)patchAbstract;//bad code
			patch.updateControl(patched2dettach,currentShouldIds,camStatus);
			currentShouldIds.remove(patch.getId());
		}
		//alte die nicht mehr verwendet werden sollen detachen
		for(AbstractSinglePatch patch:patched2dettach){
			detachPatchFromGL(patch);
			mapPatches.remove(patch);
		}
		//wenn die kamera sich gerade schnell bewegt => keine neuen hinzufügen
		if(camStatus==CAMERAMOVEMENTSTATUS.BEAMING){
			//abbrechen: keine neuen hinzufügen
			return;
		}
		//neue patches hinzufügen
		for(Integer id2Add:currentShouldIds){
			SingleBuildingPatch patch = new SingleBuildingPatch(id2Add, this,true);
			wup3d.addGeometry2BeLoaded(id2Add, patch, LOADPRIORITY.HIGH,GEOMETRYJOBTYPE.BUILDING);
			if(!bGeometryOnly){
				wup3d.addTexture2BeLoaded(id2Add,-1,patch, LOADPRIORITY.HIGH);
			}
			mapPatches.add(patch);
		}
	}
	
	protected Integer getIdByCamPosition(float xGlobal,float zGlobal){
		zGlobal-=5000000;
		int ixGlobal = (int)(xGlobal /1000);
		int izGlobal = (int)(zGlobal /1000);
		return ixGlobal*1000+izGlobal;
	}
}
