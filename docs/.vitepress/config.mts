import { defineConfig } from 'vitepress'

const nav = (text: string, link: string) => ({ text, link })
const sidebar = (text: string, link: string) => ({ text, link })
const social = (icon: string, link: string) => ({ icon, link })

const sidebarGroup = (text: string, items: Array<any>, collapsed: boolean = false) => ({
  text,
  items,
  collapsed
})

export default defineConfig({
  title: "FoundryEngine",
  description: "Foundry Engine Docs",
  base: '/FoundryEngine/',

  // Exclude javadoc from the Vite build process entirely
  srcExclude: ['**/javadoc/**'],

  themeConfig: {
    nav: [
        nav('Guide', '/guide/'),
        nav('API Reference', '/api/'),
        nav('Javadoc', '/javadoc/index.html')
    ],

    sidebar: [
      sidebarGroup('Guide', [
        sidebar('Overview', '/guide/'),
        sidebarGroup("Developer", [
            sidebar("Getting Started", '/guide/developer/getting-started'),
            sidebar("Project Structure", '/guide/developer/project-structure'),
            sidebar("Configuration", '/guide/developer/configuration'),
            sidebar("Minecraft Code", "/guide/developer/minecraft-code")
        ]),
        sidebarGroup("User", [
            sidebar("Installation", '/guide/user/installation')
        ])
      ]),

      sidebarGroup('API Reference', [
          sidebar("Overview", "/api/"),
          sidebarGroup("Builders", [
            sidebar("Item Builder", "/api/builder/item-builder"),
            sidebar("Block Builder", "/api/builder/block-builder"),
            sidebar("Recipe Builder", "/api/builder/recipe-builder")
          ])
      ]),
    ],

    socialLinks: [
      social('github', 'https://github.com/LuckyMcDev/FoundryEngine'),
    ]
  }
})