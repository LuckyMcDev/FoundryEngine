package io.github.luckymcdev.foundryengine.client.imgui.imnodes.lang;

import imgui.ImGui;
import io.github.luckymcdev.foundryengine.client.imgui.imnodes.Node;
import io.github.luckymcdev.foundryengine.client.imgui.imnodes.NodeEditorInstance;
import io.github.luckymcdev.foundryengine.client.imgui.imnodes.pin.NodePin;
import io.github.luckymcdev.foundryengine.client.imgui.imnodes.pin.NodePinShape;
import io.github.luckymcdev.foundryengine.client.imgui.imnodes.pin.NodePinType;

import java.util.*;

/**
 * Defines a language or domain that can be edited visually in the node graph editor.
 *
 * <p>Extend this class to create a new language definition (e.g. Groovy, Lua, GLSL).
 * The definition is responsible for declaring pin types, registering node templates,
 * rendering optional inline body widgets, and generating source code from the graph.
 *
 * <p>Example:
 * <pre>{@code
 * public class MyLang extends NodeLanguageDefinition {
 *
 *     public NodePinType<?> execType;
 *     public NodePinType<?> numberType;
 *
 *     @Override public String languageName() { return "MyLang"; }
 *     @Override public NodePinType<?> rootPinType() { return execType; }
 *
 *     @Override
 *     protected void registerPinTypes() {
 *         execType   = registerPinType("Exec",   NodePinShape.FILLED_SQUARE);
 *         numberType = registerPinType("Number", NodePinShape.FILLED_CIRCLE);
 *     }
 *
 *     @Override
 *     protected void registerNodes() {
 *         register("Print", category("IO"),
 *             List.of(execType.required("exec"), numberType.required("value")),
 *             List.of(execType.output("exec"))
 *         );
 *     }
 *
 *     @Override
 *     public String generateCode(NodeEditorInstance<?> editor) { ... }
 * }
 * }</pre>
 */
public abstract class NodeLanguageDefinition {

    private final Map<String, NodePinType<?>> pinTypeRegistry = new LinkedHashMap<>();
    private final List<NodeTemplate> nodeTemplates = new ArrayList<>();
    private final Map<Integer, Map<String, String>> nodeData = new HashMap<>();
    private boolean initialised = false;

    /**
     * Returns the given string as a category label. Purely cosmetic — improves
     * readability at the call site inside {@link #registerNodes()}.
     *
     * <pre>{@code register("Print", category("IO"), inputs, outputs); }</pre>
     *
     * @param name The category name.
     * @return The same string, unchanged.
     */
    protected static String category(String name) {
        return name;
    }

    /**
     * @return The human-readable name of this language, shown in the panel title.
     */
    public abstract String languageName();

    /**
     * @return The {@link NodePinType} used as the root pin type. Usually the exec type
     * for imperative languages, or the primary data type for expression languages.
     */
    public abstract NodePinType<?> rootPinType();

    /**
     * Creates and returns the {@link NodeEditorInstance} for this language.
     *
     * <p>The default implementation creates an editor whose root node has a single
     * required input pin of {@link #rootPinType()}, matching the standard node editor
     * behaviour (e.g. GLSL, where the root accepts a final value in).
     *
     * <p>Override when you need a different root shape. For example, an imperative
     * language like Groovy wants the root to have an exec output pin so that execution
     * flows out of the root into the first statement node.
     *
     * @return A freshly constructed {@link NodeEditorInstance} ready for use.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public NodeEditorInstance<?> createEditor() {
        return new NodeEditorInstance(rootPinType());
    }

    /**
     * Generates source code from the current state of the node graph.
     *
     * @param editor The live editor instance to traverse.
     * @return The generated source code as a single string.
     */
    public abstract String generateCode(NodeEditorInstance<?> editor);

    /**
     * Renders optional inline ImGui widgets inside the body of a node.
     *
     * <p>Override to add text fields, sliders, checkboxes, etc. for specific node types.
     * The default implementation renders nothing.
     *
     * @param node   The node being rendered.
     * @param editor The live editor instance.
     */
    public void renderNodeBody(Node node, NodeEditorInstance<?> editor) {
    }

    /**
     * Builds the context menu shown on right-click inside the node editor.
     *
     * <p>Groups registered templates by category and renders them as ImGui menu items,
     * using submenus when more than one category is present.
     * Override for full custom control.
     *
     * @param editor The live editor instance that new nodes will be added to.
     */
    public void buildContextMenu(NodeEditorInstance<?> editor) {
        Map<String, List<NodeTemplate>> byCategory = new LinkedHashMap<>();
        for (var template : nodeTemplates) {
            byCategory.computeIfAbsent(template.category(), k -> new ArrayList<>()).add(template);
        }

        for (var entry : byCategory.entrySet()) {
            String cat = entry.getKey();
            List<NodeTemplate> templates = entry.getValue();
            boolean useSubmenu = !cat.isBlank() && byCategory.size() > 1;

            if (useSubmenu && !ImGui.beginMenu(cat)) {
                continue;
            }

            for (var template : templates) {
                if (ImGui.menuItem(template.name())) {
                    editor.addNode(instantiate(template));
                }
            }

            if (useSubmenu) {
                ImGui.endMenu();
            }
        }

        ImGui.separator();

        if (ImGui.menuItem("Clear All")) {
            editor.clear();
        }
    }

    /**
     * Ensures pin types and node templates are registered exactly once.
     * Called automatically by the editor panel before first render.
     */
    public final void ensureInitialised() {
        if (initialised) return;
        initialised = true;
        registerPinTypes();
        registerNodes();
    }

