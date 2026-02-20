package io.github.luckymcdev.foundryengine.client.post.pipeline.pass;

/**
 * A temporary FrameBuffer for a given Name.
 * @param name the name of the temporary Target.
 */
public record TemporaryTarget(String name) {
    /**
     * Creates a new Temporary Target.
     *
     * @param name the name for the target. Must NOT be {@link TargetRef#MAIN_NAME}
     */
    public TemporaryTarget {
        if (TargetRef.MAIN_NAME.equalsIgnoreCase(name)) {
            throw new IllegalArgumentException(
                    "\"main\" is a reserved target name – choose a different name for your TemporaryTarget.");
        }
    }

    /**
     * Convenience for creating a {@link TemporaryTarget}
     * @param name the name of the target.
     * @return the created Temporary Target.
     */
    public static TemporaryTarget named(String name) {
        return new TemporaryTarget(name);
    }
}