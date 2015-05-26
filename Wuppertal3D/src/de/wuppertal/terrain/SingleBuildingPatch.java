package de.wuppertal.terrain;

import java.util.Vector;

import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;

import de.wuppertal.abstractAndInterfaces.AbstractSinglePatch;
import de.wuppertal.abstractAndInterfaces.GeometryLoadListener;
import de.wuppertal.abstractAndInterfaces.TextureLoadListener;
import de.wuppertal.terrain.Wuppertal3D.CAMERAMOVEMENTSTATUS;

public class SingleBuildingPatch extends AbstractSinglePatch implements TextureLoadListener,GeometryLoadListener{
	private volatile boolean isGeometryFileLoaded;
	private volatile boolean isTextureFileLoaded;
	private final boolean isGeometryOnly; 
	private Texture texture;
	private DynamicPatchedBuildings patchedBuildings;
	public static boolean bWireframe = false;


	public SingleBuildingPatch(Integer id2Add,	DynamicPatchedBuildings dynamicPatchedBuildings, boolean isGeometryOnly) {
		super(id2Add);
		this.patchedBuildings = dynamicPatchedBuildings;
		isGeometryFileLoaded = false;
		isDetachingFromGL = false;
		isAttached2GL = false;
		isAttaching2GL = false;
		this.isGeometryOnly = isGeometryOnly;
	}

	public void updateControl(Vector<AbstractSinglePatch> patches2Detach, Vector<Integer> currentIds,CAMERAMOVEMENTSTATUS camStatus) {
		if(!currentIds.contains(id)){
			//wennn this ID gar nicht mehr vorhanden ist => detaching
			isDetachingFromGL = true;
			patches2Detach.add(this);
			return;
		}
		//wenn geometry und texture geladen wurde, dieses patch aber noch nicht im GL ist => GL Laden anstoßen
		if(isGeometryFileLoaded && (isTextureFileLoaded || isGeometryOnly ) && !isDetachingFromGL && !isAttaching2GL && !isAttached2GL){
			for(Spatial sp: node.getChildren()){
				Geometry  g = (Geometry) sp;
				if(g!=null){
					if(!isGeometryOnly){
						g.getMaterial().setTexture("DiffuseMap",texture);
					}
					g.getMaterial().getAdditionalRenderState().setWireframe(bWireframe);
				}
			}
			patchedBuildings.attachPatch2GL(this);
			isAttaching2GL = true;
			return;
		}
	}

	@Override
	public void fireGeometryLoaded(Integer id, Node n) {
		if(!isDetachingFromGL){
			this.node = n;
			node.setLocalScale(1, 1, -1);
			isGeometryFileLoaded = true;
		}
	}

	@Override
	public void fireTextureLoaded(Integer id, Texture t) {
		if(!isDetachingFromGL){
			texture = t;
			isTextureFileLoaded = true;
		}
	}

	

	
}