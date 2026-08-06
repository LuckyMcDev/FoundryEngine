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

## Versioning

The live `docs/docs/` folder always documents the current Minecraft version (labeled `26.1`). To freeze a version:

```bash
npm run docs:version <version>
```

This copies the current content into a versioned folder and lets you continue editing the live one. Update `version_labels.json` whenever the current label changes. See `.agents/docs-cicd.md` in the repository root for the full workflow.

## Writing

Follow the writing standard in `docs/docs/contributing.mdx` before adding or editing content.
