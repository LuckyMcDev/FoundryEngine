import {defineConfig} from 'vitepress'

export default defineConfig({
    title: "FoundryEngine",
    description: "A Minecraft mod that turns the game into a game engine — with an in-game editor, visual scripting, custom dimensions, cutscenes, and more.",
    base: '/FoundryEngine/',

    srcExclude: ['**/javadoc/**'],

    head: [
        ['link', { rel: 'icon', href: '/FoundryEngine/favicon.ico' }]
    ],

    themeConfig: {
        logo: '/FoundryEngine/logo.svg',

        nav: [
            { text: 'Guide', link: '/guide' },
            { text: 'Getting Started', link: '/getting_started' },
            { text: 'Examples', link: '/examples' },
            { text: 'GitHub', link: 'https://github.com/LuckyMcDev/FoundryEngine' }
        ],

        sidebar: {
            '/guide': [
                {
                    text: 'User Guide',
                    items: [
                        { text: 'Installation', link: '/guide' },
                        { text: 'Getting Started', link: '/getting_started' }
                    ]
                }
            ],
            '/getting_started': [
                {
                    text: 'Getting Started',
                    items: [
                        { text: 'Overview', link: '/getting_started' },
                        { text: 'Workspaces', link: '/concepts/workspaces' }
                    ]
                }
            ],
            '/concepts/': [
                {
                    text: 'Core Concepts',
                    items: [
                        { text: 'Overview', link: '/concepts/index' },
                        { text: 'Bundles', link: '/concepts/bundles' },
                        { text: 'Builders', link: '/concepts/builders' },
                        { text: 'Registries', link: '/concepts/registries' },
                        { text: 'Scripts', link: '/concepts/scripts' },
                        { text: 'Entrypoints', link: '/concepts/entrypoint' },
                        { text: 'Events', link: '/concepts/events' },
                        { text: 'Blueprints', link: '/concepts/blueprints' },
                        { text: 'Dependencies', link: '/concepts/dependencies' },
                        { text: 'Sides', link: '/concepts/sides' },
                        { text: 'Bundle Config', link: '/concepts/config' },
                        { text: 'Easing Functions', link: '/concepts/easing' },
                        { text: 'Markdown Rendering', link: '/concepts/markdown' }
                    ]
                },
                {
                    text: 'Systems',
                    items: [
                        { text: 'In-Game Editor', link: '/concepts/editor' },
                        { text: 'Cutscene System', link: '/concepts/cutscenes' },
                        { text: 'Instanced Worlds', link: '/concepts/instanced-worlds' },
                        { text: 'Game Stages', link: '/concepts/stages' },
                        { text: 'Areas', link: '/concepts/areas' },
                        { text: 'Waypoints', link: '/concepts/waypoints' },
                        { text: 'Custom Particles', link: '/concepts/particles' },
                        { text: 'Post-Processing', link: '/concepts/post-processing' }
                    ]
                },
                {
                    text: 'Reference',
                    items: [
                        { text: 'Commands', link: '/concepts/commands' }
                    ]
                }
            ],
            '/examples': [
                {
                    text: 'Examples',
                    items: [
                        { text: 'Overview', link: '/examples' }
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
    }
})
