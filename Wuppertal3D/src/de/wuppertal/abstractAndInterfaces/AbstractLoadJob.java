package de.wuppertal.abstractAndInterfaces;

import java.util.TreeSet;

import com.jme3.asset.AssetManager;

import de.wuppertal.terrain.Wuppertal3D.LOADPRIORITY;

public abstract class AbstractLoadJob {
	private int id;
	private LOADPRIORITY prio;
	private static TreeSet<Integer> unprocessableIds;
	public AbstractLoadJob(int id, LOADPRIORITY prio){
		this.id = id;
		this.prio = prio;
		if(unprocessableIds==null){
			unprocessableIds = new TreeSet<Integer>();
		}
	}
	
	public int getId(){
		return id;
	}
	
	public LOADPRIORITY getPrio(){
		return prio;
	}
	
	public abstract void doJob(AssetManager am);
	
	protected boolean isProcessable(Integer id){
		return !unprocessableIds.contains(id);
	}
	
	protected void addUnProcessableId(int id){
		synchronized (unprocessableIds) {
			unprocessableIds.add(new Integer(id));
		}
	}
	
	public boolean equals(Object o){
		return ((AbstractLoadJob)o).id == id;
	}
}
