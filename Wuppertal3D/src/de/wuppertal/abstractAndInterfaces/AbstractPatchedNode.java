package de.wuppertal.abstractAndInterfaces;

import java.util.Stack;
import java.util.TreeMap;
import java.util.Vector;

import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import de.wuppertal.Coord;
import de.wuppertal.terrain.Wuppertal3D;
import de.wuppertal.terrain.Wuppertal3D.CAMERAMOVEMENTSTATUS;



public abstract class AbstractPatchedNode extends Node{
	protected float fPatchSize;
	protected Wuppertal3D wup3d;
	protected Vector2f[] positionsCache;
	protected Vector<AbstractSinglePatch> mapPatches ;
	protected Stack<AbstractSinglePatch> patches2Attach;
	protected Stack<AbstractSinglePatch> patches2Detach;
	protected Coord offsetGlobal;
	
	
	protected abstract void updateControl(CAMERAMOVEMENTSTATUS camStatus,Vector3f currentCamPosition);
	
	public AbstractPatchedNode(String scenegraphid,float fPatchSize,Wuppertal3D wup3d, Coord offsetGlobal) {
		super(scenegraphid);
		this.offsetGlobal = offsetGlobal;
		this.fPatchSize = fPatchSize;
		this.wup3d = wup3d;
		this.patches2Attach = new Stack<AbstractSinglePatch>();
		this.patches2Detach = new Stack<AbstractSinglePatch>();
		this.mapPatches = new Vector<AbstractSinglePatch>();
	}
	
	
	public void update(float tpf){
		//Attach max 1 per Frame
		AbstractSinglePatch patch=null;
		synchronized (patches2Attach) {
			while(!patches2Attach.isEmpty()){
				patch = patches2Attach.pop();
				this.attachChild(patch.getNode());
				patch.setAttached2GL();
			}
		}
		//one per Frame
		synchronized (patches2Detach) {
			while(!patches2Detach.isEmpty()){
				patch = patches2Detach.pop();
				if(this.hasChild(patch.getNode())){
					this.detachChild(patch.getNode());
					patch.setDetachedFromGL();
				}
			}
		}
	}
	

	protected  Vector<Integer> getShouldIds(CAMERAMOVEMENTSTATUS camStatus,Vector3f currentCamPosition){
		float xGlobal = currentCamPosition.x+((float)offsetGlobal.x());
		float zGlobal = ((float)offsetGlobal.y())-currentCamPosition.z;
		Vector<Integer> shouldIdsLow = new Vector<Integer>();
		for(Vector2f pos : positionsCache){
			shouldIdsLow.add(0,getIdByCamPosition(xGlobal+pos.x, zGlobal+pos.y));
		}
		return shouldIdsLow;
	}
	
	

	protected abstract Integer getIdByCamPosition(float xGlobal,float zGlobal);


	protected Vector2f[] createPositionsVector(float n, float fPatchSize){
		TreeMap<Float, Vector2f> coordsByDistance = new TreeMap<Float, Vector2f>();
		//n =1 => 9, n =2  => 25, n =3 => 49, n =4 => 81,
		//System.out.println("Start adding");
		for(float x= -n*fPatchSize;x<=+n*fPatchSize;x+=fPatchSize){
			for(float z= -n*fPatchSize;z<=n*fPatchSize;z+=fPatchSize){
				//addSinglePatch(new Vector3f(x, currentCamPosition.y, z));
				float sqrt = FastMath.sqrt(x*x+z*z);
				while(coordsByDistance.containsKey(sqrt)){
					sqrt-=0.01;
				}
				coordsByDistance.put(sqrt, new Vector2f(x,z));
			}	
		}
		Vector2f[] positions= new Vector2f[coordsByDistance.size()];
		int i=0;
		for(Vector2f c: coordsByDistance.values()){
			positions[i] =c;
			i++;
		}
		return positions;
	}
	
	public void detachPatchFromGL(AbstractSinglePatch patch2detach){
		synchronized (patches2Detach) {
			if(!patches2Detach.contains(patch2detach)){
				patches2Detach.add(patch2detach);
			}
		}
	}

	public void attachPatch2GL(AbstractSinglePatch patch2attach){
		synchronized (patches2Attach) {
			if(!patches2Attach.contains(patch2attach)){
				patches2Attach.add(patches2Attach.size(),patch2attach);
			}
		}
	}

}
