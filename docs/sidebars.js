// @ts-check

/** @type {import('@docusaurus/plugin-content-docs').SidebarsConfig} */
const sidebars = {
	docs: [
		{
			type: 'category',
			label: 'Getting Started',
			collapsed: false,
			items: [
				'getting-started/index',
				'getting-started/installation',
				'getting-started/first-bundle',
				'getting-started/workspaces',
			],
		},
		{
			type: 'category',
			label: 'Bundles & Scripts',
			collapsed: false,
			items: [
				'core-concepts/index',
				'core-concepts/what-is-a-bundle',
				'core-concepts/bundle-manifest',
				'core-concepts/scripts',
				'core-concepts/sides',
				'core-concepts/dependencies',
			],
		},
		{
			type: 'category',
			label: 'Creating Content',
			collapsed: false,
			items: [
				'core-concepts/creating-items',
				'core-concepts/creating-blocks',
				'core-concepts/creating-recipes',
				'core-concepts/creating-sounds-particles',
			],
		},
		{
			type: 'category',
			label: 'Events & Registration',
			collapsed: false,
			items: [
				'core-concepts/events-guide',
				'core-concepts/events-reference',
				'core-concepts/registration',
			],
		},
		{
			type: 'category',
			label: 'Editor & Cinematics',
			collapsed: false,
			items: [
				'systems/editor',
				'systems/cutscenes',
			],
		},
		{
			type: 'category',
			label: 'World & Progression',
			collapsed: false,
			items: [
				'systems/instanced-worlds',
				'systems/stages',
				'systems/game-sessions',
				'systems/areas',
				'systems/waypoints',
			],
		},
		{
			type: 'category',
			label: 'Rendering & Effects',
			collapsed: false,
			items: [
				'systems/particles',
				'systems/post-processing',
				'systems/mesh-rendering',
				'systems/skybox',
				'systems/node-editor',
				'systems/easing',
			],
		},
		{
			type: 'category',
			label: 'Interaction',
			collapsed: false,
			items: [
				'systems/dialogue',
				'systems/markdown',
				'systems/nbt-suggestions',
				'systems/tooltips',
				'systems/audio-streaming',
			],
		},
		{
			type: 'category',
			label: 'Reference',
			collapsed: false,
			items: [
				'systems/persistence',
				'systems/commands',
			],
		},
		{
			type: 'category',
			label: 'For Java Developers',
			collapsed: false,
			items: [
				'advanced/index',
				'advanced/addon-api',
				'advanced/data-generation',
				'advanced/network-packets',
				'advanced/editor-themes',
			],
		},
		{
			type: 'category',
			label: 'Examples',
			collapsed: false,
			items: [
				'examples/index',
				'examples/showcase-bundle',
			],
		},
		{
			type: 'category',
			label: 'Contributing',
			collapsed: true,
			items: ['contributing'],
		},
	],
};

export default sidebars;
