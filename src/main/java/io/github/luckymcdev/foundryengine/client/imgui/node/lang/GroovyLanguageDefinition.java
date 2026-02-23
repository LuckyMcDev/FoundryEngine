package io.github.luckymcdev.foundryengine.client.imgui.node.lang;

import imgui.ImGui;
import imgui.type.ImString;
import io.github.luckymcdev.foundryengine.client.imgui.node.Node;
import io.github.luckymcdev.foundryengine.client.imgui.node.NodeEditorInstance;
import io.github.luckymcdev.foundryengine.client.imgui.node.pin.NodePinInfo;
import io.github.luckymcdev.foundryengine.client.imgui.node.pin.NodePinShape;
import io.github.luckymcdev.foundryengine.client.imgui.node.pin.NodePinType;

import java.util.*;

/**
 * A {@link NodeLanguageDefinition} for the Groovy scripting language.
 *
 * <p>Provides nodes for common Groovy constructs — variable declarations, control flow,
 * arithmetic, string operations, method calls, and literals — and generates valid
 * Groovy source code by traversing the execution flow graph from the root node.
 *
 * <h2>Pin type legend</h2>
 * <ul>
 *   <li><b>Exec</b> (filled square) — control-flow connections (statement ordering)</li>
 *   <li><b>Any</b> (circle) — dynamically-typed value, compatible with everything</li>
 *   <li><b>String</b> (filled circle) — string values</li>
 *   <li><b>Number</b> (filled square) — numeric values</li>
 *   <li><b>Boolean</b> (triangle) — boolean values</li>
 *   <li><b>List</b> (filled triangle) — list/collection values</li>
 * </ul>
 */
public class GroovyLanguageDefinition extends NodeLanguageDefinition {

    private final Map<String, ImString> inputBuffers = new HashMap<>();
    /**
     * Execution flow pin — controls statement ordering.
     */
    public NodePinType<?> execType;
    /**
     * Dynamically-typed value pin — compatible with any other data pin.
     */
    public NodePinType<?> anyType;
    /**
     * String value pin.
     */
    public NodePinType<?> stringType;
    /**
     * Numeric value pin (int or double at runtime).
     */
    public NodePinType<?> numberType;
    /**
     * Boolean value pin.
     */
    public NodePinType<?> boolType;
    /**
     * List/collection value pin.
     */
    public NodePinType<?> listType;

    @Override
    public String languageName() {
        return "Groovy";
    }

    @Override
    public NodePinType<?> rootPinType() {
        ensureInitialised();
        return execType;
    }

    /**
     * Creates an editor whose root node ("Script Start") has a single exec output pin.
     * Execution flows out of the root into the first connected statement, which is the
     * correct model for an imperative language like Groovy.
     */
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public NodeEditorInstance<?> createEditor() {
        ensureInitialised();
        var editor = new NodeEditorInstance(execType, List.of(execType.output("exec")));
        editor.root.name = "Script Start";
        return editor;
    }

    @Override
    protected void registerPinTypes() {
        execType = registerPinType("Exec", NodePinShape.FILLED_SQUARE);
        anyType = registerPinType("Any", NodePinShape.CIRCLE);
        stringType = registerPinType("String", NodePinShape.FILLED_CIRCLE);
        numberType = registerPinType("Number", NodePinShape.FILLED_SQUARE);
        boolType = registerPinType("Boolean", NodePinShape.TRIANGLE);
        listType = registerPinType("List", NodePinShape.FILLED_TRIANGLE);
    }

