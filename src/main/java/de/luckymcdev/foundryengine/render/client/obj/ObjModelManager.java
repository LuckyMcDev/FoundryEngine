package de.luckymcdev.foundryengine.render.client.obj;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.ApiStatus;
import de.luckymcdev.foundryengine.manager.api.EngineManager;

import java.util.ArrayList;
import java.util.List;

public class ObjModelManager implements EngineManager, ResourceManagerReloadListener {
	private static final int MAX_OBJ_MODELS = 256;

	public List<ObjModel> OBJ_MODELS = new ArrayList<>();

	public ObjModel registerObjModel(ObjModel objModel) {
		OBJ_MODELS.add(objModel);
		while (OBJ_MODELS.size() > MAX_OBJ_MODELS) {
			OBJ_MODELS.removeFirst();
		}
		return objModel;
	}

	@ApiStatus.Internal
	public void loadModels() {
		OBJ_MODELS.forEach(ObjModel::loadModel);
	}

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		loadModels();
	}
}