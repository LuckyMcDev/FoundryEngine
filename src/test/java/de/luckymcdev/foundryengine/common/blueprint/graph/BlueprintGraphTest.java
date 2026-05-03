package de.luckymcdev.foundryengine.common.blueprint.graph;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BlueprintGraphTest {

    private BlueprintGraph graph;

    @BeforeEach
    void setUp() {
        graph = new BlueprintGraph();
    }

    private NodePin inputPin(String label) {
        return new NodePin(BlueprintTypes.ANY, label, NodePinConnectionType.REQUIRED_INPUT, NodePinShape.CIRCLE);
    }

    private NodePin outputPin(String label) {
        return new NodePin(BlueprintTypes.ANY, label, NodePinConnectionType.OUTPUT, NodePinShape.CIRCLE);
    }

    @Test
    void newGraph_IsEmpty() {
        assertTrue(graph.nodes.isEmpty());
        assertTrue(graph.pins.isEmpty());
        assertEquals(0, graph.lastId);
    }

    @Test
    void nextId_Increments() {
        int id1 = graph.nextId();
        int id2 = graph.nextId();
        assertEquals(1, id1);
        assertEquals(2, id2);
    }

    @Test
    void addNode_AssignsId() {
        BlueprintNode node = new BlueprintNode("Test", List.of());
        assertEquals(0, node.id);
        graph.addNode(node);
        assertEquals(1, node.id);
        assertEquals(1, graph.lastId);
    }

    @Test
    void addNode_WithExistingId_KeepsId() {
        BlueprintNode node = new BlueprintNode("Test", List.of());
        node.id = 42;
        graph.addNode(node);
        assertEquals(42, node.id);
        assertEquals(42, graph.lastId);
    }

    @Test
    void addNode_WithExistingId_UpdatesLastId() {
        BlueprintNode node = new BlueprintNode("Test", List.of());
        node.id = 42;
        graph.addNode(node);
        graph.resetLastId();
        assertEquals(42, graph.lastId);
    }

    @Test
    void addNode_AddsToNodesMap() {
        BlueprintNode node = new BlueprintNode("Test", List.of());
        graph.addNode(node);
        assertEquals(node, graph.nodes.get(node.id));
    }

    @Test
    void addNode_WithInputPins_AssignsPinIds() {
        NodePin pin = inputPin("input");
        BlueprintNode node = new BlueprintNode("Test", List.of(pin));
        graph.addNode(node);

        assertEquals(2, graph.lastId);
        assertEquals(2, node.inputPins.get(0).id);
        assertEquals(node.inputPins.get(0), graph.pins.get(2));
    }

    @Test
    void addNode_WithOutputPins_AssignsPinIds() {
        NodePin pin = outputPin("output");
        BlueprintNode node = new BlueprintNode("Test", List.of(pin));
        graph.addNode(node);

        assertEquals(2, graph.lastId);
        assertEquals(2, node.outputPins.get(0).id);
        assertEquals(node.outputPins.get(0), graph.pins.get(2));
    }

    @Test
    void removeNode_RemovesFromNodes() {
        BlueprintNode node = new BlueprintNode("Test", List.of());
        graph.addNode(node);
        graph.removeNode(node);
        assertTrue(graph.nodes.isEmpty());
    }

    @Test
    void removeNode_RemovesPins() {
        NodePin pin = outputPin("output");
        BlueprintNode node = new BlueprintNode("Test", List.of(pin));
        graph.addNode(node);
        int pinId = node.outputPins.get(0).id;

        graph.removeNode(node);
        assertNull(graph.pins.get(pinId));
    }

    @Test
    void removeNode_ClearsInputLinks() {
        NodePin inputPinDef = inputPin("in");
        NodePin outputPinDef = outputPin("out");
        BlueprintNode node1 = new BlueprintNode("Node1", List.of(outputPinDef));
        BlueprintNode node2 = new BlueprintNode("Node2", List.of(inputPinDef));

        graph.addNode(node1);
        graph.addNode(node2);

        NodePinInfo outputPinInfo = node1.outputPin("out");
        NodePinInfo inputPinInfo = node2.inputPin("in");
        inputPinInfo.inputLink = outputPinInfo;

        graph.removeNode(node1);
        assertNull(inputPinInfo.inputLink);
        assertFalse(inputPinInfo.inputLinkSelected);
    }

    @Test
    void clear_RemovesAll() {
        BlueprintNode node = new BlueprintNode("Test", List.of());
        graph.addNode(node);
        graph.clear();

        assertTrue(graph.nodes.isEmpty());
        assertTrue(graph.pins.isEmpty());
    }

    @Test
    void getConnectedInputPin_FindsConnection() {
        NodePin outputPinDef = outputPin("out");
        NodePin inputPinDef = inputPin("in");
        BlueprintNode node1 = new BlueprintNode("Node1", List.of(outputPinDef));
        BlueprintNode node2 = new BlueprintNode("Node2", List.of(inputPinDef));

        graph.addNode(node1);
        graph.addNode(node2);

        NodePinInfo outputPinInfo = node1.outputPin("out");
        NodePinInfo inputPinInfo = node2.inputPin("in");
        inputPinInfo.inputLink = outputPinInfo;

        NodePinInfo result = graph.getConnectedInputPin(outputPinInfo);
        assertSame(inputPinInfo, result);
    }

    @Test
    void getConnectedInputPin_NoConnection_ReturnsNull() {
        NodePin outputPinDef = outputPin("out");
        BlueprintNode node = new BlueprintNode("Node", List.of(outputPinDef));
        graph.addNode(node);

        NodePinInfo outputPinInfo = node.outputPin("out");
        assertNull(graph.getConnectedInputPin(outputPinInfo));
    }

    @Test
    void resetLastId_SetsToMaxId() {
        BlueprintNode node1 = new BlueprintNode("A", List.of());
        node1.id = 10;
        BlueprintNode node2 = new BlueprintNode("B", List.of());
        node2.id = 5;

        graph.nodes.put(node1.id, node1);
        graph.nodes.put(node2.id, node2);
        graph.lastId = 0;

        graph.resetLastId();
        assertEquals(10, graph.lastId);
    }

    @Test
    void resetLastId_IncludesPins() {
        NodePinInfo pin = new NodePinInfo(new BlueprintNode("Test", List.of(inputPin("p"))), inputPin("p"));
        pin.id = 50;
        graph.pins.put(pin.id, pin);
        graph.lastId = 0;

        graph.resetLastId();
        assertEquals(50, graph.lastId);
    }

    @Test
    void addNode_PositionAtCursorFlag_DoesNotAffectGraph() {
        BlueprintNode node = new BlueprintNode("Test", List.of());
        graph.addNode(node, true);
        assertEquals(1, graph.nodes.size());
    }

    @Test
    void multipleNodes_GetUniqueIds() {
        BlueprintNode node1 = new BlueprintNode("A", List.of());
        BlueprintNode node2 = new BlueprintNode("B", List.of());
        BlueprintNode node3 = new BlueprintNode("C", List.of());

        graph.addNode(node1);
        graph.addNode(node2);
        graph.addNode(node3);

        assertEquals(1, node1.id);
        assertEquals(2, node2.id);
        assertEquals(3, node3.id);
    }
}
