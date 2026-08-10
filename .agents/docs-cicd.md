# Agent Reference — Docs & CI/CD

Read this file when working on documentation or CI.

## Docusaurus

- Location: `docs/` (site config, `package.json`, `docusaurus.config.js`, `sidebars.js`)
- Content: `docs/content/`
- Static assets: `docs/static/` (logo, screenshots)
- Scripts: `npm run docs:dev` (dev) / `npm run docs:build` (prod) / `npm run docs:preview` (serve build)
- Versioning (docs per Minecraft version): `npm run docs:version <mc>` freezes `content/` into `versioned_docs/`; `npm run docs:versions` lists frozen versions. Current version label lives in `docusaurus.config.js` → `presets[0].docs.versions.current.label`. See `docs/content/contributing.md`.
- i18n (future languages): locale dropdown is already in the navbar; adding a locale = edit `docusaurus.config.js` → `i18n.locales` + run `npm run docs:write-translations -- --locale <code>`. Only `en` is active.
- Build output: `docs/build`
- Base URL: `/FoundryEngine/` (GitHub Pages project site)
- Deploy: `.github/workflows/build-docusaurus.yml` + `.github/workflows/deploy.yml`
- Deployment model: on push to `master`, docs and javadoc are built, merged (docusaurus at root, javadoc under `javadoc/`), and published to the `docs` branch (root) via `peaceiris/actions-gh-pages@v4`. GitHub Pages source must be set to **Deploy from a branch: `docs` / root** in repo settings.
- Agent reference docs live in `.agents/` (not part of the published Docusaurus site)

## Code Documentation

- Use Javadoc for public APIs
- Internal classes can omit javadoc (ApiStatus.Internal)
- Keep docstrings concise and accurate

## CI/CD — Qodana

- Configuration: `qodana.yaml`
- Runs on: CI pipeline
- Analyzes: Code quality, style, potential issues

## Code Analysis

- Disabled: Java linter/formatter
- Enabled: Qodana static analysis
- Focus: Logic errors, potential bugs, best practices

## Dependencies (jarJar)

| Library    | Purpose               |
|------------|-----------------------|
| ImGui      | GUI rendering         |
| Groovy     | Script execution      |
| CommonMark | Markdown parsing      |
| RenderDoc  | Debugging             |
| JEI        | In-game item info     |
| Spark      | Performance profiling |
