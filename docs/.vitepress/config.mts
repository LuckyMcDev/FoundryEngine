import {defineConfig} from 'vitepress'

export default defineConfig({
	title: "FoundryEngine",
	description: "A Minecraft mod that turns the game into a game engine — build mods without writing Java.",
	base: '/FoundryEngine/',

	srcExclude: ['**/javadoc/**'],

	head: [
		['link', {rel: 'icon', href: '/logo_transparent.png'}]
	],

	themeConfig: {
		logo: '/logo_transparent.png',

		nav: [
			{text: 'Home', link: '/'},
			{text: 'Features', link: '/features'},
			{text: 'Get Started', link: '/getting-started/'},
			{text: 'Core Concepts', link: '/core-concepts/'},
			{text: 'Systems', link: '/systems/'},
			{text: 'Examples', link: '/examples/'},
			{text: 'GitHub', link: 'https://github.com/LuckyMcDev/FoundryEngine'}
		],

		sidebar: {
			'/getting-started/': [
				{
					text: 'Getting Started',
					items: [
						{text: 'What is FoundryEngine?', link: '/getting-started/'},
						{text: 'Installation', link: '/getting-started/installation'},
						{text: 'Your First Bundle', link: '/getting-started/first-bundle'},
						{text: 'Workspaces', link: '/getting-started/workspaces'}
					]
				}
			],

			'/core-concepts/': [
				{
					text: 'Bundles & Scripts',
					items: [
						{text: 'Overview', link: '/core-concepts/'},
						{text: 'What is a Bundle?', link: '/core-concepts/what-is-a-bundle'},
						{text: 'Bundle Manifest', link: '/core-concepts/bundle-manifest'},
						{text: 'Groovy Scripts', link: '/core-concepts/scripts'},
						{text: 'Client & Server', link: '/core-concepts/sides'},
						{text: 'Dependencies', link: '/core-concepts/dependencies'}
					]
				},
				{
					text: 'Creating Content',
					items: [
						{text: 'Creating Items', link: '/core-concepts/creating-items'},
						{text: 'Creating Blocks', link: '/core-concepts/creating-blocks'},
						{text: 'Creating Recipes', link: '/core-concepts/creating-recipes'},
						{text: 'Creating Sounds & Particles', link: '/core-concepts/creating-sounds-particles'}
					]
				},
				{
					text: 'Events & Registration',
					items: [
						{text: 'Events Guide', link: '/core-concepts/events-guide'},
						{text: 'Events Reference', link: '/core-concepts/events-reference'},
						{text: 'Registration', link: '/core-concepts/registration'}
					]
				}
			],

			'/systems/': [
				{
					text: 'Editor & Cinematics',
					items: [
						{text: 'In-Game Editor', link: '/systems/editor'},
						{text: 'Cutscenes', link: '/systems/cutscenes'}
					]
				},
				{
					text: 'World & Progression',
					items: [
						{text: 'Custom Worlds', link: '/systems/instanced-worlds'},
						{text: 'Game Stages', link: '/systems/stages'},
						{text: 'Game Sessions', link: '/systems/game-sessions'},
						{text: 'Areas', link: '/systems/areas'},
						{text: 'Waypoints', link: '/systems/waypoints'}
					]
				},
				{
					text: 'Rendering & Effects',
					items: [
						{text: 'Custom Particles', link: '/systems/particles'},
						{text: 'Post-Processing', link: '/systems/post-processing'},
						{text: 'Mesh Rendering', link: '/systems/mesh-rendering'},
						{text: 'Skybox', link: '/systems/skybox'},
						{text: 'Node Graph Editor', link: '/systems/node-editor'},
						{text: 'Easing Functions', link: '/systems/easing'}
					]
				},
				{
					text: 'Interaction',
					items: [
						{text: 'Dialogue System', link: '/systems/dialogue'},
						{text: 'Markdown Rendering', link: '/systems/markdown'},
						{text: 'NBT Suggestions', link: '/systems/nbt-suggestions'},
						{text: 'Item Tooltips', link: '/systems/tooltips'},
						{text: 'Audio Streaming', link: '/systems/audio-streaming'}
					]
				},
				{
					text: 'Reference',
					items: [
						{text: 'Saved Data & Persistence', link: '/systems/persistence'},
						{text: 'Commands Reference', link: '/systems/commands'}
					]
				}
			],

			'/advanced/': [
				{
					text: 'For Java Developers',
					items: [
						{text: 'Overview', link: '/advanced/'},
						{text: 'Java Addon API', link: '/advanced/addon-api'},
						{text: 'Data Generation', link: '/advanced/data-generation'},
						{text: 'Network Packets', link: '/advanced/network-packets'},
						{text: 'Editor Themes', link: '/advanced/editor-themes'}
					]
				}
			],

			'/examples/': [
				{
					text: 'Examples',
					items: [
						{text: 'Code Examples', link: '/examples/'},
						{text: 'Showcase Bundle', link: '/examples/showcase-bundle'}
					]
				}
			]
		},

		socialLinks: [
			{icon: 'github', link: 'https://github.com/LuckyMcDev/FoundryEngine'}
		],

		footer: {
			message: 'FoundryEngine is a work-in-progress. Documentation may change.',
			copyright: 'Copyright © 2025 LuckyMcDev'
		},

		editLink: {
			pattern: 'https://github.com/LuckyMcDev/FoundryEngine/blob/main/docs/:path',
			text: 'Edit this page on GitHub'
		}
	},

	markdown: {
		lineNumbers: true,
		theme: 'github-dark'
	},

	ignoreDeadLinks: false
})
