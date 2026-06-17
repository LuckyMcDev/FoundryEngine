package de.luckymcdev.foundryengine.client.editor.panel;

import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.panel.editor.EditorPanel;
import de.luckymcdev.foundryengine.client.imgui.ImGuiUtils;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.graph.ScriptDomain;
import de.luckymcdev.foundryengine.common.graph.ScriptRuntime;
import de.luckymcdev.foundryengine.common.graph.domain.GraphDomain;
import de.luckymcdev.foundryengine.common.graph.script.ScriptNodes;
import de.luckymcdev.foundryengine.common.graph.model.GraphModel;
import de.luckymcdev.foundryengine.common.graph.model.LinkModel;
import de.luckymcdev.foundryengine.common.graph.model.NodeModel;
import de.luckymcdev.foundryengine.common.graph.model.PinDirection;
import de.luckymcdev.foundryengine.common.graph.model.PinModel;
import de.luckymcdev.foundryengine.common.graph.registry.NodeDefinition;
import de.luckymcdev.foundryengine.common.graph.registry.NodeRegistry;
import de.luckymcdev.foundryengine.common.graph.serial.GraphSerializer;
import de.luckymcdev.foundryengine.common.graph.type.PinType;
import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesMiniMapLocation;
import imgui.type.ImInt;
import imgui.type.ImString;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class GraphEditorPanel extends EditorPanel {
    private static final int NODE_PALETTE_WIDTH = 180;
    private static final Path GRAPH_DIR = Common.DIRECTORY.resolve("graphs");

    private GraphModel graph;
    private GraphDomain domain;
    private final ImInt newLinkSrc = new ImInt();
    private final ImInt newLinkDst = new ImInt();
    private final ImInt droppedLinkPin = new ImInt();
    private String statusMsg = "";
    private String fileName = "untitled";

    private ScriptRuntime scriptRuntime;
    private boolean running;

    private static boolean nodesRegistered;

    public GraphEditorPanel() {
        super(Common.id("graph_editor"), "Graph Editor", ImIcons.FA.FA_BLUETOOTH, PanelCategory.EDITOR);
        this.domain = new ScriptDomain();
        this.graph = new GraphModel(domain.id());
        if (!nodesRegistered) {
            ScriptNodes.register((ScriptDomain) domain);
            nodesRegistered = true;
        }
    }

    @Override
    public void content() {
        beginContent();
        try {
            editorToolbar();
            ImGui.sameLine();
            domainCombo();

            ImGui.separator();

            float availX = ImGui.getContentRegionAvailX();
            if (availX > NODE_PALETTE_WIDTH * 2) {
                ImGui.beginChild("##palette", NODE_PALETTE_WIDTH, 0, true);
                renderPalette();
                ImGui.endChild();
                ImGui.sameLine();
            }

            ImGui.beginChild("##canvas", 0, 0, false);
            renderEditor();
            ImGui.endChild();

            renderStatusLine();
        } finally {
            endContent();
        }
    }

    private void editorToolbar() {
        if (ImGui.button("New")) {
            stopRuntime();
            graph = new GraphModel(domain.id());
            setStatus("New graph created");
        }
        ImGui.sameLine();
        ImGui.sameLine();
        ImGui.text("Name:");
        ImGui.sameLine();
        var fileNameStr = new ImString(fileName, 200);
        if (ImGui.inputText("##filename", fileNameStr)) {
            fileName = fileNameStr.get().trim();
        }
        ImGui.sameLine();
        if (ImGui.button("Load")) {
            loadGraph(fileName);
        }
        ImGui.sameLine();
        if (ImGui.button("Save")) {
            saveGraph(fileName);
        }
        ImGui.sameLine();
        if (ImGui.button("Validate")) {
            var errors = domain.validate(graph);
            if (errors.isEmpty()) {
                setStatus("Graph is valid");
            } else {
                setStatus("Errors: " + String.join("; ", errors));
            }
        }
        ImGui.sameLine();
        if (domain instanceof ScriptDomain) {
            if (running) {
                if (ImGui.button("Stop")) {
                    stopRuntime();
                }
            } else {
                if (ImGui.button("Run")) {
                    startRuntime();
                }
            }
        }
    }

    private void saveGraph(String name) {
        try {
            Files.createDirectories(GRAPH_DIR);
            var path = GRAPH_DIR.resolve(name + ".fgraph");
            var json = GraphSerializer.toJson(graph);
            Files.writeString(path, json);
            setStatus("Saved: " + name + ".fgraph");
        } catch (Exception e) {
            setStatus("Save failed: " + e.getMessage());
        }
    }

    private void loadGraph(String name) {
        try {
            var path = GRAPH_DIR.resolve(name + ".fgraph");
            if (!Files.exists(path)) {
                setStatus("File not found: " + name + ".fgraph");
                return;
            }
            var json = Files.readString(path);
            stopRuntime();
            graph = GraphSerializer.fromJson(json);
            domain = resolveDomain(graph.domain());
            setStatus("Loaded: " + name + ".fgraph");
        } catch (Exception e) {
            setStatus("Load failed: " + e.getMessage());
        }
    }

    private static GraphDomain resolveDomain(Identifier domainId) {
        if (ScriptDomain.ID.equals(domainId)) return new ScriptDomain();
        return new ScriptDomain();
    }

    private void startRuntime() {
        var sd = (ScriptDomain) domain;
        var errors = domain.validate(graph);
        if (!errors.isEmpty()) {
            setStatus("Cannot run: " + String.join("; ", errors));
            return;
        }
        scriptRuntime = new ScriptRuntime();
        scriptRuntime.compile(graph, sd);
        running = true;
        setStatus("Runtime started");
    }

    private void stopRuntime() {
        if (scriptRuntime != null) {
            scriptRuntime.dispose();
            scriptRuntime = null;
        }
        running = false;
        setStatus("Runtime stopped");
    }

    private void domainCombo() {
        var domains = List.<GraphDomain>of(new ScriptDomain());
        var names = domains.stream().map(GraphDomain::displayName).toList();
        int current = 0;
        for (int i = 0; i < domains.size(); i++) {
            if (domains.get(i).id().equals(domain.id())) {
                current = i;
                break;
            }
        }

        if (ImGui.beginCombo("##domain", domain.displayName())) {
            for (int i = 0; i < names.size(); i++) {
                if (ImGui.selectable(names.get(i), i == current)) {
                    if (i != current) {
                        stopRuntime();
                        domain = domains.get(i);
                        graph = new GraphModel(domain.id());
                        setStatus("Switched to " + names.get(i));
                    }
                }
            }
            ImGui.endCombo();
        }
    }

    private void renderPalette() {
        ImGui.text("Nodes");
        ImGui.separator();

        var allDefs = NodeRegistry.INSTANCE.all();
        if (allDefs.isEmpty()) {
            ImGui.textDisabled("No node types registered");
            return;
        }

        var categories = allDefs.stream()
                .collect(Collectors.groupingBy(NodeDefinition::category, LinkedHashMap::new, Collectors.toList()));

        for (var entry : categories.entrySet()) {
            if (ImGui.collapsingHeader(entry.getKey())) {
                for (var def : entry.getValue()) {
                    if (ImGui.selectable(def.displayName())) {
                        addNodeFromDefinition(def);
                    }
                }
            }
        }
    }

    private void addNodeFromDefinition(NodeDefinition def) {
        var pins = new ArrayList<PinModel>();
        int index = 0;
        for (var input : def.inputs()) {
            pins.add(new PinModel(UUID.randomUUID(), input.type(), input.name(), PinDirection.INPUT, index++));
        }
        for (var output : def.outputs()) {
            pins.add(new PinModel(UUID.randomUUID(), output.type(), output.name(), PinDirection.OUTPUT, index++));
        }

        var node = new NodeModel(
                UUID.randomUUID(),
                def.id(),
                100 + (float) (Math.random() * 400),
                100 + (float) (Math.random() * 300),
                pins,
                def.defaultData()
        );
        graph = graph.withNode(node);
    }

    private void renderEditor() {
        ImNodes.beginNodeEditor();

        for (var node : graph.nodes().values()) {
            int editorId = editorNodeId(node.id());
            ImNodes.beginNode(editorId);
            ImNodes.beginNodeTitleBar();
            var def = NodeRegistry.INSTANCE.get(node.typeRef());
            ImGui.textUnformatted(def != null ? def.displayName() : node.typeRef().toString());
            ImNodes.endNodeTitleBar();

            ImGui.pushItemWidth(120F);

            for (var pin : node.pins()) {
                int pinId = editorPinId(pin.id());
                if (pin.direction() == PinDirection.INPUT) {
                    ImNodes.beginInputAttribute(pinId, pinShape(pin.type()));
                    ImGui.textUnformatted(pin.label());
                    ImNodes.endInputAttribute();
                } else {
                    ImNodes.beginOutputAttribute(pinId, pinShape(pin.type()));
                    ImGui.textUnformatted(pin.label());
                    ImNodes.endOutputAttribute();
                }
            }

            ImGui.popItemWidth();
            ImNodes.endNode();
        }

        for (var link : graph.links().values()) {
            int fromId = editorPinId(link.fromPin());
            int toId = editorPinId(link.toPin());
            ImNodes.link(editorLinkId(link.id()), fromId, toId);
        }

        ImNodes.miniMap(0.15F, ImNodesMiniMapLocation.TopRight);
        ImNodes.endNodeEditor();

        handleLinkCreation();
        handleLinkDeletion();
        handleNodeDeletion();
        handleContextMenu();
    }

    private void handleLinkCreation() {
        if (ImNodes.isLinkCreated(newLinkSrc, newLinkDst)) {
            UUID srcPinId = editorToRealPinId(newLinkSrc.get());
            UUID dstPinId = editorToRealPinId(newLinkDst.get());
            if (srcPinId != null && dstPinId != null) {
                var srcPin = graph.pin(srcPinId);
                var dstPin = graph.pin(dstPinId);
                if (srcPin != null && dstPin != null
                        && srcPin.direction() == PinDirection.OUTPUT
                        && dstPin.direction() == PinDirection.INPUT
                        && srcPin.type().canConnectTo(dstPin.type())) {
                    graph = graph.withLink(new LinkModel(UUID.randomUUID(), srcPinId, dstPinId));
                }
            }
        }
    }

    private void handleLinkDeletion() {
        if (ImNodes.isLinkDropped(droppedLinkPin, false)) {
            UUID pinId = editorToRealPinId(droppedLinkPin.get());
            if (pinId != null) {
                var link = graph.linkTo(pinId);
                if (link != null) {
                    graph = graph.withoutLink(link.id());
                }
            }
        }
    }

    private void handleNodeDeletion() {
        if (ImGui.getIO().getKeysDown(GLFW.GLFW_KEY_DELETE)) {
            var toRemove = new HashSet<UUID>();
            for (var node : graph.nodes().values()) {
                if (ImNodes.isNodeSelected(editorNodeId(node.id()))) {
                    toRemove.add(node.id());
                }
            }
            for (var id : toRemove) {
                graph = graph.withoutNode(id);
            }
        }
    }

    private void handleContextMenu() {
        boolean rightClick = ImGui.isMouseClicked(1);
        boolean hovered = ImNodes.isEditorHovered();
        boolean linkDropped = ImNodes.isLinkDropped(droppedLinkPin, false);

        if ((hovered && rightClick) || linkDropped) {
            ImGui.openPopup("##graph-context-menu");
        }

        if (ImGui.beginPopup("##graph-context-menu")) {
            ImGui.text("Add Node");
            ImGui.separator();

            var categories = NodeRegistry.INSTANCE.all().stream()
                    .collect(Collectors.groupingBy(NodeDefinition::category, LinkedHashMap::new, Collectors.toList()));

            for (var entry : categories.entrySet()) {
                if (ImGui.beginMenu(entry.getKey())) {
                    for (var def : entry.getValue()) {
                        if (ImGui.menuItem(def.displayName())) {
                            addNodeFromDefinition(def);
                        }
                    }
                    ImGui.endMenu();
                }
            }

            ImGui.endPopup();
        }
    }

    private void renderStatusLine() {
        var errors = domain.validate(graph);
        if (!errors.isEmpty()) {
            ImGuiUtils.redTextIf("Validation: " + errors.size() + " error(s)", true);
            for (var err : errors) {
                ImGui.textDisabled("  " + err);
            }
        } else {
            ImGui.textDisabled("Graph valid");
        }
        if (!statusMsg.isEmpty()) {
            ImGui.sameLine();
            ImGui.textUnformatted(" | " + statusMsg);
        }
    }

    private static int editorNodeId(UUID nodeId) {
        return nodeId.hashCode();
    }

    private static int editorPinId(UUID pinId) {
        return pinId.hashCode();
    }

    private static int editorLinkId(UUID linkId) {
        return linkId.hashCode();
    }

    private UUID editorToRealPinId(int editorId) {
        for (var node : graph.nodes().values()) {
            for (var pin : node.pins()) {
                if (editorPinId(pin.id()) == editorId) {
                    return pin.id();
                }
            }
        }
        return null;
    }

    private static int pinShape(PinType type) {
        return switch (type.defaultShape()) {
            case CIRCLE -> 0;
            case CIRCLE_FILLED -> 1;
            case TRIANGLE -> 2;
            case TRIANGLE_FILLED -> 3;
            case QUAD, SQUARE -> 4;
            case QUAD_FILLED, SQUARE_FILLED -> 5;
            case DIAMOND -> 2;
            case DIAMOND_FILLED -> 3;
        };
    }

    @Override
    protected void setStatus(String msg) {
        this.statusMsg = msg;
    }
}