    @Override
    protected void registerNodes() {
        register("If",
                category("Control Flow"),
                List.of(execType.required("exec"), boolType.required("condition")),
                List.of(execType.output("true"), execType.output("false"))
        );
        register("While",
                category("Control Flow"),
                List.of(execType.required("exec"), boolType.required("condition")),
                List.of(execType.output("body"), execType.output("done"))
        );
        register("ForEach",
                category("Control Flow"),
                List.of(execType.required("exec"), listType.required("list")),
                List.of(execType.output("body"), anyType.output("item"), execType.output("done"))
        );
        register("Return",
                category("Control Flow"),
                List.of(execType.required("exec"), anyType.optional("value")),
                List.of()
        );

        register("DeclareVar",
                category("Variables"),
                List.of(execType.required("exec"), anyType.optional("value")),
                List.of(execType.output("exec"), anyType.output("ref"))
        );
        register("SetVar",
                category("Variables"),
                List.of(execType.required("exec"), anyType.required("value")),
                List.of(execType.output("exec"))
        );
        register("GetVar",
                category("Variables"),
                List.of(),
                List.of(anyType.output("ref"))
        );

        register("Print",
                category("IO"),
                List.of(execType.required("exec"), anyType.required("value")),
                List.of(execType.output("exec"))
        );
        register("PrintLine",
                category("IO"),
                List.of(execType.required("exec"), anyType.required("value")),
                List.of(execType.output("exec"))
        );

        register("MethodCall",
                category("Methods"),
                List.of(execType.required("exec"), anyType.optional("target"),
                        anyType.optional("arg0"), anyType.optional("arg1"), anyType.optional("arg2")),
                List.of(execType.output("exec"), anyType.output("result"))
        );
        register("ClosureCall",
                category("Methods"),
                List.of(execType.required("exec"), anyType.required("closure"),
                        anyType.optional("arg0"), anyType.optional("arg1")),
                List.of(execType.output("exec"), anyType.output("result"))
        );

        register("StringLiteral",
                category("Literals"),
                List.of(),
                List.of(stringType.output("value"))
        );
        register("NumberLiteral",
                category("Literals"),
                List.of(),
                List.of(numberType.output("value"))
        );
        register("BoolLiteral",
                category("Literals"),
                List.of(),
                List.of(boolType.output("value"))
        );
        register("ListLiteral",
                category("Literals"),
                List.of(anyType.optional("item0"), anyType.optional("item1"), anyType.optional("item2")),
                List.of(listType.output("list"))
        );
        register("NullLiteral",
                category("Literals"),
                List.of(),
                List.of(anyType.output("value"))
        );

        register("Add", category("Math"), List.of(numberType.required("a"), numberType.required("b")), List.of(numberType.output("result")));
        register("Subtract", category("Math"), List.of(numberType.required("a"), numberType.required("b")), List.of(numberType.output("result")));
        register("Multiply", category("Math"), List.of(numberType.required("a"), numberType.required("b")), List.of(numberType.output("result")));
        register("Divide", category("Math"), List.of(numberType.required("a"), numberType.required("b")), List.of(numberType.output("result")));
        register("Modulo", category("Math"), List.of(numberType.required("a"), numberType.required("b")), List.of(numberType.output("result")));
        register("Power", category("Math"), List.of(numberType.required("base"), numberType.required("exp")), List.of(numberType.output("result")));
        register("Negate", category("Math"), List.of(numberType.required("value")), List.of(numberType.output("result")));

        register("Equals", category("Comparison"), List.of(anyType.required("a"), anyType.required("b")), List.of(boolType.output("result")));
        register("NotEquals", category("Comparison"), List.of(anyType.required("a"), anyType.required("b")), List.of(boolType.output("result")));
        register("GreaterThan", category("Comparison"), List.of(numberType.required("a"), numberType.required("b")), List.of(boolType.output("result")));
        register("LessThan", category("Comparison"), List.of(numberType.required("a"), numberType.required("b")), List.of(boolType.output("result")));

        register("And", category("Logic"), List.of(boolType.required("a"), boolType.required("b")), List.of(boolType.output("result")));
        register("Or", category("Logic"), List.of(boolType.required("a"), boolType.required("b")), List.of(boolType.output("result")));
        register("Not", category("Logic"), List.of(boolType.required("value")), List.of(boolType.output("result")));

        register("Concat",
                category("Strings"),
                List.of(stringType.required("a"), stringType.required("b")),
                List.of(stringType.output("result"))
        );
        register("GString",
                category("Strings"),
                List.of(anyType.optional("v0"), anyType.optional("v1"), anyType.optional("v2")),
                List.of(stringType.output("result"))
        );
    }

    @Override
    public void renderNodeBody(Node node, NodeEditorInstance<?> editor) {
        switch (node.name) {
            case "StringLiteral" -> {
                var buf = getBuffer(node, "value", "");
                ImGui.setNextItemWidth(120F);
                if (ImGui.inputText("##val-" + node.id, buf)) {
                    setNodeData(node, "value", buf.get());
                }
            }
            case "NumberLiteral" -> {
                var buf = getBuffer(node, "value", "0");
                ImGui.setNextItemWidth(80F);
                if (ImGui.inputText("##val-" + node.id, buf)) {
                    setNodeData(node, "value", buf.get());
                }
            }
            case "BoolLiteral" -> {
                String current = getNodeData(node, "value", "true");
                boolean bVal = Boolean.parseBoolean(current);
                if (ImGui.checkbox("##val-" + node.id, bVal)) {
                    setNodeData(node, "value", String.valueOf(!bVal));
                }
            }
            case "GString" -> {
                var buf = getBuffer(node, "template", "\"Hello, ${v0}!\"");
                ImGui.setNextItemWidth(150F);
                if (ImGui.inputText("##tmpl-" + node.id, buf)) {
                    setNodeData(node, "template", buf.get());
                }
            }
            case "DeclareVar", "SetVar", "GetVar" -> {
                var buf = getBuffer(node, "varName", "myVar");
                ImGui.setNextItemWidth(100F);
                if (ImGui.inputText("##var-" + node.id, buf)) {
                    setNodeData(node, "varName", buf.get());
                }
            }
            case "MethodCall" -> {
                var buf = getBuffer(node, "methodName", "myMethod");
                ImGui.setNextItemWidth(120F);
                if (ImGui.inputText("##method-" + node.id, buf)) {
                    setNodeData(node, "methodName", buf.get());
                }
            }
            case "ClosureCall" -> {
                var buf = getBuffer(node, "closureName", "myClosure");
                ImGui.setNextItemWidth(120F);
                if (ImGui.inputText("##closure-" + node.id, buf)) {
                    setNodeData(node, "closureName", buf.get());
                }
            }
        }
    }

