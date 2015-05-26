package de.wuppertal.abstractAndInterfaces;

import com.jme3.scene.Node;


public abstract class AbstractSinglePatch {
	protected Integer id;
	protected Node node;
	protected volatile boolean isAttached2GL;
	protected volatile boolean isAttaching2GL;
	protected volatile boolean isDetachingFromGL;
	
	
	public AbstractSinglePatch(Integer id){
		this.id = id;
		isAttached2GL = false;
		isAttaching2GL = false;
		isDetachingFromGL = false;
	}
	
	
	public void setAttached2GL() {
		isAttached2GL =true;
		isAttaching2GL =false;
	}
	

	public void setDetachedFromGL() {
		isAttached2GL = false;
		isDetachingFromGL = false;
		node = null;
	}

	public boolean isAttached2GL() {
		return isAttached2GL;
	}

	public boolean isDetachingFromGL() {
		return isDetachingFromGL;
	}
	
	
	
	public Integer getId(){
		return id;
	}
	
	public Node getNode(){
		return node;
	}
	
}
