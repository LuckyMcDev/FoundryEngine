package de.luckymcdev.foundryengine.client.editor.panel.test;

import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.panel.editor.EditorPanel;
import de.luckymcdev.foundryengine.client.imgui.ImGraphicsExtractor;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.client.node.NodeBuilder;
import de.luckymcdev.foundryengine.client.node.NodeEditorInstance;
import de.luckymcdev.foundryengine.client.node.NodePin;
import de.luckymcdev.foundryengine.client.node.NodePinType;
import de.luckymcdev.foundryengine.client.node.NodeTypes;
import de.luckymcdev.foundryengine.common.Common;
import imgui.ImGui;
import net.minecraft.network.chat.Component;

import java.util.List;

public class NodeTestPanel extends EditorPanel {
	public static final NodeTestPanel INSTANCE = new NodeTestPanel();

	private final NodeEditorInstance<Double> editor;
	private final NodeBuilder<Double> rootEvalBuilder;

	protected NodeTestPanel() {
		super(new Builder(Common.id("node_test_panel"))
			.icon(ImIcons.BLUETOOTH)
			.category(PanelCategory.EDITOR)
		);

		NodePinType<Double> doubleType = NodeTypes.DOUBLE;

		editor = new NodeEditorInstance<>(doubleType);

		rootEvalBuilder = new NodeBuilder<>() {
			@Override
			public List<NodePin<Double>> getPins() {
				return List.of();
			}

			@Override
			public boolean render() {
				return false;
			}

			@Override
			public Double evaluate() {
				var rootInput = editor.root.inputPins.get(0);
				if (rootInput.inputLink != null) {
					return rootInput.inputLink.node.builder.evaluate();
				}
				return 0.0;
			}

			@Override
			public Component getDisplayName() {
				return Component.translatable("node.foundryengine.root");
			}
		};

		editor.rootBuilder = rootEvalBuilder;
	}

	@Override
	public void content(ImGraphicsExtractor g) {
		editor.content(g);
		ImGui.separator();
		ImGui.text("Final value: " + editor.rootBuilder.evaluate());
	}
}
