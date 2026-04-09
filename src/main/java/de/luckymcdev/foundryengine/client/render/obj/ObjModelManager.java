package de.luckymcdev.foundryengine.client.render.obj;

import java.util.ArrayList;
import java.util.List;

public class ObjModelManager {
    public static List<ObjModel> OBJ_MODELS = new ArrayList<>();

    public static ObjModel registerObjModel(ObjModel objModel) {
        OBJ_MODELS.add(objModel);
        return objModel;
    }

    public static void loadModels() {
        OBJ_MODELS.forEach(ObjModel::loadModel);
    }
}