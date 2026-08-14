import {themes as prismThemes} from 'prism-react-renderer';
import type {Config} from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';

// This runs in Node.js - Don't use client-side code here (browser APIs, JSX...)

const config: Config = {
	title: 'FoundryEngine',
	tagline: 'Turn Minecraft into a game engine, one bundle at a time',
	favicon: 'img/favicon.png',

	plugins: [
		[
			"@cmfcmf/docusaurus-search-local",
			{
				// Options here
			},
		],
	],

	// Future flags, see https://docusaurus.io/docs/api/docusaurus-config#future
	future: {
		v4: true, // Improve compatibility with the upcoming Docusaurus v4
	},

	// Set the production url of your site here
	url: 'https://luckymcdev.github.io',
	// Set the /<baseUrl>/ pathname under which your site is served
	// For GitHub pages deployment, it is often '/<projectName>/'
	baseUrl: '/FoundryEngine/',

	// GitHub pages deployment config.
	// If you aren't using GitHub pages, you don't need these.
	organizationName: 'LuckyMcDev', // Usually your GitHub org/user name.
	projectName: 'FoundryEngine', // Usually your repo name.

	onBrokenLinks: 'throw',

	// Even if you don't use internationalization, you can use this field to set
	// useful metadata like html lang. For example, if your site is Chinese, you
	// may want to replace "en" with "zh-Hans".
	i18n: {
		defaultLocale: 'en',
		locales: ['en'],
	},

	presets: [
		[
			'classic',
			{
				docs: {
					sidebarPath: './sidebars.ts',
					editUrl:
						'https://github.com/LuckyMcDev/FoundryEngine/tree/master/',
					// Docs are versioned per mod version. The live ./docs folder is
					// always the newest mod version; older mod versions are frozen snapshots
					// created with `npm run docs:version <version>`.
					lastVersion: 'current',
					includeCurrentVersion: true,
					versions: require('./version_labels.json'),
				},
				blog: {
					showReadingTime: false,
					routeBasePath: 'news',
					blogTitle: 'News',
					blogDescription: 'Changelog and news for FoundryEngine',
					postsPerPage: 10,
					feedOptions: {
						type: ['rss', 'atom'],
						xslt: true,
					},
					// Please change this to your repo.
					// Remove this to remove the "edit this page" links.
					editUrl:
						'https://github.com/LuckyMcDev/FoundryEngine/tree/master/',
					// Useful options to enforce blogging best practices
					onInlineTags: 'warn',
					onInlineAuthors: 'warn',
					onUntruncatedBlogPosts: 'warn',
				},
				theme: {
					customCss: './src/css/custom.css',
				},
			} satisfies Preset.Options,
		],
	],

	themeConfig: {
		colorMode: {
			respectPrefersColorScheme: true,
		},
		navbar: {
			title: 'FoundryEngine',
			logo: {
				alt: 'FoundryEngine Logo',
				src: 'img/logo.png',
			},
			items: [
				{
					type: 'docSidebar',
					sidebarId: 'gettingStartedSidebar',
					position: 'left',
					label: 'Getting started',
				},
				{
					type: 'docSidebar',
					sidebarId: 'guidesSidebar',
					position: 'left',
					label: 'Guides',
				},
				{
					type: 'docSidebar',
					sidebarId: 'conceptsSidebar',
					position: 'left',
					label: 'Concepts',
				},
				{to: '/news', label: 'News', position: 'left'},
				{
					to: 'pathname:///javadoc/',
					label: 'Javadoc',
					position: 'left',
				},
				{
					type: 'docsVersionDropdown',
					position: 'right',
				},
				{
					href: 'https://github.com/LuckyMcDev/FoundryEngine/',
					label: 'GitHub',
					position: 'right',
				},
				{
					type: 'search',
					position: 'right',
				}
			],
		},
		footer: {
			style: 'dark',
			links: [
				{
					title: 'Docs',
					items: [
						{
							label: 'Getting started',
							to: '/docs/getting-started/',
						},
						{
							label: 'Guides',
							to: '/docs/guides/',
						},
						{
							label: 'Concepts',
							to: '/docs/concepts/',
						},
					],
				},
				{
					title: 'Community',
					items: [
						{
							label: 'News & Changelog',
							to: '/news',
						},
						{
							label: 'GitHub',
							href: 'https://github.com/LuckyMcDev/FoundryEngine/',
						},
						{
							label: 'Bundle Template',
							href: 'https://github.com/LuckyMcDev/ExampleBundle',
						},
					],
				},
				{
					title: 'More',
					items: [
						{
							label: 'GitHub',
							href: 'https://github.com/LuckyMcDev/FoundryEngine/',
						},
					],
				},
			],
			copyright: `Copyright © ${new Date().getFullYear()} LuckyMcDev. Built with Docusaurus.
			<br />
			NOT AN OFFICIAL MINECRAFT SERVICE. NOT APPROVED BY OR ASSOCIATED WITH MOJANG OR MICROSOFT.`,
		},
		prism: {
			theme: prismThemes.github,
			darkTheme: prismThemes.dracula,
			additionalLanguages: ['groovy', 'gradle', 'toml', 'properties'],
		},
	} satisfies Preset.ThemeConfig,
};

export default config;
