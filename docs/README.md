# FoundryEngine docs

The docs site for FoundryEngine, built with [Docusaurus](https://docusaurus.io/). Content lives in `docs/docs/` in three sections: getting started, guides, concepts.

## Prerequisites

- Node.js 18 or newer.
- `npm install` in this directory.

## Local development

```bash
npm run start
```

Starts a local server that hot-reloads on change. Most edits show up without a restart.

## Build

```bash
npm run docs:build
```

Builds static content into the `build` directory. The build fails on broken links and missing pages, so run it before pushing.

## Content layout

- `docs/docs/getting-started/`: learning journeys, read in order.
- `docs/docs/guides/`: recipes and API lookups.
- `docs/docs/concepts/`: how the engine works.
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
# Bump patch version (e.g. 0.1.9 -> 0.1.10)
./gradlew bumpVersion --bump=patch

# Bump minor version (e.g. 0.1.9 -> 0.2.0)
./gradlew bumpVersion --bump=minor

# Set an explicit version and suffix
./gradlew bumpVersion --to=0.2.0 --suffix=beta

# Freeze current docs snapshot before bumping, commit, and tag
./gradlew bumpVersion --bump=minor --snapshot-docs --commit --tag
```

### 3. GitHub Actions Release Workflow

Releases and documentation deployments can be triggered in two ways:

1. **Pushing a Git Tag**: Pushing any tag starting with `v*` (e.g. `v0.1.9` or `v0.1.9-26.1`) runs `.github/workflows/release.yml`, building all Stonecutter targets and publishing to GitHub Releases, CurseForge, and Modrinth.
2. **Workflow Dispatch**: Run **Release & Publish** under GitHub Actions with inputs for `bump_type`, `suffix`, `snapshot_docs`, and `dry_run`. The workflow automatically handles bumping files, committing as `github-actions[bot]`, creating Git tags, publishing artifacts, and deploying updated docs.

## Writing

Follow the writing standard in `docs/docs/contributing.mdx` before adding or editing content.
