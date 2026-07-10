package de.luckymcdev.foundryengine.client.editor.panel.explorer;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.panel.Panel;
import de.luckymcdev.foundryengine.client.editor.panel.editor.EditorPanel;
import de.luckymcdev.foundryengine.client.editor.panel.files.CodeEditor;
import de.luckymcdev.foundryengine.client.editor.panel.files.TextureViewerPanel;
import de.luckymcdev.foundryengine.client.imgui.ImGraphicsExtractor;
import de.luckymcdev.foundryengine.client.imgui.ImGuiShortcut;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.network.packets.explorer.ClientBoundExplorerPacket;
import de.luckymcdev.foundryengine.common.network.packets.explorer.ServerBoundExplorerPacket;
import de.luckymcdev.foundryengine.common.util.FileEndings;
import de.luckymcdev.foundryengine.common.util.PackResourceScanner;
import de.luckymcdev.foundryengine.server.Server;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiKey;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImString;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.permissions.PermissionLevel;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ExplorerPanel extends EditorPanel {
	public static final ExplorerPanel INSTANCE = new ExplorerPanel();
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final DateTimeFormatter DATE_FMT =
		DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

	private final File rootDir = Common.DIRECTORY.toFile();
	private final ImString searchFilter = new ImString(256);

	private boolean initialized = false;
	private boolean rootReadable = true;
	private @Nullable String lastError = null;

	private @Nullable LocalTree localTree = null;
	private @Nullable RemoteTree remoteTree = null;
	private boolean remoteRequested = false;
	private boolean remoteLoading = false;
	private @Nullable ResourceTree resourceTree = null;
	private boolean resourceRequested = false;
	private boolean resourceLoading = false;

	public ExplorerPanel() {
		super(new Builder(Common.id("explorer"))
			.icon(ImIcons.FILES_O)
			.shortcut(ImGuiShortcut.empty())
			.category(PanelCategory.EDITOR_EXPLORER));
	}

	private static boolean hasServer() {
		return Client.getMc().level != null;
	}

	private static boolean isMultiplayer() {
		return Client.getMc().getCurrentServer() != null;
	}

	private static String fileNameFrom(String path) {
		return path.contains("/") ? path.substring(path.lastIndexOf('/') + 1) : path;
	}

	private static @Nullable URL scriptRootFor(File file) {
		try {
			Path path = file.toPath().toAbsolutePath();
			for (int i = 0; i < path.getNameCount(); i++) {
				if ("scripts".equals(path.getName(i).toString()) && i < path.getNameCount() - 1) {
					Path root = path.getRoot().resolve(path.subpath(0, i + 1));
					return root.toUri().toURL();
				}
			}
		} catch (Exception ignored) {
		}
		return null;
	}

	private static Identifier fileToEditorId(File file) {
		String sanitised = file.getAbsolutePath().toLowerCase().replaceAll("[^a-z0-9]", "_");
		return Common.id("editor_" + sanitised);
	}

	private static Identifier remoteFileEditorId(String relativePath) {
		String sanitised = ("remote_" + relativePath).toLowerCase().replaceAll("[^a-z0-9]", "_");
		return Common.id("editor_" + sanitised);
	}

	private static File uniqueFile(File dir, String baseName, @Nullable String extension) {
		String suffix = extension != null ? "." + extension : "";
		File candidate = new File(dir, baseName + suffix);
		int n = 1;
		while (candidate.exists()) {
			candidate = new File(dir, baseName + "_" + n++ + suffix);
		}
		return candidate;
	}

	public void receiveRemoteFileList(List<ClientBoundExplorerPacket.RemoteEntry> entries) {
		remoteTree = new RemoteTree();
		for (var entry : entries) {
			remoteTree.add(entry.relativePath(), entry.isDirectory());
		}
		remoteLoading = false;
	}

	public void receiveRemoteFileContent(String relativePath, String content) {
		Identifier editorId = remoteFileEditorId(relativePath);
		if (Client.getEditorManager().getPanel(editorId) != null) {
			return;
		}

		String fileName = fileNameFrom(relativePath);
		CodeEditor editor = new CodeEditor(editorId, Component.literal(fileName), content);
		editor.applyLanguage(fileName);
		editor.setSaveCallback((source, errors) ->
			ClientPacketDistributor.sendToServer(new ServerBoundExplorerPacket(ServerBoundExplorerPacket.Action.SAVE_FILE, relativePath, source)));
		Client.getEditorManager().register(editor);
		editor.open();
	}

	public void receiveResourceList(List<String> resourceIds) {
		if (resourceTree == null) {
			resourceTree = new ResourceTree();
		}
		for (String raw : resourceIds) {
			Identifier id = Identifier.tryParse(raw);
			if (id != null) {
				resourceTree.add(id);
			}
		}
		resourceLoading = false;
	}

	public void receiveResourceContent(String resourceId, String content) {
		Identifier id = Identifier.tryParse(resourceId);
		if (id == null) {
			return;
		}

		Identifier editorId = generateEditorId("res_" + id.getNamespace(), id.getPath());
		if (Client.getEditorManager().getPanel(editorId) != null) {
			return;
		}

		String fileName = id.getPath().contains("/") ? id.getPath().substring(id.getPath().lastIndexOf('/') + 1) : id.getPath();
		CodeEditor editor = new CodeEditor(editorId, Component.literal(id.getPath()), content);
		editor.getTextEditor().setReadOnly(true);
		editor.forceReadOnly = true;
		editor.applyLanguage(fileName);
		Client.getEditorManager().register(editor);
		editor.open();
	}

	@Override
	public void content(ImGraphicsExtractor g) {
		if (!initialized) {
			refresh();
		}
		renderBrowser();
	}

	private void refresh() {
		localTree = buildLocalTree();
		rootReadable = localTree != null;

		resourceTree = new ResourceTree();
		buildClientResources();
		resourceRequested = false;
		resourceLoading = false;

		if (hasServer()) {
			resourceRequested = true;
			resourceLoading = true;
			ClientPacketDistributor.sendToServer(new ServerBoundExplorerPacket(ServerBoundExplorerPacket.Action.REQUEST_RESOURCE_LIST, "", ""));
		}

		if (isMultiplayer()) {
			requestRemoteFileList();
		}

		initialized = true;
	}

	private void buildClientResources() {
		try {
			PackResourceScanner.scanAll(Client.getResourceManager(), PackType.CLIENT_RESOURCES,
				(id, file) -> resourceTree.add(id));
		} catch (Exception e) {
			LOGGER.debug("Failed to list client resources: {}", e.getMessage());
		}
	}

	private @Nullable LocalTree buildLocalTree() {
		File[] contents = rootDir.listFiles();
		if (contents == null) {
			return null;
		}
		Arrays.sort(contents, Comparator.comparing(File::isFile).thenComparing(f -> f.getName().toLowerCase()));
		LocalTree tree = new LocalTree();
		for (File entry : contents) {
			tree.add(entry);
		}
		return tree;
	}

	private void renderBrowser() {
		if (!requireLevelOnServer(PermissionLevel.OWNERS)) {
			return;
		}

		renderToolbar();
		renderErrorBanner();

		if (!rootReadable) {
			ImGui.textDisabled("Root directory is not accessible.");
			return;
		}

		ImGui.separator();

		if (ImGui.beginChild("##explorerTree", 0, 0, false)) {
			boolean filtering = !searchFilter.get().isBlank();
			String query = searchFilter.get().trim().toLowerCase();

			renderSection(ImGraphicsExtractor.icon(ImIcons.DESKTOP) + " Local Files", "##section_local", () -> {
				if (localTree != null) {
					if (filtering) {
						localTree.renderFiltered(query, this::renderLocalFileItem);
					} else {
						localTree.render(this::renderLocalFileNode, this::renderLocalFileItem);
					}
				}
			});

			ImGui.spacing();
			renderSection(ImGraphicsExtractor.icon(ImIcons.CUBES) + " Resources", "##section_res", () -> {
				boolean empty = resourceTree == null || resourceTree.isEmpty();
				if (empty && resourceLoading) {
					ImGui.textDisabled("Loading\u2026");
				} else if (empty) {
					ImGui.textDisabled("No resources found");
				} else if (filtering) {
					resourceTree.renderFiltered(query, this::renderResourceFile);
				} else {
					resourceTree.render(this::renderResourceFile);
				}
				if (resourceLoading) {
					ImGui.textDisabled("(loading server resources\u2026)");
				}
			});

			if (isMultiplayer()) {
				ImGui.spacing();
				renderSection(ImGraphicsExtractor.icon(ImIcons.SERVER) + " Remote Server", "##section_remote", () -> {
					if (remoteLoading) {
						ImGui.textDisabled("Loading\u2026");
					} else if (remoteTree == null) {
						if (!remoteRequested) {
							requestRemoteFileList();
						}
						ImGui.textDisabled("Fetching file list\u2026");
					} else if (filtering) {
						remoteTree.renderFiltered(query, this::renderRemoteFileItem);
					} else {
						remoteTree.render(this::renderRemoteFileItem);
					}
				});
			}
		}
		ImGui.endChild();
	}

	private void renderSection(String label, String id, Runnable body) {
		int flags = ImGuiTreeNodeFlags.SpanAvailWidth | ImGuiTreeNodeFlags.DefaultOpen | ImGuiTreeNodeFlags.Framed;
		if (ImGui.treeNodeEx(id, flags, label)) {
			body.run();
			ImGui.treePop();
		}
	}

	private void renderLocalFileNode(LocalTree.Node node) {
		boolean isOpen = ImGui.treeNodeEx("##dir_" + node.file.getPath(), ImGuiTreeNodeFlags.SpanAvailWidth, "");
		ImGui.sameLine();
		String icon = isOpen ? ImGraphicsExtractor.icon(ImIcons.FOLDER_OPEN) : ImGraphicsExtractor.icon(ImIcons.FOLDER);
		ImGui.textUnformatted(icon + " " + node.name);
		renderDirectoryContextMenu(node.file, "##ctx_dir_" + node.file.getPath());
		if (isOpen) {
			node.renderChildren(this::renderLocalFileNode, this::renderLocalFileItem);
			ImGui.treePop();
		}
	}

	private void renderLocalFileItem(File file) {
		String id = "##file_" + file.getPath();
		String fileName = file.getName();
		String icon = FileEndings.getFileIcon(fileName);
		boolean open = isFileOpen(file);

		if (open) {
			ImGui.pushStyleColor(ImGuiCol.Text, ImGui.getStyle().getColor(ImGuiCol.CheckMark));
		}
		ImGui.treeNodeEx(id,
			ImGuiTreeNodeFlags.Leaf | ImGuiTreeNodeFlags.NoTreePushOnOpen | ImGuiTreeNodeFlags.SpanAvailWidth,
			icon + " " + fileName);
		if (open) {
			ImGui.popStyleColor();
		}

		if (ImGui.isItemClicked(ImGuiMouseButton.Left)) {
			openLocalFile(file);
		}

		renderFileContextMenu(file, id + "_ctx");
		renderLocalFileTooltip(file, open);
	}

	private void renderResourceFile(Identifier id) {
		String fileName = id.getPath().substring(id.getPath().lastIndexOf('/') + 1);
		String nodeId = "##file_" + id;

		ImGui.treeNodeEx(nodeId,
			ImGuiTreeNodeFlags.Leaf | ImGuiTreeNodeFlags.NoTreePushOnOpen | ImGuiTreeNodeFlags.SpanAvailWidth,
			ImGraphicsExtractor.icon(ImIcons.FILE_CODE) + " " + fileName);

		if (ImGui.isItemClicked(ImGuiMouseButton.Left)) {
			openResource(id);
		}

		if (ImGui.beginPopupContextItem(nodeId + "_ctx")) {
			if (ImGui.menuItem(ImIcons.EDIT + "  Open")) {
				openResource(id);
			}
			ImGui.separator();
			if (ImGui.menuItem(ImIcons.COPY + "  Copy Identifier")) {
				ImGui.setClipboardText(id.toString());
			}
			ImGui.endPopup();
		}
	}

	private void renderRemoteFileItem(RemoteTree.Entry entry) {
		String id = "##rfile_" + entry.relativePath;
		String icon = FileEndings.getFileIcon(entry.name);
		boolean open = Client.getEditorManager().getPanel(remoteFileEditorId(entry.relativePath)) != null;

		if (open) {
			ImGui.pushStyleColor(ImGuiCol.Text, ImGui.getStyle().getColor(ImGuiCol.CheckMark));
		}
		ImGui.treeNodeEx(id,
			ImGuiTreeNodeFlags.Leaf | ImGuiTreeNodeFlags.NoTreePushOnOpen | ImGuiTreeNodeFlags.SpanAvailWidth,
			icon + " " + entry.name);
		if (open) {
			ImGui.popStyleColor();
		}

		if (ImGui.isItemClicked(ImGuiMouseButton.Left)) {
			openRemoteFile(entry.relativePath);
		}

		if (ImGui.isItemHovered()) {
			ImGui.beginTooltip();
			ImGui.text(entry.name);
			ImGui.separator();
			ImGui.textDisabled("Remote: " + entry.relativePath);
			if (open) {
				ImGui.separator();
				ImGui.textDisabled("Currently open in editor");
			}
			ImGui.endTooltip();
		}
	}

	private void renderToolbar() {
		ImGui.textDisabled(rootDir.getName() + "/");
		ImGui.sameLine();

		float buttonWidth = ImGui.getFrameHeight();
		float spacing = ImGui.getStyle().getItemSpacingX();
		float searchWidth = ImGui.getContentRegionAvailX() - (buttonWidth + spacing) * 2 - spacing;
		ImGui.setNextItemWidth(Math.max(searchWidth, 60.0f));
		ImGui.inputTextWithHint("##search", ImIcons.SEARCH + " Filter\u2026", searchFilter);

		if (ImGui.isItemFocused() && ImGui.isKeyPressed(ImGuiKey.Escape)) {
			searchFilter.set("");
		}

		if (!searchFilter.get().isEmpty()) {
			ImGui.sameLine();
			if (ImGui.smallButton("\u00d7##clearSearch")) {
				searchFilter.set("");
			}
		}

		ImGui.sameLine();
		if (ImGui.button(ImIcons.ROTATE_RIGHT + "##refresh", buttonWidth, 0)) {
			clearError();
			remoteTree = null;
			remoteRequested = false;
			resourceTree = new ResourceTree();
			buildClientResources();
			resourceRequested = false;
			resourceLoading = false;
			if (hasServer()) {
				resourceRequested = true;
				resourceLoading = true;
				ClientPacketDistributor.sendToServer(new ServerBoundExplorerPacket(ServerBoundExplorerPacket.Action.REQUEST_RESOURCE_LIST, "", ""));
			}
			refresh();
		}
		if (ImGui.isItemHovered()) {
			ImGui.setTooltip("Refresh  (Ctrl+R)");
		}

		if (!isMultiplayer()) {
			ImGui.sameLine();
			if (ImGui.button(ImIcons.ROTATE + "##reloadres", buttonWidth, 0)) {
				reloadResources();
			}
			if (ImGui.isItemHovered()) {
				ImGui.setTooltip("Reload all resources");
			}
		}
	}

	private void renderErrorBanner() {
		if (lastError == null) {
			return;
		}
		ImGui.pushStyleColor(ImGuiCol.Text, 1.0f, 0.35f, 0.35f, 1.0f);
		ImGui.textWrapped(ImIcons.EXCLAMATION_TRIANGLE + " " + lastError);
		ImGui.popStyleColor();
		ImGui.sameLine();
		if (ImGui.smallButton("\u00d7")) {
			lastError = null;
		}
	}

	private void renderFileContextMenu(File file, String popupId) {
		if (ImGui.beginPopupContextItem(popupId)) {
			if (ImGui.menuItem(ImIcons.EDIT + "  Open")) {
				openLocalFile(file);
			}
			ImGui.separator();
			if (ImGui.menuItem(ImIcons.COPYRIGHT + "  Copy Path")) {
				ImGui.setClipboardText(file.getAbsolutePath());
			}
			if (ImGui.menuItem(ImIcons.FOLDER_OPEN + "  Reveal in Explorer")) {
				revealInExplorer(file.getParentFile());
			}
			ImGui.endPopup();
		}
	}

	private void renderDirectoryContextMenu(File dir, String popupId) {
		if (ImGui.beginPopupContextItem(popupId)) {
			if (ImGui.menuItem(ImIcons.COPY + "  Copy Path")) {
				ImGui.setClipboardText(dir.getAbsolutePath());
			}
			if (ImGui.menuItem(ImIcons.FOLDER_OPEN + "  Reveal in Explorer")) {
				revealInExplorer(dir);
			}
			ImGui.separator();
			if (ImGui.menuItem(ImIcons.FILE + "  New File\u2026")) {
				createNewFileIn(dir);
			}
			if (ImGui.menuItem(ImIcons.FOLDER + "  New Folder\u2026")) {
				createNewFolderIn(dir);
			}
			ImGui.endPopup();
		}
	}

	private void openLocalFile(File file) {
		String name = file.getName().toLowerCase();
		if (name.endsWith(".png") || name.endsWith(".jpg")) {
			openTextureViewer(file);
			return;
		}
		try {
			String content = Files.readString(file.toPath());
			Identifier editorId = fileToEditorId(file);
			Panel existing = Client.getEditorManager().getPanel(editorId);
			if (existing != null) {
				existing.open();
				return;
			}

			URL scriptRoot = scriptRootFor(file);
			CodeEditor editor = new CodeEditor(editorId, Component.literal(file.getName()), content, scriptRoot);
			editor.applyLanguage(file.getName());
			editor.setSaveCallback((source, errors) -> {
				try {
					Files.writeString(file.toPath(), source);
				} catch (IOException e) {
					setError("Save failed for \"" + file.getName() + "\": " + e.getLocalizedMessage());
				}
			});
			Client.getEditorManager().register(editor);
			editor.open();
		} catch (IOException e) {
			setError("Could not open \"" + file.getName() + "\": " + e.getLocalizedMessage());
		}
	}

	private void openResource(Identifier id) {
		String path = id.getPath().toLowerCase();
		if (path.endsWith(".png") || path.endsWith(".jpg")) {
			openTextureViewer(id);
			return;
		}
		Identifier editorId = generateEditorId("res_" + id.getNamespace(), id.getPath());
		if (Client.getEditorManager().getPanel(editorId) != null) {
			return;
		}

		// Try client resources first
		var opt = Client.getResourceManager().getResource(id);
		if (opt.isPresent()) {
			openResourceFromStream(id, opt.get());
			return;
		}

		// Otherwise request from server (if connected)
		if (hasServer()) {
			ClientPacketDistributor.sendToServer(new ServerBoundExplorerPacket(ServerBoundExplorerPacket.Action.REQUEST_RESOURCE_CONTENT, id.getNamespace() + ":" + id.getPath(), ""));
		}
	}

	private void openResourceFromStream(Identifier id, Resource resource) {
		try (InputStream in = resource.open();
		     BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			String content = reader.lines().collect(Collectors.joining("\n"));
			String fileName = id.getPath().contains("/") ? id.getPath().substring(id.getPath().lastIndexOf('/') + 1) : id.getPath();
			Identifier editorId = generateEditorId("res_" + id.getNamespace(), id.getPath());
			CodeEditor editor = new CodeEditor(editorId, Component.literal(id.getPath()), content);
			editor.getTextEditor().setReadOnly(true);
			editor.forceReadOnly = true;
			editor.applyLanguage(fileName);
			Client.getEditorManager().register(editor);
			editor.open();
		} catch (IOException e) {
			LOGGER.error("Failed to open resource '{}': {}", id, e.getMessage());
		}
	}

	private void openTextureViewer(File file) {
		Identifier viewerId = Common.id("tex_viewer_" + file.getAbsolutePath().hashCode());
		if (Client.getEditorManager().getPanel(viewerId) instanceof TextureViewerPanel viewer) {
			viewer.open();
			return;
		}
		TextureViewerPanel viewer = new TextureViewerPanel(viewerId, Component.literal("Texture: " + file.getName()), file);
		Client.getEditorManager().register(viewer);
		viewer.open();
	}

	private void openTextureViewer(Identifier id) {
		Identifier viewerId = generateEditorId("res_tex", id.toString());
		if (Client.getEditorManager().getPanel(viewerId) instanceof TextureViewerPanel viewer) {
			viewer.open();
			return;
		}
		TextureViewerPanel viewer = new TextureViewerPanel(viewerId, Component.literal("Texture: " + id.getPath()), id);
		Client.getEditorManager().register(viewer);
		viewer.open();
	}

	private void openRemoteFile(String relativePath) {
		Identifier editorId = remoteFileEditorId(relativePath);
		if (Client.getEditorManager().getPanel(editorId) != null) {
			return;
		}
		ClientPacketDistributor.sendToServer(new ServerBoundExplorerPacket(ServerBoundExplorerPacket.Action.REQUEST_FILE_CONTENT, relativePath, ""));
	}

	private void requestRemoteFileList() {
		remoteRequested = true;
		remoteLoading = true;
		ClientPacketDistributor.sendToServer(new ServerBoundExplorerPacket(ServerBoundExplorerPacket.Action.REQUEST_FILE_LIST, "", ""));
	}

	private void reloadResources() {
		Server.reloadResources().thenRun(this::refresh).exceptionally(e -> {
			LOGGER.error("Resource reload failed", e);
			return null;
		});
	}

	private Identifier generateEditorId(String prefix, String resourcePath) {
		String sanitized = resourcePath.toLowerCase().replaceAll("[^a-z0-9]", "_");
		return Common.id(prefix + "_" + sanitized);
	}

	private boolean isFileOpen(File file) {
		return Client.getEditorManager().getPanel(fileToEditorId(file)) != null;
	}

	private void renderLocalFileTooltip(File file, boolean isOpen) {
		if (!ImGui.isItemHovered()) {
			return;
		}
		ImGui.beginTooltip();
		ImGui.text(file.getName());
		ImGui.separator();
		try {
			long bytes = Files.readAttributes(file.toPath(), BasicFileAttributes.class).size();
			String size = bytes < 1_024 ? bytes + " B"
				: bytes < 1_048_576 ? String.format("%.1f KB", bytes / 1_024.0)
				  : String.format("%.1f MB", bytes / 1_048_576.0);
			ImGui.textDisabled("Size:     " + size);
		} catch (IOException ignored) {
		}
		ImGui.textDisabled("Modified: " + DATE_FMT.format(Instant.ofEpochMilli(file.lastModified())));
		if (isOpen) {
			ImGui.separator();
			ImGui.textDisabled("Currently open in editor");
		}
		ImGui.endTooltip();
	}

	private void createNewFileIn(File dir) {
		File newFile = uniqueFile(dir, "new_file", "txt");
		try {
			if (newFile.createNewFile()) {
				refresh();
				openLocalFile(newFile);
			}
		} catch (IOException e) {
			setError("Could not create file: " + e.getLocalizedMessage());
		}
	}

	private void createNewFolderIn(File dir) {
		File newDir = uniqueFile(dir, "new_folder", null);
		if (!newDir.mkdir()) {
			setError("Could not create folder: " + newDir.getName());
		} else {
			refresh();
		}
	}

	private void revealInExplorer(File dir) {
		try {
			Desktop.getDesktop().open(dir);
		} catch (Exception ignored) {
		}
	}

	private void setError(String message) {
		lastError = message;
		LOGGER.error(message);
	}

	private void clearError() {
		lastError = null;
		rootReadable = true;
	}

	@FunctionalInterface
	private interface RenderFileNode {
		void render(LocalTree.Node node);
	}

	@FunctionalInterface
	private interface RenderFileItem {
		void render(File file);
	}

	@FunctionalInterface
	private interface ResourceFileRenderer {
		void render(Identifier id);
	}

	@FunctionalInterface
	private interface RemoteFileRenderer {
		void render(RemoteTree.Entry entry);
	}

	private static class LocalTree {
		final Node root = new Node(null, null);

		void add(File file) {
			root.addChild(file);
		}

		void render(RenderFileNode nodeRenderer, RenderFileItem itemRenderer) {
			root.renderChildren(nodeRenderer, itemRenderer);
		}

		void renderFiltered(String query, RenderFileItem itemRenderer) {
			root.renderFiltered(query, itemRenderer);
		}

		static class Node {
			@Nullable
			final String name;
			@Nullable
			final File file;
			final List<Node> children = new ArrayList<>();
			final List<File> files = new ArrayList<>();

			Node(@Nullable String name, @Nullable File file) {
				this.name = name;
				this.file = file;
			}

			void addChild(File entry) {
				if (entry.isDirectory()) {
					Node dirNode = new Node(entry.getName(), entry);
					File[] contents = entry.listFiles();
					if (contents != null) {
						Arrays.sort(contents,
							Comparator.comparing(File::isFile).thenComparing(f -> f.getName().toLowerCase()));
						for (File child : contents) {
							dirNode.addChild(child);
						}
					}
					children.add(dirNode);
				} else {
					files.add(entry);
				}
			}

			void renderChildren(RenderFileNode nodeRenderer, RenderFileItem itemRenderer) {
				for (Node child : children) {
					nodeRenderer.render(child);
				}
				for (File file : files) {
					itemRenderer.render(file);
				}
			}

			boolean renderFiltered(String query, RenderFileItem itemRenderer) {
				boolean matched = false;
				for (Node child : children) {
					matched |= child.renderFiltered(query, itemRenderer);
				}
				for (File file : files) {
					if (file.getName().toLowerCase().contains(query)) {
						itemRenderer.render(file);
						matched = true;
					}
				}
				return matched;
			}
		}
	}

	private static class ResourceTree {
		final TreeMap<String, ResourceTree> children = new TreeMap<>();
		final List<Identifier> resources = new ArrayList<>();

		boolean isEmpty() {
			return children.isEmpty() && resources.isEmpty();
		}

		void add(Identifier id) {
			ResourceTree nsNode = children.computeIfAbsent(id.getNamespace(), k -> new ResourceTree());
			String[] segments = id.getPath().split("/");
			ResourceTree current = nsNode;
			for (int i = 0; i < segments.length - 1; i++) {
				current = current.children.computeIfAbsent(segments[i], k -> new ResourceTree());
			}
			if (!current.resources.contains(id)) {
				current.resources.add(id);
			}
		}

		void render(ResourceFileRenderer renderer) {
			for (var entry : children.entrySet()) {
				ResourceTree subtree = entry.getValue();
				boolean isOpen = ImGui.treeNodeEx("##f_" + entry.getKey(), ImGuiTreeNodeFlags.SpanAvailWidth, "");
				ImGui.sameLine();
				String icon = isOpen ? ImGraphicsExtractor.icon(ImIcons.FOLDER_OPEN)
					: ImGraphicsExtractor.icon(ImIcons.FOLDER);
				ImGui.textUnformatted(icon + " " + entry.getKey());
				if (isOpen) {
					subtree.render(renderer);
					ImGui.treePop();
				}
			}
			for (Identifier id : resources) {
				renderer.render(id);
			}
		}

		void renderFiltered(String query, ResourceFileRenderer renderer) {
			for (Identifier id : resources) {
				if (id.toString().toLowerCase().contains(query)) {
					renderer.render(id);
				}
			}
			for (ResourceTree child : children.values()) {
				child.renderFiltered(query, renderer);
			}
		}
	}

	private static class RemoteTree {
		final RemoteNode root = new RemoteNode();

		void add(String path, boolean isDir) {
			root.add(path, isDir);
		}

		void render(RemoteFileRenderer renderer) {
			root.render(renderer);
		}

		void renderFiltered(String query, RemoteFileRenderer renderer) {
			root.renderFiltered(query, renderer);
		}

		static class RemoteNode {
			final TreeMap<String, RemoteNode> children = new TreeMap<>();
			final List<Entry> files = new ArrayList<>();

			void add(String path, boolean isDir) {
				String[] parts = path.split("/");
				RemoteNode current = this;
				for (int i = 0; i < parts.length; i++) {
					if (i == parts.length - 1) {
						if (isDir) {
							current.children.computeIfAbsent(parts[i], k -> new RemoteNode());
						} else {
							current.files.add(new Entry(parts[i], path, false));
						}
					} else {
						current = current.children.computeIfAbsent(parts[i], k -> new RemoteNode());
					}
				}
			}

			boolean isEmpty() {
				return children.isEmpty() && files.isEmpty();
			}

			void render(RemoteFileRenderer renderer) {
				for (var entry : children.entrySet()) {
					RemoteNode subtree = entry.getValue();
					if (subtree.isEmpty()) {
						continue;
					}
					boolean isOpen = ImGui.treeNodeEx("##rdir_" + entry.getKey(),
						ImGuiTreeNodeFlags.SpanAvailWidth, "");
					ImGui.sameLine();
					String icon = isOpen ? ImGraphicsExtractor.icon(ImIcons.FOLDER_OPEN)
						: ImGraphicsExtractor.icon(ImIcons.FOLDER);
					ImGui.textUnformatted(icon + " " + entry.getKey());
					if (isOpen) {
						subtree.render(renderer);
						ImGui.treePop();
					}
				}
				for (Entry file : files) {
					renderer.render(file);
				}
			}

			void renderFiltered(String query, RemoteFileRenderer renderer) {
				for (var entry : children.entrySet()) {
					entry.getValue().renderFiltered(query, renderer);
				}
				for (Entry file : files) {
					if (file.name.toLowerCase().contains(query)) {
						renderer.render(file);
					}
				}
			}
		}

		record Entry(String name, String relativePath, boolean isDirectory) {
		}
	}
}