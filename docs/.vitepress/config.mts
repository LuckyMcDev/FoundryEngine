import {defineConfig} from 'vitepress'

export default defineConfig({
    title: "FoundryEngine",
    description: "A Minecraft mod that turns the game into a game engine — with an in-game editor, visual scripting, custom dimensions, cutscenes, mesh rendering, and more.",
    base: '/FoundryEngine/',

    srcExclude: ['**/javadoc/**'],

    head: [
        ['link', { rel: 'icon', href: '/FoundryEngine/favicon.ico' }]
    ],

    themeConfig: {
        logo: '/FoundryEngine/logo.png',

        nav: [
            { text: 'Home', link: '/' },
            { text: 'Guide', link: '/guide/' },
            { text: 'Concepts', link: '/concepts/core/' },
            { text: 'Examples', link: '/examples/' },
            { text: 'GitHub', link: 'https://github.com/LuckyMcDev/FoundryEngine' }
        ],

        sidebar: {
            '/guide/': [
                {
                    text: 'Guide',
                    items: [
                        { text: 'Installation', link: '/guide/' },
                        { text: 'Getting Started', link: '/guide/getting-started' },
                        { text: 'Workspaces', link: '/guide/workspaces' }
                    ]
                }
            ],

            '/concepts/core/': [
                {
                    text: 'Core Concepts',
                    items: [
                        { text: 'Overview', link: '/concepts/core/' },
                        { text: 'Bundles', link: '/concepts/core/bundles' },
                        { text: 'Scripts & Entrypoints', link: '/concepts/core/scripts' },
                        { text: 'Builders', link: '/concepts/core/builders' },
                        { text: 'Registries', link: '/concepts/core/registries' },
                        { text: 'Events', link: '/concepts/core/events' },
                        { text: 'Sides', link: '/concepts/core/sides' },
                        { text: 'Dependencies', link: '/concepts/core/dependencies' },
                        { text: 'Bundle Config', link: '/concepts/core/config' }
                    ]
                },
                {
                    text: 'Systems',
                    items: [
                        { text: 'Overview', link: '/concepts/systems/' },
                        { text: 'In-Game Editor', link: '/concepts/systems/editor' },
                        { text: 'Blueprints', link: '/concepts/systems/blueprints' },
                        { text: 'Cutscene System', link: '/concepts/systems/cutscenes' },
                        { text: 'Instanced Worlds', link: '/concepts/systems/instanced-worlds' },
                        { text: 'Game Stages', link: '/concepts/systems/stages' },
                        { text: 'Areas', link: '/concepts/systems/areas' },
                        { text: 'Waypoints', link: '/concepts/systems/waypoints' },
                        { text: 'Custom Particles', link: '/concepts/systems/particles' },
                        { text: 'Post-Processing', link: '/concepts/systems/post-processing' },
                        { text: 'Game Sessions', link: '/concepts/systems/game-sessions' },
                        { text: 'Mesh Rendering & OBJ', link: '/concepts/systems/mesh-rendering' },
                        { text: 'Easing Functions', link: '/concepts/systems/easing' },
                        { text: 'Markdown Rendering', link: '/concepts/systems/markdown' },
                        { text: 'Commands', link: '/concepts/systems/commands' }
                    ]
                },
                {
                    text: 'Advanced',
                    items: [
                        { text: 'Overview', link: '/concepts/advanced/' },
                        { text: 'Java Addon API', link: '/concepts/advanced/addon-api' },
                        { text: 'Editor Themes', link: '/concepts/advanced/themes' },
                        { text: 'Data Generation', link: '/concepts/advanced/data-generation' },
                        { text: 'Network Packets', link: '/concepts/advanced/network' },
                        { text: 'Mixin Architecture', link: '/concepts/advanced/mixins' }
                    ]
                }
            ],

            '/concepts/systems/': [
                {
                    text: 'Systems',
                    items: [
                        { text: 'Overview', link: '/concepts/systems/' },
                        { text: 'In-Game Editor', link: '/concepts/systems/editor' },
                        { text: 'Blueprints', link: '/concepts/systems/blueprints' },
                        { text: 'Cutscene System', link: '/concepts/systems/cutscenes' },
                        { text: 'Instanced Worlds', link: '/concepts/systems/instanced-worlds' },
                        { text: 'Game Stages', link: '/concepts/systems/stages' },
                        { text: 'Areas', link: '/concepts/systems/areas' },
                        { text: 'Waypoints', link: '/concepts/systems/waypoints' },
                        { text: 'Custom Particles', link: '/concepts/systems/particles' },
                        { text: 'Post-Processing', link: '/concepts/systems/post-processing' },
                        { text: 'Game Sessions', link: '/concepts/systems/game-sessions' },
                        { text: 'Mesh Rendering & OBJ', link: '/concepts/systems/mesh-rendering' },
                        { text: 'Easing Functions', link: '/concepts/systems/easing' },
                        { text: 'Markdown Rendering', link: '/concepts/systems/markdown' },
                        { text: 'Commands', link: '/concepts/systems/commands' }
                    ]
                },
                {
                    text: 'Core Concepts',
                    items: [
                        { text: 'Overview', link: '/concepts/core/' },
                        { text: 'Bundles', link: '/concepts/core/bundles' },
                        { text: 'Scripts & Entrypoints', link: '/concepts/core/scripts' },
                        { text: 'Builders', link: '/concepts/core/builders' },
                        { text: 'Events', link: '/concepts/core/events' }
                    ]
                }
            ],

            '/concepts/advanced/': [
                {
                    text: 'Advanced',
                    items: [
                        { text: 'Overview', link: '/concepts/advanced/' },
                        { text: 'Java Addon API', link: '/concepts/advanced/addon-api' },
                        { text: 'Editor Themes', link: '/concepts/advanced/themes' },
                        { text: 'Data Generation', link: '/concepts/advanced/data-generation' },
                        { text: 'Network Packets', link: '/concepts/advanced/network' },
                        { text: 'Mixin Architecture', link: '/concepts/advanced/mixins' }
                    ]
                }
            ],

            '/examples/': [
                {
                    text: 'Examples',
                    items: [
                        { text: 'Overview', link: '/examples/' }
                    ]
                }
            ]
        },

        socialLinks: [
            { icon: 'github', link: 'https://github.com/LuckyMcDev/FoundryEngine' }
        ],

        footer: {
            message: 'FoundryEngine is a work-in-progress. Documentation may change.',
            copyright: 'Copyright © 2025 LuckyMcDev'
        },

        editLink: {
            pattern: 'https://github.com/LuckyMcDev/FoundryEngine/edit/main/docs/:path',
            text: 'Edit this page on GitHub'
        }
    },

    markdown: {
        lineNumbers: true,
        theme: 'github-dark'
    },

    ignoreDeadLinks: true
})
