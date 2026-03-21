package de.luckymcdev.foundryengine.common.vpacks.json.loot;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import de.luckymcdev.foundryengine.common.vpacks.json.JsonUtil;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JLootTable {
    private final String type;
    private List<JPool> pools;
    private List<LootItemFunction> functions;

    public JLootTable(String type) {
        this.type = type;
    }

    public JLootTable pool(JPool pool) {
        if (this.pools == null) {
            this.pools = new ArrayList<>();
        }
        this.pools.add(pool);
        return this;
    }

    public JLootTable function(LootItemFunction function) {
        if (this.functions == null) {
            this.functions = new ArrayList<>();
        }
        this.functions.add(function);
        return this;
    }

    public static class Serializer implements JsonSerializer<JLootTable> {
        @Override
        public JsonElement serialize(JLootTable src, Type type, JsonSerializationContext context) {
            JsonObject lootTable = new JsonObject();
            lootTable.addProperty("type", src.type);
            if (src.pools != null) {
                lootTable.add("pools", context.serialize(src.pools));
            }
            if (src.functions != null) {
                lootTable.add("functions", JsonUtil.serializeCodecList(src.functions, LootItemFunctions.TYPED_CODEC));
            }
            return lootTable;
        }
    }
}
