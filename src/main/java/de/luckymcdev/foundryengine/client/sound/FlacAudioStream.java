package de.luckymcdev.foundryengine.client.sound;

import net.minecraft.client.sounds.FiniteAudioStream;
import org.jflac.FLACDecoder;
import org.jflac.PCMProcessor;
import org.jflac.metadata.StreamInfo;
import org.jflac.util.ByteData;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class FlacAudioStream implements FiniteAudioStream {
	private final InputStream input;
	private final AudioFormat format;
	private final ByteBuffer decoded;

	public FlacAudioStream(InputStream input) throws IOException {
		this.input = input;

		FLACDecoder decoder = new FLACDecoder(input);
		ChunkedByteBuf output = new ChunkedByteBuf(1 << 16);
		int[] channels = new int[1];
		int[] sampleRate = new int[1];

		decoder.addPCMProcessor(new PCMProcessor() {
			@Override
			public void processStreamInfo(StreamInfo streamInfo) {
				channels[0] = streamInfo.getChannels();
				sampleRate[0] = streamInfo.getSampleRate();
			}

			@Override
			public void processPCM(ByteData byteData) {
				output.put(byteData.getData(), byteData.getLen());
			}
		});

		try {
			decoder.decode();
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException("Failed to decode FLAC stream", e);
		}

		if (channels[0] == 0) {
			throw new IOException("Invalid FLAC file - no stream info found");
		}

		this.format = new AudioFormat(sampleRate[0], 16, channels[0], true, false);
		this.decoded = output.get();
	}

	@Override
	public AudioFormat getFormat() {
		return this.format;
	}

	@Override
	public ByteBuffer read(int expectedSize) {
		int size = Math.min(expectedSize, this.decoded.remaining());
		ByteBuffer slice = this.decoded.slice();
		slice.limit(size);
		this.decoded.position(this.decoded.position() + size);
		return slice;
	}

	@Override
	public ByteBuffer readAll() {
		ByteBuffer remaining = this.decoded.slice();
		this.decoded.position(this.decoded.limit());
		return remaining;
	}

	@Override
	public void close() throws IOException {
		this.input.close();
	}
}