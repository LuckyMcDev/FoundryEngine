package io.github.luckymcdev.client.opengl.shaders.preprocessing;

import com.mojang.logging.LogUtils;
import io.github.luckymcdev.common.Commons;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The type Include glsl pre-processor.
 */
public class IncludeGLSLPreProcessor extends GLSLPreProcessor {
    private static final Logger LOGGER = LogUtils.getLogger();

    // This regex mimics Minecraft's: matches #include "path" or #include <path>
    // Group 2 is the " " path, Group 3 is the < > path
    private static final Pattern INCLUDE_PATTERN = Pattern.compile(
            "(?:^|\\v)\\s*#include\\s+(?:\"([^\"]+)\"|<([^>]+)>)"
    );

    /**
     * Instantiates a new Include glsl pre-processor.
     */
    public IncludeGLSLPreProcessor() {
        super(Commons.id("include_preprocessor"), INCLUDE_PATTERN);
    }

    @Override
    public String apply(String source) {
        StringBuilder sb = new StringBuilder();
        Matcher matcher = INCLUDE_PATTERN.matcher(source);
        int lastEnd = 0;

        while (matcher.find()) {
            sb.append(source, lastEnd, matcher.start());

            String path = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);

            if (path != null) {
                try {
                    Identifier loc;
                    if (path.contains("/")) {
                        // Split "minecraft/fog.glsl" into "minecraft" and "fog.glsl"
                        String namespace = path.substring(0, path.indexOf("/"));
                        String remainingPath = path.substring(path.indexOf("/") + 1);

                        // Reconstruct the internal Minecraft path
                        loc = Identifier.fromNamespaceAndPath(namespace, "shaders/include/" + remainingPath);
                    } else {
                        // Fallback for local files in your mod's namespace
                        loc = Commons.id("shaders/include/" + path);
                    }

                    String rawSource = Commons.getRlSource(loc);

                    if (rawSource == null || rawSource.isEmpty()) {
                        sb.append("\n/* Error: Source for ").append(loc).append(" was null or empty */\n");
                    } else {
                        sb.append("\n// --- START: ").append(loc).append(" ---\n");
                        String strippedSource = rawSource.replaceAll("(?m)^\\s*#version.*", "// Removed version for include");
                        sb.append(this.apply(strippedSource));
                        sb.append("\n// --- END: ").append(loc).append(" ---\n");
                    }
                } catch (Exception e) {
                    sb.append("\n/* Error resolving: ").append(path).append(" */\n");
                }
            }

            lastEnd = matcher.end();
        }

        sb.append(source.substring(lastEnd));

        return sb.toString();
    }
}