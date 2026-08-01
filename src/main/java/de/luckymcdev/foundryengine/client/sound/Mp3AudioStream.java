package de.luckymcdev.foundryengine.client.sound;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.decoder.SampleBuffer;
import net.minecraft.client.sounds.FiniteAudioStream;
import org.jspecify.annotations.Nullable;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class Mp3AudioStream implements FiniteAudioStream {
	private static final int EXPECTED_MAX_FRAME_SIZE = 8192;

	private final Bitstream bitstream;
	private final Decoder decoder = new Decoder();
	private final AudioFormat audioFormat;

	public Mp3AudioStream(InputStream input) throws IOException {
		this.bitstream = new Bitstream(input);
		Header header = this.readHeader();
		if (header == null) {
			throw new IOException("Invalid MP3 file - can't find first frame");
		}

		int channels = header.mode() == Header.SINGLE_CHANNEL ? 1 : 2;
		this.audioFormat = new AudioFormat(header.frequency(), 16, channels, true, false);
	}

	private @Nullable Header readHeader() throws IOException {
		try {
			return this.bitstream.readFrame();
		} catch (BitstreamException e) {
			throw new IOException("Failed to read MP3 frame", e);
		}
	}

	@Override
	public AudioFormat getFormat() {
		return this.audioFormat;
	}

	@Override
	public ByteBuffer read(int expectedSize) throws IOException {
		ChunkedByteBuf output = new ChunkedByteBuf(expectedSize + EXPECTED_MAX_FRAME_SIZE);

		while (this.readChunk(output) && output.size() < expectedSize) {
		}

		return output.get();
	}

	@Override
	public ByteBuffer readAll() throws IOException {
		ChunkedByteBuf output = new ChunkedByteBuf(16384);

		while (this.readChunk(output)) {
		}

		return output.get();
	}

	private boolean readChunk(ChunkedByteBuf output) throws IOException {
		Header header = this.readHeader();
		if (header == null) {
			return false;
		}

		try {
			SampleBuffer samples = (SampleBuffer) this.decoder.decodeFrame(header, this.bitstream);
			output.put(samples.getBuffer(), samples.getBufferLength());
		} catch (JavaLayerException e) {
			throw new IOException("Failed to decode MP3 frame", e);
		} finally {
			this.bitstream.closeFrame();
		}

		return true;
	}

	@Override
	public void close() throws IOException {
		try {
			this.bitstream.close();
		} catch (BitstreamException e) {
			throw new IOException(e);
		}
	}
}