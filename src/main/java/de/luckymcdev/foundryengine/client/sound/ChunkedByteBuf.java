package de.luckymcdev.foundryengine.client.sound;

import com.google.common.collect.Lists;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.util.List;

public class ChunkedByteBuf {
	private final List<ByteBuffer> buffers = Lists.newArrayList();
	private final int bufferSize;
	private int byteCount;
	private ByteBuffer currentBuffer;

	public ChunkedByteBuf(int bufferSize) {
		this.bufferSize = bufferSize + 1 & -2;
		this.currentBuffer = BufferUtils.createByteBuffer(this.bufferSize);
	}

	public void put(short[] samples, int length) {
		for (int i = 0; i < length; i++) {
			if (this.currentBuffer.remaining() == 0) {
				this.currentBuffer.flip();
				this.buffers.add(this.currentBuffer);
				this.currentBuffer = BufferUtils.createByteBuffer(this.bufferSize);
			}

			this.currentBuffer.putShort(samples[i]);
			this.byteCount += 2;
		}
	}

	public ByteBuffer get() {
		this.currentBuffer.flip();
		if (this.buffers.isEmpty()) {
			return this.currentBuffer;
		} else {
			ByteBuffer result = BufferUtils.createByteBuffer(this.byteCount);
			this.buffers.forEach(result::put);
			result.put(this.currentBuffer);
			result.flip();
			return result;
		}
	}

	public int size() {
		return this.byteCount;
	}
}