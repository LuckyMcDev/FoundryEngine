package de.luckymcdev.foundryengine.common.bbmodel;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.luckymcdev.foundryengine.common.bbmodel.data.BbModel;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BbModelReader {
    private static final Map<String, BbModel> models = new HashMap<>();
    private static final Gson gson = BbModelGson.create();

    /**
     * Load a model from a file
     *
     * @param modelPath the path to the model
     * @param ressource if the model is in the ressources folder or not
     * @param string    the name of the model
     * @return the model
     */
    public static BbModel loadModel(String modelPath, boolean ressource, String string) {
        File file;
        if (ressource) {
            file = new File(Objects.requireNonNull(BbModelReader.class.getClassLoader().getResource(modelPath)).getFile());
        } else {
            file = new File(modelPath);
        }
        return loadModel(file, string);
    }

    /**
     * Load a model from a file
     *
     * @param modelPath the path to the model
     * @param ressource if the model is in the ressources folder or not
     * @return the model
     */
    public static BbModel loadModel(String modelPath, boolean ressource) {
        File file;
        if (ressource) {
            file = new File(Objects.requireNonNull(BbModelReader.class.getClassLoader().getResource(modelPath)).getFile());
        } else {
            file = new File(modelPath);
        }
        String name = file.getName();

        return loadModel(file, name);
    }

    /**
     * Load a model from a file
     *
     * @param file the file
     * @param name the name of the model
     * @return the model
     */
    public static BbModel loadModel(File file, String name) {
        String jsonString;
        try {
            jsonString = new String(Files.readAllBytes(file.toPath()));
        } catch (Exception e) {
            throw new RuntimeException("Error while reading file " + name, e);
        }
        if (jsonString.startsWith("<lz>")) {
            throw new IllegalArgumentException("Compressed bbmodel files ('<lz>...') are not supported yet");
        }
        JsonObject object = JsonParser.parseString(jsonString).getAsJsonObject();

        if (!object.has("elements") && object.has("cubes")) {
            object.add("elements", object.get("cubes"));
        }

        if (!object.has("model_identifier") && object.has("geometry_name")) {
            object.add("model_identifier", object.get("geometry_name"));
        }

        if (object.has("meta") && object.get("meta").isJsonObject()) {
            JsonObject meta = object.getAsJsonObject("meta");
            if (!meta.has("format_version") && meta.has("format")) {
                meta.add("format_version", meta.get("format"));
            }
        }

        BbModel model = gson.fromJson(object, BbModel.class);
        models.put(name, model);

        return model;
    }

    public static BbModel loadFromJson(String json, String name) {
        if (json == null || json.isBlank()) throw new IllegalArgumentException("json must not be blank");
        if (json.startsWith("<lz>")) throw new IllegalArgumentException("Compressed bbmodel not supported");
        JsonObject object = JsonParser.parseString(json).getAsJsonObject();
        if (!object.has("elements") && object.has("cubes")) object.add("elements", object.get("cubes"));
        if (!object.has("model_identifier") && object.has("geometry_name"))
            object.add("model_identifier", object.get("geometry_name"));
        if (object.has("meta") && object.get("meta").isJsonObject()) {
            JsonObject meta = object.getAsJsonObject("meta");
            if (!meta.has("format_version") && meta.has("format")) meta.add("format_version", meta.get("format"));
        }
        BbModel model = gson.fromJson(object, BbModel.class);
        if (name != null && !name.isBlank()) models.put(name, model);
        return model;
    }

    /**
     * Get a model by its name
     *
     * @param name the name of the model
     * @return the model
     */
    public static BbModel getModel(String name) {
        return models.get(name);
    }

}
