package io.github.luckymcdev.foundryengine.common.vpacks.json.recipe.component;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.minecraft.resources.Identifier;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JBeesComponent extends AbstractJComponent {
    private final List<BeeData> bees = new ArrayList<>();

    public JBeesComponent bee(BeeData bee) {
        this.bees.add(bee);
        return this;
    }

    public static class BeeData {
        public Identifier entityId;
        public int minimumTicksInHive = 0;
        public int ticksInHive = 0;

        public BeeData entityId(Identifier entityId) {
            this.entityId = entityId;
            return this;
        }

        public BeeData minimumTicksInHive(int minimumTicksInHive) {
            this.minimumTicksInHive = minimumTicksInHive;
            return this;
        }

        public BeeData ticksInHive(int ticksInHive) {
            this.ticksInHive = ticksInHive;
            return this;
        }

        public static class Serializer implements JsonSerializer<BeeData> {
            @Override
            public JsonElement serialize(BeeData src, Type type, JsonSerializationContext context) {
                JsonObject jsonObject = new JsonObject();
                JsonObject entityData = new JsonObject();
                entityData.add("id", context.serialize(src.entityId));

                jsonObject.add("entity_data", entityData);
                jsonObject.add("min_ticks_in_hive", context.serialize(src.minimumTicksInHive));
                jsonObject.add("ticks_in_hive", context.serialize(src.ticksInHive));
                return jsonObject;
            }
        }
    }

    public static class Serializer implements JsonSerializer<JBeesComponent> {
        @Override
        public JsonElement serialize(JBeesComponent src, Type type, JsonSerializationContext context) {
            return context.serialize(src.bees);
        }
    }
}
