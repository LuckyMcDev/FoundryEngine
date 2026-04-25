package de.luckymcdev.foundryengine.common.bbmodel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.luckymcdev.foundryengine.common.bbmodel.data.Outliner;

public final class BbModelGson {

    private BbModelGson() {
    }

    public static Gson create() {
        return new GsonBuilder()
                .registerTypeAdapter(Outliner.class, new OutlinerTypeAdapter())
                .create();
    }
}
