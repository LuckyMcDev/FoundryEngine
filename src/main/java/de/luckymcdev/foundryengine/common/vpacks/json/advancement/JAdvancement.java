package de.luckymcdev.foundryengine.common.vpacks.json.advancement;

import com.google.gson.annotations.SerializedName;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JAdvancement {
    private DisplayInfo display;
    private Identifier parent;
    private Map<String, Criterion<?>> criteria;
    private List<List<String>> requirements;
    private AdvancementRewards rewards;
    @SerializedName("sends_telemetry_event")
    private Boolean sendsTelemetryEvent;

    public JAdvancement parent(Identifier parent) {
        this.parent = parent;
        return this;
    }

    public JAdvancement display(DisplayInfo displayInfo) {
        this.display = displayInfo;
        return this;
    }

    public JAdvancement rewards(AdvancementRewards rewards) {
        this.rewards = rewards;
        return this;
    }

    public JAdvancement criteria(String name, Criterion<?> criteria) {
        if (this.criteria == null) {
            this.criteria = new HashMap<>();
        }
        this.criteria.put(name, criteria);
        return this;
    }

    public JAdvancement requirement(List<String> requirement) {
        if (this.requirements == null) {
            this.requirements = new ArrayList<>();
        }
        this.requirements.add(requirement);
        return this;
    }

    public JAdvancement sendsTelemetryEvent() {
        this.sendsTelemetryEvent = true;
        return this;
    }
}
