package de.luckymcdev.foundryengine.common.vpacks.json.loot;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.SerializedName;
import de.luckymcdev.foundryengine.common.vpacks.json.JsonUtil;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JPool {
    private List<LootItemCondition> conditions;
    private List<LootItemFunction> functions;
    private List<LootPoolEntryContainer> entries;
    private NumberProvider rolls;
    @SerializedName("bonus_rolls")
    private NumberProvider bonusRolls;

    public JPool entry(LootPoolEntryContainer entry) {
        if (this.entries == null) {
            this.entries = new ArrayList<>();
        }
        this.entries.add(entry);
        return this;
    }

    public JPool condition(LootItemCondition condition) {
        if (this.conditions == null) {
            this.conditions = new ArrayList<>();
        }
        this.conditions.add(condition);
        return this;
    }

    public JPool function(LootItemFunction function) {
        if (this.functions == null) {
            this.functions = new ArrayList<>();
        }
        this.functions.add(function);
        return this;
    }

    public JPool rolls(NumberProvider rolls) {
        this.rolls = rolls;
        return this;
    }

    public JPool bonusRolls(NumberProvider bonusRolls) {
        this.bonusRolls = bonusRolls;
        return this;
    }

    public static class Serializer implements JsonSerializer<JPool> {
        @Override
        public JsonElement serialize(JPool src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            if (src.conditions != null) {
                jsonObject.add("conditions", JsonUtil.serializeCodecList(src.conditions, LootItemCondition.TYPED_CODEC));
            }
            if (src.functions != null) {
                jsonObject.add("functions", JsonUtil.serializeCodecList(src.functions, LootItemFunctions.TYPED_CODEC));
            }
            if (src.entries != null) {
                jsonObject.add("entries", JsonUtil.serializeCodecList(src.entries, LootPoolEntries.CODEC));
            }
            if (src.rolls != null) {
                jsonObject.add("rolls", JsonUtil.serializeCodec(src.rolls, NumberProviders.CODEC));
            }
            if (src.bonusRolls != null) {
                jsonObject.add("bonus_rolls", JsonUtil.serializeCodec(src.bonusRolls, NumberProviders.CODEC));
            }
            return jsonObject;
        }
    }
}
