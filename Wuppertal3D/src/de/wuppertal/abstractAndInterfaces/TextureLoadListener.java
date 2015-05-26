package de.wuppertal.abstractAndInterfaces;

import com.jme3.texture.Texture;

public interface TextureLoadListener {
	public void fireTextureLoaded(Integer id,Texture t);
}
