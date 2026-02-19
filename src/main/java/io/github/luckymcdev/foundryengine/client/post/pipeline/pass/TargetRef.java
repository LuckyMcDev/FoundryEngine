package io.github.luckymcdev.foundryengine.client.post.pipeline.pass;

/**
 * A reference to a named framebuffer used as the input or output of a pipeline pass.
 *
 * <p>There are two kinds of target:</p>
 * <ul>
 *   <li><b>Main</b> — the singleton {@link #MAIN} constant, representing Minecraft's
 *       primary render target.  When used as an <em>output</em> the manager renders into
 *       a temporary buffer and blits the result back into the main framebuffer.</li>
 *   <li><b>Temporary</b> — any name other than {@code "main"}, resolved against the
 *       {@link TemporaryTarget} declarations on the owning pipeline.  The manager
 *       allocates (and auto-resizes) one {@code FrameBuffer} per unique name per
 *       pipeline, mirroring Minecraft's {@code PostChain} targets.</li>
 * </ul>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * // Built-in sentinel
 * TargetRef.MAIN
 *
 * // Named temporary (must also appear in the pipeline's TemporaryTarget list)
 * TargetRef.of("blur_ping")
 * TargetRef.of("blur_pong")
 * TargetRef.of("brightness")
 * }</pre>
 */
public final class TargetRef {

    /** The reserved name for the main render target. */
    public static final String MAIN_NAME = "main";

    /** Singleton representing Minecraft's primary render target. */
    public static final TargetRef MAIN = new TargetRef(MAIN_NAME);

    private final String name;

    private TargetRef(String name) {
        this.name = name;
    }

    /**
     * Returns a {@link TargetRef} for the given name.
     *
     * @param name an arbitrary identifier for a temporary framebuffer declared via
     *             {@link TemporaryTarget}.  Must not be {@code "main"} — use {@link #MAIN}.
     * @throws IllegalArgumentException if {@code name} is {@code "main"} (use the constant)
     */
    public static TargetRef of(String name) {
        if (MAIN_NAME.equalsIgnoreCase(name)) {
            throw new IllegalArgumentException(
                    "Use TargetRef.MAIN instead of TargetRef.of(\"main\").");
        }
        return new TargetRef(name);
    }

    /** Returns the raw name of this target. */
    public String getName() { return name; }

    /** Returns {@code true} if this reference points to the main render target. */
    public boolean isMain() { return MAIN_NAME.equals(name); }

    @Override
    public String toString() { return name; }

    @Override
    public boolean equals(Object o) {
        return o instanceof TargetRef r && name.equals(r.name);
    }

    @Override
    public int hashCode() { return name.hashCode(); }
}