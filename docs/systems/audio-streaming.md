# Audio Streaming (MP3 / FLAC)

By default Minecraft only plays OGG audio. The **audio streaming** system extends the sound engine so you can use **MP3** and **FLAC** files in addition to OGG. It patches Minecraft's sound loading so a missing `.ogg` falls back to the same base name with a `.mp3` or `.flac` extension.

## Supported formats

| Format | Extension |
|--------|-----------|
| OGG    | `.ogg`    |
| MP3    | `.mp3`    |
| FLAC   | `.flac`   |

## How it works

- `AudioFormats.open(...)` opens any stream, detects the format from its magic header and wraps it in the right decoder (`JOrbisAudioStream`, `Mp3AudioStream`, `FlacAudioStream`). Looping is supported.
- `AudioFormats.resolve(...)` looks up a resource, and if the requested `.ogg` doesn't exist it falls back to a sibling `.mp3` / `.flac` file with the same base path.
- The mixins in the `sound` package hook Minecraft's `SoundManager`/`SoundBufferLibrary` so arbitrary format streams flow through the normal playback path.

## Authoring

Place sound files in the bundle's `assets/<namespace>/sounds/` folder. Reference them by their normal identifier; FoundryEngine will transparently pick up `.mp3` or `.flac` when no `.ogg` exists.

## Notes

- MP3/FLAC decoding is done client-side.
- Use the same registry names as vanilla sounds; the format is just swapped at load time.

## Related

- [Creating Sounds & Particles](../core-concepts/creating-sounds-particles.md) — registering sounds via the `SoundBuilder`