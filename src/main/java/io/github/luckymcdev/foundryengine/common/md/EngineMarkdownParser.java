package io.github.luckymcdev.foundryengine.common.md;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.slf4j.Logger;

import java.util.Arrays;

public class EngineMarkdownParser {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Parser GFM_PARSER = Parser.builder()
            .extensions(Arrays.asList(
                    TablesExtension.create(),
                    StrikethroughExtension.create(),
                    AutolinkExtension.create()
            )).build();

    public static MutableComponent parse(String input) {
        if (input == null || input.isEmpty()) {
            return Component.literal("");
        }

        try {
            Node document = GFM_PARSER.parse(input);

            ComponentBuildingVisitor visitor = new GfmComponentBuildingVisitor();

            document.accept(visitor);

            return visitor.getComponent();

        } catch (Exception e) {
            LOGGER.error("Failed to parse markdown: {}", input, e);
            return Component.literal(input).withStyle(style -> style.withColor(0xFF0000));
        }
    }
}