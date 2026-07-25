package de.luckymcdev.foundryengine.client.editor.panel.tools;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.panel.editor.EditorPanel;
import de.luckymcdev.foundryengine.client.editor.panel.files.CodeEditor;
import de.luckymcdev.foundryengine.client.ide.WorkspaceState;
import de.luckymcdev.foundryengine.client.imgui.ImGraphicsExtractor;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.script.ScriptConfig;
import imgui.ImGui;
import imgui.flag.ImGuiSelectableFlags;
import imgui.flag.ImGuiTreeNodeFlags;
import net.minecraft.resources.Identifier;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;

import java.util.ArrayList;
import java.util.List;

public class OutlinePanel extends EditorPanel {
	public static final OutlinePanel INSTANCE = new OutlinePanel();

	private OutlinePanel() {
		super(new Builder(Common.id("outline"))
			.icon(ImIcons.LIST)
			.category(PanelCategory.TOOLS)
			.menuBar(true));
	}

	private static String extensionFrom(String fileName) {
		if (fileName == null || !fileName.contains(".")) {
			return "";
		}
		return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
	}

	private static List<OutlineNode> parseOutline(String code) {
		List<OutlineNode> nodes = new ArrayList<>();

		CompilerConfiguration config = new CompilerConfiguration();
		SourceUnit source = new SourceUnit("OutlineParser", code, config, null, null);

		try {
			CompilationUnit cu = new CompilationUnit(config);
			cu.addSource(source);
			cu.compile(Phases.CANONICALIZATION);
		} catch (Exception ignored) {
		}

		var module = source.getAST();
		if (module == null) {
			return nodes;
		}

		for (var cls : module.getClasses()) {
			nodes.add(new OutlineNode(cls.getName(), OutlineKind.CLASS,
				cls.getLineNumber(), cls.getLastLineNumber()));

			for (var method : cls.getMethods()) {
				nodes.add(new OutlineNode("  " + method.getName(), OutlineKind.METHOD,
					method.getLineNumber(), method.getLastLineNumber()));
			}

			for (var field : cls.getFields()) {
				nodes.add(new OutlineNode("  " + field.getName(), OutlineKind.FIELD,
					field.getLineNumber(), field.getLastLineNumber()));
			}

			for (var prop : cls.getProperties()) {
				nodes.add(new OutlineNode("  " + prop.getName(), OutlineKind.PROPERTY,
					prop.getLineNumber(), prop.getLastLineNumber()));
			}
		}

		if (nodes.isEmpty()) {
			var stmts = module.getStatementBlock().getStatements();
			for (var stmt : stmts) {
				if (stmt instanceof org.codehaus.groovy.ast.stmt.ExpressionStatement es) {
					var expr = es.getExpression();
					if (expr instanceof org.codehaus.groovy.ast.expr.MethodCallExpression mce) {
						String name = mce.getMethodAsString();
						if (name != null && !name.isEmpty()) {
							nodes.add(new OutlineNode(name, OutlineKind.EXPRESSION,
								es.getLineNumber(), es.getLastLineNumber()));
						}
					}
				}
			}
		}

		return nodes;
	}

	private static void collectNodes(org.codehaus.groovy.ast.stmt.Statement stmt, List<OutlineNode> nodes, int depth) {
		if (stmt == null) {
			return;
		}

		if (stmt instanceof org.codehaus.groovy.ast.stmt.ExpressionStatement es) {
			var expr = es.getExpression();
			if (expr instanceof org.codehaus.groovy.ast.expr.MethodCallExpression mce) {
				String name = mce.getMethodAsString();
				if (name != null && !name.isEmpty() && depth == 0) {
					nodes.add(new OutlineNode(name, OutlineKind.EXPRESSION,
						es.getLineNumber(), es.getLastLineNumber()));
				}
			} else if (expr instanceof org.codehaus.groovy.ast.expr.ClosureExpression) {
				if (depth == 0) {
					nodes.add(new OutlineNode("closure", OutlineKind.EXPRESSION,
						es.getLineNumber(), es.getLastLineNumber()));
				}
			}
		}

		if (stmt instanceof org.codehaus.groovy.ast.stmt.BlockStatement bs) {
			for (var s : bs.getStatements()) {
				collectNodes(s, nodes, depth + 1);
			}
		}
	}

	@Override
	public void content(ImGraphicsExtractor g) {
		Identifier activeId = Client.getWorkspaceState().getActiveBufferId();
		if (activeId == null) {
			ImGui.textDisabled("No active editor");
			return;
		}

		WorkspaceState.Buffer buffer = Client.getWorkspaceState().getBuffer(activeId);
		if (buffer == null) {
			ImGui.textDisabled("No active editor");
			return;
		}

		String code = Client.getWorkspaceState().getBufferContent(activeId);
		if (code == null || code.isBlank()) {
			ImGui.textDisabled("Empty buffer");
			return;
		}

		String filePath = buffer.filePath();
		String ext = extensionFrom(filePath);
		if (ScriptConfig.fileExtensions().stream().noneMatch(ext::equals)) {
			ImGui.textDisabled("Outline not available for this file type");
			return;
		}

		List<OutlineNode> nodes = parseOutline(code);
		if (nodes.isEmpty()) {
			ImGui.textDisabled("No symbols found");
			return;
		}

		renderNodes(nodes, activeId);
	}

	private void renderNodes(List<OutlineNode> nodes, Identifier bufferId) {
		for (var node : nodes) {
			int flags = ImGuiTreeNodeFlags.Leaf | ImGuiTreeNodeFlags.NoTreePushOnOpen
				| ImGuiTreeNodeFlags.SpanAvailWidth;

			String icon = switch (node.kind) {
				case CLASS -> ImIcons.CUBE + "";
				case METHOD -> ImIcons.EDIT + "";
				case FIELD, PROPERTY -> ImIcons.CODE + "";
				case EXPRESSION -> ImIcons.CHEVRON_RIGHT + "";
			};

			if (ImGui.selectable(icon + " " + node.label + "##outline_" + bufferId + "_" + node.line,
				false, ImGuiSelectableFlags.SpanAllColumns)) {
				navigateToLine(bufferId, node.line);
			}
		}
	}

	private void navigateToLine(Identifier bufferId, int line) {
		var panel = Client.getEditorManager().getPanel(bufferId);
		if (panel instanceof CodeEditor editor) {
			editor.getTextEditor().setCursor(Math.max(0, line - 1), 0);
			editor.open();
		}
	}

	private enum OutlineKind {
		CLASS, METHOD, FIELD, PROPERTY, EXPRESSION
	}

	private record OutlineNode(String label, OutlineKind kind, int line, int lastLine) {
	}
}