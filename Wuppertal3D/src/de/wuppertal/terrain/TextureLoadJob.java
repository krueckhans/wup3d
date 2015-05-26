package de.wuppertal.terrain;

import com.jme3.asset.AssetManager;
import com.jme3.texture.Texture;

import de.wuppertal.abstractAndInterfaces.AbstractLoadJob;
import de.wuppertal.abstractAndInterfaces.TextureLoadListener;
import de.wuppertal.terrain.Wuppertal3D.LOADPRIORITY;

public class TextureLoadJob extends AbstractLoadJob{
	private TextureLoadListener listener;
	private int resolution;
	public TextureLoadJob(Integer id,int resolution, LOADPRIORITY prio, TextureLoadListener listener) {
		super(id, prio);
		this.resolution =resolution;
		this.listener = listener;
	}

	@Override
	public void doJob(AssetManager am) {
		if(isProcessable(getId())){
			Texture t =null;;
			try{
				t = am.loadTexture(resolution+"_"+getId()+".jpg");
			}
			catch(Exception e){
				//			System.err.println("Texture \""+resolution+"_"+getId()+".png"+"\" could not be loaded");
				//			e.printStackTrace();
				addUnProcessableId(getId());
			}
			if(t!=null){
				listener.fireTextureLoaded(getId(),t);
			}
		}
	}

}
