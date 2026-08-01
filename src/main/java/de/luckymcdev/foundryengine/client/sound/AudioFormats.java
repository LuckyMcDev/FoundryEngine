package de.luckymcdev.foundryengine.client.sound;

import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.FiniteAudioStream;
import net.minecraft.client.sounds.JOrbisAudioStream;
import net.minecraft.client.sounds.LoopingAudioStream;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Optional;

public class AudioFormats {

	public static FiniteAudioStream openFinite(InputStream input) throws IOException {
		PushbackInputStream pushback = new PushbackInputStream(input, 4);
		return switch (detect(pushback)) {
			case OGG -> new JOrbisAudioStream(pushback);
			case MP3 -> new Mp3AudioStream(pushback);
		};
	}

	public static AudioStream open(InputStream input, boolean looping) throws IOException {
		PushbackInputStream pushback = new PushbackInputStream(input, 4);
		return switch (detect(pushback)) {
			case OGG -> looping ? new LoopingAudioStream(JOrbisAudioStream::new, pushback) : new JOrbisAudioStream(pushback);
			case MP3 -> looping ? new LoopingAudioStream(Mp3AudioStream::new, pushback) : new Mp3AudioStream(pushback);
		};
	}

	public static Optional<Identifier> resolve(ResourceProvider provider, Identifier oggPath) {
		if (provider.getResource(oggPath).isPresent()) {
			return Optional.of(oggPath);
		}

		String path = oggPath.getPath();
		if (!path.endsWith(Supported.OGG.extension())) {
			return Optional.empty();
		}

		String base = path.substring(0, path.length() - Supported.OGG.extension().length());
		for (Supported format : Supported.values()) {
			if (format == Supported.OGG) {
				continue;
			}

			Identifier candidate = Identifier.fromNamespaceAndPath(oggPath.getNamespace(), base + format.extension());
			if (provider.getResource(candidate).isPresent()) {
				return Optional.of(candidate);
			}
		}

		return Optional.empty();
	}

	private static Supported detect(PushbackInputStream input) throws IOException {
		byte[] header = new byte[4];
		int read = input.read(header);
		if (read > 0) {
			input.unread(header, 0, read);
		}

		if (read >= 4 && header[0] == 'O' && header[1] == 'g' && header[2] == 'g' && header[3] == 'S') {
			return Supported.OGG;
		}

		return Supported.MP3;
	}

	public enum Supported {
		OGG(".ogg"),
		MP3(".mp3");

		private final String extension;

		Supported(String extension) {
			this.extension = extension;
		}

		public String extension() {
			return this.extension;
		}
	}
}