    @Override
    public String generateCode(NodeEditorInstance<?> editor) {
        var sb = new StringBuilder();

        if (!editor.root.outputPins.isEmpty()) {
            emitExecChain(editor, editor.root.outputPins.getFirst(), sb, 0);
        } else {
            sb.append("// Connect nodes to Script Start to generate code.\n");
        }

        return sb.toString();
    }

    @Override
    protected String emitExpression(NodeEditorInstance<?> editor, Node node) {
        return switch (node.name) {
            case "StringLiteral" -> "\"" + getNodeData(node, "value", "").replace("\"", "\\\"") + "\"";
            case "NumberLiteral" -> getNodeData(node, "value", "0");
            case "BoolLiteral" -> getNodeData(node, "value", "true");
            case "NullLiteral" -> "null";
            case "GString" -> getNodeData(node, "template", "\"\"");

            case "GetVar", "DeclareVar" -> getNodeData(node, "varName", "myVar");

            case "ListLiteral" -> {
                var items = new ArrayList<String>();
                for (int i = 0; i < 3; i++) {
                    String item = resolveInput(editor, node, i, null);
                    if (item != null) items.add(item);
                }
                yield "[" + String.join(", ", items) + "]";
            }

            case "Add" -> binary(editor, node, "+");
            case "Subtract" -> binary(editor, node, "-");
            case "Multiply" -> binary(editor, node, "*");
            case "Divide" -> binary(editor, node, "/");
            case "Modulo" -> binary(editor, node, "%");
            case "Power" -> binary(editor, node, "**");
            case "Negate" -> "-(" + resolveInput(editor, node, 0, "0") + ")";

            case "Equals" -> binary(editor, node, "==");
            case "NotEquals" -> binary(editor, node, "!=");
            case "GreaterThan" -> binary(editor, node, ">");
            case "LessThan" -> binary(editor, node, "<");

            case "And" -> binary(editor, node, "&&");
            case "Or" -> binary(editor, node, "||");
            case "Not" -> "!(" + resolveInput(editor, node, 0, "false") + ")";

            case "Concat" -> resolveInput(editor, node, 0, "\"\"") + " + " + resolveInput(editor, node, 1, "\"\"");

            case "MethodCall" -> buildMethodCallExpr(editor, node);

            default -> node.name.toLowerCase(Locale.ROOT) + "_" + node.id;
        };
    }

    /**
     * Follows an exec-output pin to the next statement node and emits it,
     * then recursively follows that node's exec output(s).
     *
     * @param editor     The editor instance.
     * @param execOutPin The exec output pin to follow.
     * @param sb         The output buffer.
     * @param depth      Current indentation depth.
     */
    private void emitExecChain(NodeEditorInstance<?> editor, NodePinInfo execOutPin, StringBuilder sb, int depth) {
        for (var pin : editor.pins.values()) {
            if (pin.inputLink == execOutPin) {
                emitStatement(editor, pin.node, sb, depth);
                return;
            }
        }
    }

