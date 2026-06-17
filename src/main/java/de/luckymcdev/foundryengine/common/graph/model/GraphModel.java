package de.luckymcdev.foundryengine.common.graph.model;

import net.minecraft.resources.Identifier;

import java.util.*;

public final class GraphModel {
    private final UUID id;
    private final Identifier domain;
    private final Map<UUID, NodeModel> nodes;
    private final Map<UUID, LinkModel> links;
    private final Map<UUID, PinModel> allPins;
    private final Map<UUID, UUID> pinToNode;

    private GraphModel(UUID id, Identifier domain,
                       Map<UUID, NodeModel> nodes,
                       Map<UUID, LinkModel> links,
                       Map<UUID, PinModel> allPins,
                       Map<UUID, UUID> pinToNode) {
        this.id = id;
        this.domain = domain;
        this.nodes = Collections.unmodifiableMap(new LinkedHashMap<>(nodes));
        this.links = Collections.unmodifiableMap(new LinkedHashMap<>(links));
        this.allPins = Collections.unmodifiableMap(new LinkedHashMap<>(allPins));
        this.pinToNode = Collections.unmodifiableMap(new LinkedHashMap<>(pinToNode));
    }

    public GraphModel(Identifier domain) {
        this(UUID.randomUUID(), domain, Map.of(), Map.of(), Map.of(), Map.of());
    }

    public GraphModel(UUID id, Identifier domain,
                      Collection<NodeModel> nodes,
                      Collection<LinkModel> links) {
        this(id, domain, toNodeMap(nodes), toLinkMap(links), buildPinIndex(nodes), buildNodeIndex(nodes));
    }

    public UUID id() { return id; }
    public Identifier domain() { return domain; }
    public Map<UUID, NodeModel> nodes() { return nodes; }
    public Map<UUID, LinkModel> links() { return links; }
    public PinModel pin(UUID pinId) { return allPins.get(pinId); }
    public NodeModel nodeForPin(UUID pinId) {
        UUID nid = pinToNode.get(pinId);
        return nid != null ? nodes.get(nid) : null;
    }

    public List<LinkModel> linksFrom(UUID outputPinId) {
        return links.values().stream()
                .filter(l -> l.fromPin().equals(outputPinId))
                .toList();
    }

    public List<LinkModel> linksTo(UUID inputPinId) {
        return links.values().stream()
                .filter(l -> l.toPin().equals(inputPinId))
                .toList();
    }

    public LinkModel linkTo(UUID inputPinId) {
        return links.values().stream()
                .filter(l -> l.toPin().equals(inputPinId))
                .findFirst().orElse(null);
    }

    public GraphModel withNode(NodeModel node) {
        var newNodes = new LinkedHashMap<>(nodes);
        newNodes.put(node.id(), node);
        return rebuild(newNodes, this.links);
    }

    public GraphModel withoutNode(UUID nodeId) {
        var newNodes = new LinkedHashMap<>(nodes);
        newNodes.remove(nodeId);
        var newLinks = new LinkedHashMap<>(links);
        var removedNode = nodes.get(nodeId);
        if (removedNode != null) {
            var removedPinIds = removedNode.pins().stream()
                    .map(PinModel::id)
                    .collect(java.util.stream.Collectors.toSet());
            newLinks.values().removeIf(l ->
                    removedPinIds.contains(l.fromPin()) || removedPinIds.contains(l.toPin()));
        }
        return rebuild(newNodes, newLinks);
    }

    public GraphModel withLink(LinkModel link) {
        var newLinks = new LinkedHashMap<>(links);
        // Remove any existing link to the same input pin
        newLinks.values().removeIf(l -> l.toPin().equals(link.toPin()));
        newLinks.put(link.id(), link);
        return rebuild(this.nodes, newLinks);
    }

    public GraphModel withoutLink(UUID linkId) {
        var newLinks = new LinkedHashMap<>(links);
        newLinks.remove(linkId);
        return rebuild(this.nodes, newLinks);
    }

    private static GraphModel rebuild(Map<UUID, NodeModel> newNodes, Map<UUID, LinkModel> newLinks) {
        return new GraphModel(
                UUID.randomUUID(), // new revision
                null,              // domain preserved from caller
                newNodes,
                newLinks,
                buildPinIndex(newNodes.values()),
                buildNodeIndex(newNodes.values())
        );
    }

    private static Map<UUID, NodeModel> toNodeMap(Collection<NodeModel> nodes) {
        var m = new LinkedHashMap<UUID, NodeModel>(nodes.size());
        for (var n : nodes) m.put(n.id(), n);
        return m;
    }

    private static Map<UUID, LinkModel> toLinkMap(Collection<LinkModel> links) {
        var m = new LinkedHashMap<UUID, LinkModel>(links.size());
        for (var l : links) m.put(l.id(), l);
        return m;
    }

    private static Map<UUID, PinModel> buildPinIndex(Collection<NodeModel> nodes) {
        var m = new LinkedHashMap<UUID, PinModel>();
        for (var n : nodes) {
            for (var p : n.pins()) m.put(p.id(), p);
        }
        return m;
    }

    private static Map<UUID, UUID> buildNodeIndex(Collection<NodeModel> nodes) {
        var m = new LinkedHashMap<UUID, UUID>();
        for (var n : nodes) {
            for (var p : n.pins()) m.put(p.id(), n.id());
        }
        return m;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GraphModel that)) return false;
        return nodes.equals(that.nodes) && links.equals(that.links);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodes, links);
    }
}
