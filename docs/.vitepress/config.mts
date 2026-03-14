import { defineConfig } from 'vitepress'

// https://vitepress.dev/reference/site-config
export default defineConfig({
  title: "FoundryEngine",
  description: "Foundry Engine Docs",
  base: '/FoundryEngine',
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    nav: [
      { text: 'Docs', link: '/' },
      { text: 'Javadoc', link: '/javadoc/index.html' }
    ],

    sidebar: [
      {
      }
    ],

    socialLinks: [
      { icon: 'github', link: 'https://github.com/LuckyMcDev/FoundryEngine' }
    ]
  }
})