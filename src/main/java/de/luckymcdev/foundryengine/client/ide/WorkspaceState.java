package de.luckymcdev.foundryengine.client.ide;

import de.luckymcdev.foundryengine.client.editor.panel.files.CodeEditor;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public final class WorkspaceState {
	private final Map<Identifier, Buffer> buffers = new LinkedHashMap<>();
	private final Map<Identifier, String> bufferContents = new HashMap<>();
	private final Map<Identifier, Int2ObjectMap<String>> bufferErrors = new HashMap<>();
	private final List<Runnable> changeListeners = new CopyOnWriteArrayList<>();
	private @Nullable Identifier activeBufferId;

	public void registerBuffer(Identifier id, String content) {
		Buffer buf = new Buffer(id, id.toString(), null, null);
		buffers.put(id, buf);
		bufferContents.put(id, content);
		activeBufferId = id;
		notifyChange();
	}

	public void registerBuffer(Identifier id, String filePath, String content) {
		Buffer buf = new Buffer(id, filePath, null, null);
		buffers.put(id, buf);
		bufferContents.put(id, content);
		activeBufferId = id;
		notifyChange();
	}

	public void registerBuffer(Identifier id, String filePath, String content, @Nullable URL scriptRoot) {
		Buffer buf = new Buffer(id, filePath, null, scriptRoot);
		buffers.put(id, buf);
		bufferContents.put(id, content);
		activeBufferId = id;
		notifyChange();
	}

	public @Nullable URL getBufferScriptRoot(Identifier id) {
		Buffer buf = buffers.get(id);
		return buf != null ? buf.scriptRoot() : null;
	}

	public void deregisterBuffer(Identifier id) {
		buffers.remove(id);
		bufferContents.remove(id);
		bufferErrors.remove(id);
		if (id.equals(activeBufferId)) {
			activeBufferId = buffers.isEmpty() ? null : buffers.keySet().iterator().next();
		}
		notifyChange();
	}

	public void updateBufferContent(Identifier id, String content) {
		bufferContents.put(id, content);
		notifyChange();
	}

	public @Nullable String getBufferContent(Identifier id) {
		return bufferContents.get(id);
	}

	public void setBufferErrors(Identifier id, Int2ObjectMap<String> errors) {
		setErrors(id, errors);
	}

	public void clearBufferErrors(Identifier id) {
		bufferErrors.remove(id);
		notifyChange();
	}

	public Int2ObjectMap<String> getBufferErrors(Identifier id) {
		return getErrors(id);
	}

	public void registerBuffer(Buffer buffer) {
		buffers.put(buffer.id(), buffer);
		activeBufferId = buffer.id();
		notifyChange();
	}

	public void removeBuffer(Identifier id) {
		buffers.remove(id);
		bufferErrors.remove(id);
		if (id.equals(activeBufferId)) {
			activeBufferId = buffers.isEmpty() ? null : buffers.keySet().iterator().next();
		}
		notifyChange();
	}

	public @Nullable Buffer getBuffer(Identifier id) {
		return buffers.get(id);
	}

	public @Nullable Buffer getActiveBuffer() {
		return activeBufferId != null ? buffers.get(activeBufferId) : null;
	}

	public void setActiveBuffer(Identifier id) {
		if (buffers.containsKey(id)) {
			this.activeBufferId = id;
			notifyChange();
		}
	}

	public @Nullable Identifier getActiveBufferId() {
		return activeBufferId;
	}

	public Map<Identifier, Buffer> getAllBuffers() {
		return new LinkedHashMap<>(buffers);
	}

	public void setErrors(Identifier bufferId, Int2ObjectMap<String> errors) {
		bufferErrors.put(bufferId, errors);
		notifyChange();
	}

	public Int2ObjectMap<String> getErrors(Identifier bufferId) {
		return bufferErrors.getOrDefault(bufferId, new Int2ObjectArrayMap<>());
	}

	public Int2ObjectMap<String> getActiveErrors() {
		if (activeBufferId != null) {
			return getErrors(activeBufferId);
		}
		return new Int2ObjectArrayMap<>();
	}

	public void addChangeListener(Runnable listener) {
		changeListeners.add(listener);
	}

	public void removeChangeListener(Runnable listener) {
		changeListeners.remove(listener);
	}

	private void notifyChange() {
		for (var l : changeListeners) {
			l.run();
		}
	}

	public record Buffer(Identifier id, String filePath, @Nullable CodeEditor editor, @Nullable URL scriptRoot) {
	}
}