    /**
     * Emits a single statement node and follows its exec output(s).
     *
     * @param editor The editor instance.
     * @param node   The statement node to emit.
     * @param sb     The output buffer.
     * @param depth  Current indentation depth.
     */
    private void emitStatement(NodeEditorInstance<?> editor, Node node, StringBuilder sb, int depth) {
        if (node == editor.root) return;
        String indent = "    ".repeat(depth);

        switch (node.name) {
            case "Print" -> {
                sb.append(indent).append("print(").append(resolveInput(editor, node, 1, "null")).append(")\n");
                followExecOut(editor, node, 0, sb, depth);
            }
            case "PrintLine" -> {
                sb.append(indent).append("println(").append(resolveInput(editor, node, 1, "null")).append(")\n");
                followExecOut(editor, node, 0, sb, depth);
            }
            case "DeclareVar" -> {
                String varName = getNodeData(node, "varName", "myVar");
                sb.append(indent).append("def ").append(varName).append(" = ").append(resolveInput(editor, node, 1, "null")).append("\n");
                followExecOut(editor, node, 0, sb, depth);
            }
            case "SetVar" -> {
                String varName = getNodeData(node, "varName", "myVar");
                sb.append(indent).append(varName).append(" = ").append(resolveInput(editor, node, 1, "null")).append("\n");
                followExecOut(editor, node, 0, sb, depth);
            }
            case "If" -> {
                sb.append(indent).append("if (").append(resolveInput(editor, node, 1, "false")).append(") {\n");
                followExecOut(editor, node, 0, sb, depth + 1);
                sb.append(indent).append("} else {\n");
                followExecOut(editor, node, 1, sb, depth + 1);
                sb.append(indent).append("}\n");
            }
            case "While" -> {
                sb.append(indent).append("while (").append(resolveInput(editor, node, 1, "false")).append(") {\n");
                followExecOut(editor, node, 0, sb, depth + 1);
                sb.append(indent).append("}\n");
                followExecOut(editor, node, 1, sb, depth);
            }
            case "ForEach" -> {
                String itemVar = getNodeData(node, "varName", "item");
                sb.append(indent).append(resolveInput(editor, node, 1, "[]")).append(".each { ").append(itemVar).append(" ->\n");
                followExecOut(editor, node, 0, sb, depth + 1);
                sb.append(indent).append("}\n");
                followExecOut(editor, node, 2, sb, depth);
            }
            case "MethodCall" -> {
                sb.append(indent).append(buildMethodCallExpr(editor, node)).append("\n");
                followExecOut(editor, node, 0, sb, depth);
            }
            case "Return" -> {
                String val = resolveInput(editor, node, 1, "");
                sb.append(indent).append("return").append(val.isBlank() ? "" : " " + val).append("\n");
            }
            default -> {
                sb.append(indent).append("// Unknown node: ").append(node.name).append("\n");
                followExecOut(editor, node, 0, sb, depth);
            }
        }
    }

    /**
     * Emits a binary infix expression for a node with two data inputs at indices 0 and 1.
     *
     * @param editor The editor instance.
     * @param node   The operator node.
     * @param op     The infix operator string, e.g. {@code "+"} or {@code "=="}.
     * @return The formatted expression string.
     */
    private String binary(NodeEditorInstance<?> editor, Node node, String op) {
        return "(" + resolveInput(editor, node, 0, "0") + " " + op + " " + resolveInput(editor, node, 1, "0") + ")";
    }

    /**
     * Builds a method call expression string from a MethodCall node's data and connected inputs.
     *
     * @param editor The editor instance.
     * @param node   The MethodCall node.
     * @return The formatted call expression, e.g. {@code "target.method(arg0, arg1)"}.
     */
    private String buildMethodCallExpr(NodeEditorInstance<?> editor, Node node) {
        String methodName = getNodeData(node, "methodName", "myMethod");
        String target = resolveInput(editor, node, 1, null);
        var args = new ArrayList<String>();
        for (int i = 2; i <= 4; i++) {
            String arg = resolveInput(editor, node, i, null);
            if (arg != null) args.add(arg);
        }
        return (target != null ? target + "." : "") + methodName + "(" + String.join(", ", args) + ")";
    }

    /**
     * Follows the Nth exec-output pin of a node and continues emitting the chain.
     *
     * <p>Exec output pins are those whose type is {@link #execType}, counted in order
     * among the node's output pins.
     *
     * @param editor       The editor instance.
     * @param node         The node whose exec output to follow.
     * @param execOutIndex Zero-based index among exec-type output pins.
     * @param sb           The output buffer.
     * @param depth        Current indentation depth.
     */
    private void followExecOut(NodeEditorInstance<?> editor, Node node, int execOutIndex, StringBuilder sb, int depth) {
        int count = 0;
        for (var outPin : node.outputPins) {
            if (outPin.pin.type() == execType) {
                if (count == execOutIndex) {
                    emitExecChain(editor, outPin, sb, depth);
                    return;
                }
                count++;
            }
        }
    }

    /**
     * Gets or creates an {@link ImString} buffer for a node and field combination,
     * seeding it from the stored node data if the field already has a value.
     *
     * @param node         The node the buffer belongs to.
     * @param field        The field key.
     * @param defaultValue The initial string if no value is stored yet.
     * @return The {@link ImString} buffer for that node/field pair.
     */
    private ImString getBuffer(Node node, String field, String defaultValue) {
        return inputBuffers.computeIfAbsent(
                node.id + ":" + field,
                k -> new ImString(getNodeData(node, field, defaultValue), 256)
        );
    }
}