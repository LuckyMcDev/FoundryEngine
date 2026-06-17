package de.luckymcdev.foundryengine.common.graph.domain;

import de.luckymcdev.foundryengine.common.graph.model.GraphModel;
import de.luckymcdev.foundryengine.common.graph.type.PinType;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * A graph domain owns validation and code generation for a family of
 * node graphs (shader, event scripting, JSON editing, etc.).
 * <p>
 * Each domain maintains its own handler registries for node definitions
 * that participate in it. Handlers implement domain-specific interfaces
 * like {@link ShaderNodeHandler}, never a unified "DomainLogic".
 */
public interface GraphDomain {

    Identifier id();

    String displayName();

    /**
     * The exec flow pin type for this domain, or {@code null}
     * if the domain is pure dataflow (shader, JSON).
     */
    default PinType flowType() { return null; }

    /**
     * Validate a graph and collect errors.
     * @return list of validation error messages (empty = valid)
     */
    List<String> validate(GraphModel graph);

    /**
     * Generate output from a validated graph.
     * Domain-specific subclasses return richer result types.
     */
    String generate(GraphModel graph);
}
