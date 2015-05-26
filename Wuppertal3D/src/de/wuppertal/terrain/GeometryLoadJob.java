package de.wuppertal.terrain;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;

import de.wuppertal.abstractAndInterfaces.AbstractLoadJob;
import de.wuppertal.abstractAndInterfaces.GeometryLoadListener;
import de.wuppertal.terrain.Wuppertal3D.LOADPRIORITY;

public class GeometryLoadJob extends AbstractLoadJob {
	private GeometryLoadListener listener;
	public enum GEOMETRYJOBTYPE {TIN,BUILDING};
	private GEOMETRYJOBTYPE type;

	public GeometryLoadJob(Integer id, LOADPRIORITY prio,GeometryLoadListener listener, GEOMETRYJOBTYPE type) {
		super(id, prio);
		this.listener = listener;
		this.type = type;
		
	}

	@Override
	public void doJob(AssetManager am) {
		if(isProcessable(getId())){
			String name ="";
			switch (type){
			case TIN:{
				name = "_"+getId()+".j3o";
				break;
			}
			case BUILDING:{
				name = ""+getId()+".j3o";
				break;
			}
			}
			Node n = null;
			try{
				n = (Node)am.loadModel(name);
			}
			catch(Exception e){
				addUnProcessableId(getId());
//				System.err.println("Geometry of type "+type.name()+" \""+name+"\"");
			}
			if(n!=null){
				listener.fireGeometryLoaded(getId(),n);
			}
		}

	}

}
