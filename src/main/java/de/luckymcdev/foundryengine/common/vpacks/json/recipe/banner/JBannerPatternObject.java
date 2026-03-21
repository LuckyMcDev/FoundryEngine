package de.luckymcdev.foundryengine.common.vpacks.json.recipe.banner;

import com.google.gson.annotations.SerializedName;
import net.minecraft.resources.Identifier;

public class JBannerPatternObject {
    @SerializedName("asset_id")
    private Identifier assetId;
    @SerializedName("translation_key")
    private String translationKey;

    public JBannerPatternObject assetId(Identifier assetId) {
        this.assetId = assetId;
        return this;
    }

    public JBannerPatternObject translationKey(String translationKey) {
        this.translationKey = translationKey;
        return this;
    }
}