    /**
     * Override to declare all pin types via {@link #registerPinType}.
     * Called once during initialisation, before {@link #registerNodes()}.
     */
    protected abstract void registerPinTypes();

    /**
     * Override to declare all node templates via {@link #register}.
     * Called once during initialisation, after {@link #registerPinTypes()}.
     */
    protected abstract void registerNodes();

    /**
     * Registers a new {@link NodePinType} and returns it for assignment.
     *
     * @param displayName The human-readable name shown on pin labels.
     * @param shape       The default visual shape for pins of this type.
     * @param <T>         The value type carried by this pin during code generation.
     * @return The newly registered pin type.
     */
    @SuppressWarnings("unchecked")
    protected <T> NodePinType<T> registerPinType(String displayName, NodePinShape shape) {
        var type = new NodePinType<T>(displayName, shape, null);
        pinTypeRegistry.put(displayName, type);
        return type;
    }

    /**
     * Registers a node template under a given category.
     *
     * @param name     Display name of the node.
     * @param category Category label for context menu grouping.
     *                 Pass an empty string to place the node at the top level.
     * @param inputs   Input pins, created via {@code myType.required(...)}, etc.
     * @param outputs  Output pins, created via {@code myType.output(...)}.
     */
    protected void register(String name, String category, List<NodePin> inputs, List<NodePin> outputs) {
        nodeTemplates.add(new NodeTemplate(name, category, List.copyOf(inputs), List.copyOf(outputs)));
    }

    /**
     * Registers a node template with no category, placing it at the top level of the context menu.
     *
     * @param name    Display name of the node.
     * @param inputs  Input pins.
     * @param outputs Output pins.
     */
    protected void register(String name, List<NodePin> inputs, List<NodePin> outputs) {
        register(name, "", inputs, outputs);
    }

    /**
     * Gets a string data field stored on a node, or {@code defaultValue} if absent.
     *
     * @param node         The node whose data to read.
     * @param key          Field key, e.g. {@code "value"} or {@code "varName"}.
     * @param defaultValue Fallback if the field has not been set.
     * @return The stored value, or {@code defaultValue}.
     */
    public String getNodeData(Node node, String key, String defaultValue) {
        return nodeData.getOrDefault(node.id, Map.of()).getOrDefault(key, defaultValue);
    }

    /**
     * Sets a string data field on a node.
     *
     * @param node  The node whose data to write.
     * @param key   Field key.
     * @param value The value to store.
     */
    public void setNodeData(Node node, String key, String value) {
        nodeData.computeIfAbsent(node.id, k -> new LinkedHashMap<>()).put(key, value);
    }

    /**
     * Removes all stored data for a node. Call this when the node is deleted.
     *
     * @param node The node to clean up.
     */
    public void clearNodeData(Node node) {
        nodeData.remove(node.id);
    }

    /**
     * Creates a new {@link Node} from a registered template by name.
     *
     * @param templateName The name of the registered template.
     * @return A fresh node ready to be added to the editor.
     * @throws IllegalArgumentException if no template with that name is registered.
     */
    protected Node createNode(String templateName) {
        return nodeTemplates.stream()
                .filter(t -> t.name().equals(templateName))
                .findFirst()
                .map(this::instantiate)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No node template registered with name: " + templateName));
    }

    /**
     * Resolves the data-flow expression for an input pin.
     *
     * <p>If the pin has an incoming link, delegates to {@link #emitExpression} on the
     * source node. Otherwise returns {@code fallback}.
     *
     * @param editor   The editor instance.
     * @param node     The node whose input to resolve.
     * @param pinIndex Index into {@code node.inputPins}.
     * @param fallback Default expression string if nothing is connected.
     * @return A code expression string.
     */
    protected String resolveInput(NodeEditorInstance<?> editor, Node node, int pinIndex, String fallback) {
        if (node.inputPins == null || pinIndex < 0 || pinIndex >= node.inputPins.size()) {
            return fallback;
        }
        var pin = node.inputPins.get(pinIndex);
        if (pin == null || pin.inputLink == null) {
            return fallback;
        }
        return emitExpression(editor, pin.inputLink.node);
    }

    /**
     * Emits a data-flow expression for a given source node.
     *
     * <p>The default implementation returns the node name as a lowercase identifier.
     * Subclasses generating real code must override this.
     *
     * @param editor The editor instance.
     * @param node   The node to emit an expression for.
     * @return A code expression string.
     */
    protected String emitExpression(NodeEditorInstance<?> editor, Node node) {
        return node.name != null ? node.name.toLowerCase(Locale.ROOT) : "null";
    }

    private Node instantiate(NodeTemplate template) {
        return new Node(template.name(), template.allPins());
    }

    /**
     * Represents a registered node template that can be instantiated into the graph.
     *
     * @param name     Display name of the node.
     * @param category Category shown in the context menu, used for submenu grouping.
     * @param inputs   Input pins the instantiated node will have.
     * @param outputs  Output pins the instantiated node will have.
     */
    public record NodeTemplate(
            String name,
            String category,
            List<NodePin> inputs,
            List<NodePin> outputs
    ) {
        /**
         * @return All pins combined in input-first order, as expected by {@link Node}.
         */
        public List<NodePin> allPins() {
            var all = new ArrayList<NodePin>(inputs.size() + outputs.size());
            all.addAll(inputs);
            all.addAll(outputs);
            return all;
        }
    }
}