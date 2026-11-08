# FoundryEngine docs

The docs site for FoundryEngine, built with [Docusaurus](https://docusaurus.io/). Content lives in `docs/docs/` in four sections: getting started, guides, concepts, and reference.

## Prerequisites

- Node.js 20 or newer.
- `npm install` in this directory.

## Local development

```bash
npm run start
```

Starts a local server that hot-reloads on change. Most edits show up without a restart.

## Build

```bash
npm run build
```

Builds static content into the `build` directory. The build fails on broken links and missing pages, so run it before pushing.

## Content layout

- `docs/docs/getting-started/`: learning journeys, read in order.
- `docs/docs/guides/`: recipes and API lookups.
- `docs/docs/concepts/`: how the engine works.
- `docs/docs/reference/`: subsystem maps and links to generated Javadoc.
- `docs/blog/`: the news and changelog feed at `/news`.
- `docs/version_labels.json`: the label for the current version.

## Versioning & Releases

FoundryEngine uses unified mod versioning across all supported Minecraft editions.

### 1. View Current Version

```bash
./gradlew currentVersion
```

### 2. Bump Mod Version Locally

Run the `bumpVersion` task to update `stonecutter.properties.toml`, `docs/version_labels.json`, and optionally create Git commits and tags:

```bash
# Bump patch version (e.g. 0.2.2 -> 0.2.3)
./gradlew bumpVersion --bump=patch

# Bump minor version (e.g. 0.2.2 -> 0.3.0)
./gradlew bumpVersion --bump=minor

# Set an explicit version and suffix
./gradlew bumpVersion --to=0.2.0 --suffix=beta

# Freeze current docs snapshot before bumping, commit, and tag
./gradlew bumpVersion --bump=minor --snapshot-docs --commit --tag
```

### 3. GitHub Actions Release Workflow

Releases and documentation deployments can be triggered in two ways:

1. **Documentation deployment**: `.github/workflows/deploy.yml` builds Docusaurus, generates Javadoc for Minecraft 26.2, and publishes both to GitHub Pages when documentation or source files change on `master`.
2. **Build validation**: `.github/workflows/build.yml` builds the mod on pushes, pull requests, and manual dispatch. Release publishing is not configured in these workflows.

## Writing

Follow the writing standard in `docs/docs/contributing.mdx` before adding or editing content.
