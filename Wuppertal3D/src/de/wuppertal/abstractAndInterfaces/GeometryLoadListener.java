package de.wuppertal.abstractAndInterfaces;

import com.jme3.scene.Node;

public interface GeometryLoadListener {
	public void fireGeometryLoaded(Integer id,Node n);
}
