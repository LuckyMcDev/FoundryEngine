// @ts-check
// `@type` JSDoc annotations allow editor autocompletion and type checking
// (when paired with `@ts-check`).

import {themes as prismThemes} from 'prism-react-renderer';

/** @type {import('@docusaurus/types').Config} */
const config = {
	title: 'FoundryEngine',
	tagline: 'A Minecraft mod that turns the game into a game engine — build mods without writing Java.',
	favicon: 'logo_transparent.png',

	url: 'https://luckymcdev.github.io',
	baseUrl: '/FoundryEngine/',

	organizationName: 'LuckyMcDev',
	projectName: 'FoundryEngine',

	onBrokenLinks: 'throw',

	i18n: {
		defaultLocale: 'en',
		// Keep this list in sync with the locale dropdown in the navbar.
		// To add a language, add its locale here and create the translated
		// content under docs/i18n/[locale]/ (see docs/content/contributing.md).
		locales: ['en'],
	},

	presets: [
		[
			'classic',
			/** @type {import('@docusaurus/preset-classic').Options} */
			({
				docs: {
					path: 'content',
					routeBasePath: '/',
					sidebarPath: './sidebars.js',
					editUrl: 'https://github.com/LuckyMcDev/FoundryEngine/edit/main/docs/content/',
					// Docs are versioned per Minecraft version. The `content/`
					// folder is the current version and lives at the site root.
					// Each released MC version gets frozen into
					// `versioned_docs/version-<mc>/` via `npm run docs:version`.
					// See docs/content/contributing.md -> "Documenting a new MC version".
					lastVersion: 'current',
					versions: {
						current: {
							label: 'MC 26.1',
						},
					},
				},
				blog: false,
				theme: {
					customCss: './src/css/custom.css',
				},
			}),
		],
	],

	themeConfig:
	/** @type {import('@docusaurus/preset-classic').ThemeConfig} */
			({
				colorMode: {
					respectPrefersColorScheme: true,
				},
				navbar: {
					title: 'FoundryEngine',
					logo: {
						alt: 'FoundryEngine Logo',
						src: 'logo_transparent.png',
					},
					items: [
						{type: 'doc', docId: 'features', position: 'left', label: 'Features'},
						{type: 'doc', docId: 'getting-started/index', position: 'left', label: 'Get Started'},
						{type: 'doc', docId: 'core-concepts/index', position: 'left', label: 'Core Concepts'},
						{type: 'doc', docId: 'systems/index', position: 'left', label: 'Systems'},
						{type: 'doc', docId: 'advanced/index', position: 'left', label: 'Advanced'},
						{type: 'doc', docId: 'examples/index', position: 'left', label: 'Examples'},
						{
							type: 'docsVersionDropdown',
							position: 'left',
							dropdownItemsAfter: [
								{type: 'html', value: '<hr class="dropdown-separator" />'},
								{
									type: 'html',
									value: '<strong style="padding: 0 0.5rem">Supported MC versions</strong>',
								},
							],
						},
						{
							type: 'localeDropdown',
							position: 'right',
						},
						{
							href: 'https://github.com/LuckyMcDev/FoundryEngine',
							label: 'GitHub',
							position: 'right',
						},
					],
				},
				footer: {
					style: 'dark',
					copyright: `FoundryEngine is a work-in-progress. Documentation may change. Copyright © ${new Date().getFullYear()} LuckyMcDev.`,
				},
				prism: {
					theme: prismThemes.github,
					darkTheme: prismThemes.dracula,
				},
			}),
};

export default config;