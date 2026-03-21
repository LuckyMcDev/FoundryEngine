package de.luckymcdev.foundryengine.common.md;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.slf4j.Logger;

import java.util.List;

/**
 * Utility class for parsing Markdown into Minecraft Components.
 */
public class MarkdownParser {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Parser PARSER = Parser.builder()
            .extensions(List.of(
                    TablesExtension.create(),
                    StrikethroughExtension.create(),
                    AutolinkExtension.create()
            ))
            .build();

    /**
     * Parse Markdown string into a Minecraft Component.
     *
     * @param markdown The Markdown text to parse
     * @return A formatted MutableComponent, or red error text if parsing fails
     */
    public static MutableComponent parse(String markdown) {
        if (markdown.isEmpty()) return Component.literal("");

        try {
            Node document = PARSER.parse(markdown);
            MarkdownComponentVisitor visitor = new MarkdownComponentVisitor();
            document.accept(visitor);
            return visitor.getComponent();
        } catch (Exception e) {
            LOGGER.error("Failed to parse markdown", e);
            return Component.literal(markdown)
                    .withStyle(style -> style.withColor(0xFF0000));
        }
    }
}