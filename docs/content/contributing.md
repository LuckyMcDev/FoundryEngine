# Contributing to the Documentation

This is a non-exhaustive guideline for making contributions to the documentation repository. Contributions can be made by forking and cloning the repository and then added via a pull request, or PR, on GitHub.

You can run the website locally using [npm](https://www.npmjs.com/). It is recommended to use a Node Version Manager like [nvm](https://github.com/nvm-sh/nvm) (Mac, Linux) or [nvs](https://github.com/jasongin/nvs) (Windows) to set up and install npm and Node. From there, you can run the following commands:

```bash
nvm use  # or nvs use on Windows
npm install
npm run docs:dev
```

## Principles

This documentation is a guide to help a developer understand and implement a given concept. This documentation is **not** meant as a tutorial, allowing a developer to copy-paste the examples. If you are looking for a tutorial, there are plenty of videos and pages, which are not linked here, that you can use and follow along with.

This documentation is also **not** meant as documentation for a class. Providing a description of an element is unavoidable when writing a guide; however, if you would like to document a class, you should contribute to the source code's inline documentation or the project's API reference.

Finally, this documentation is **not** meant to explain basic programming concepts. This documentation is intended for people who already have a solid basis in the relevant language and framework. If a concept needs to be explained to better understand the topic, a link should be provided to the original resource. Otherwise, if you are unfamiliar with the language, there are plenty of online resources to learn from.

## Concepts

Each page should guide a developer on a particular concept. If the concept is too large in scope, the concept should be split into separate sub-concepts, each within its own page. For example, if you are writing a cookbook, there can be a page for each recipe, rather than a single page containing all the recipes.

When describing a concept, you should first introduce:
- **What** the concept is
- **Where** it is used
- **Why** it should be used
- **How** to use it

Each section within a concept should have a header. A section can also be broken into sub-sections if necessary. For example, each recipe within a cookbook can have a sub-section for ingredients and the recipe itself.

If you need to refer to other concepts, the relevant page should be linked along with a summary and/or some example to understand the application.

## Examples

Code examples should generally be pseudocode-like objects meant to enhance the understanding of a developer. For this documentation, pseudocode-like refers to code blocks written in the structure and syntax of the desired language with comments used as placeholders for specific logic that the developer may choose to implement themselves.

The code blocks do not necessarily need to be compilable, but each line should have valid syntax and structure of the desired language.

When implementing a method, it is usually specific to the desired goal a developer is trying to achieve. As a guide, this documentation aims to be somewhat agnostic to a developer's specific goal, instead covering the general use case.

Let's say we are using a method called `#applyDiscount` to take some value off the current price. Not everyone will implement the same logic within the method. So, the pseudocode can leave a comment mentioning what to do instead:

```java
// In some class
public float applyDiscount(float price) {
    float newPrice = price;
    // Apply discount to newPrice
    // ...
    return newPrice;
}
```

> **Tip:** If the pseudocode is not explanatory enough to understand the concept, then a full code example can be used instead. A full code example should supply dummy values and explain what they represent.

## Version-Specific Changes

If a change occurs between versions of the framework or library, then relevant changes in the documentation should be split into separate sections or put into tabs. This maintains the accuracy of the information depending on the version the developer is currently targeting.

Docusaurus supports [tabs](https://docusaurus.io/docs/markdown-features/tabs) via the `Tabs` component:

```jsx
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs>
    <TabItem value="latest" label="Latest" default>
        public void latestMethod() {
        // ...
    }
    </TabItem>
    <TabItem value="v2" label="v2.x">
        public void previousMethod() {
        // ...
    }
    </TabItem>
    <TabItem value="v1" label="v1.x">
        public void firstMethod() {
        // ...
    }
    </TabItem>
</Tabs>
```

## Versioning by Minecraft Version

The site uses Docusaurus [versioning](https://docusaurus.io/docs/versioning) to ship docs **per Minecraft version**. Each MC version that FoundryEngine supports gets its own frozen snapshot of the docs, switchable from a dropdown in the navbar (when more than one version exists).

- The `docs/content/` folder is the **current** version. It represents the newest Minecraft version the docs describe and is served at the site root (e.g. `/FoundryEngine/features`).
- Released MC versions are frozen into `docs/versioned_docs/version-<mc>/` with a matching sidebar in `docs/versioned_sidebars/`. Their URLs are versioned (e.g. `/FoundryEngine/26.1/features`).
- `docs/versions.json` lists every frozen version, newest first.

### Cutting a new MC version

1. Make sure `docs/content/` describes the **currently released** MC version and is ready to be frozen.
2. Freeze it before changing any content for the next version:
   ```bash
   npm run docs:version 26.1
   ```
   This copies `content/` into `versioned_docs/version-26.1/`, generates `versioned_sidebars/version-26.1-sidebars.json`, and appends `26.1` to `versions.json`.
3. In `docusaurus.config.js`, update the current version label to the next MC version the engine targets:
   ```js
{
     {
       'MC 26.2',
     },
   },
   ```
4. (Optional) Open `versioned_docs/version-26.1/` and trim anything that is not relevant to that MC version.
5. Update the docs inside `docs/content/` for the new MC version, and bump anything in the sidebar or going forward.
6. Verify with `npm run docs:build`.

> **Order matters:** run `docs:version` *before* editing `docs/content/` for the new version, otherwise the frozen snapshot picks up the new content.

### Editing existing / old versions

Each `versioned_docs/version-<mc>/` folder is an independent route. Edits there only affect that MC version. Typical uses: applying a patch to a backported MC version, or removing a page that does not exist in an older version. Remember to update the corresponding `versioned_sidebars/version-<mc>-sidebars.json` whenever you add or remove a page.

### Removing a version

Per the [Docusaurus docs](https://docusaurus.io/docs/versioning#deleting-an-existing-version): remove the name from `versions.json`, then delete the `versioned_docs/version-<mc>/` folder and `versioned_sidebars/version-<mc>-sidebars.json`.

## Adding a Language (i18n)

The site is configured with English as the only locale for now, but the infrastructure is ready. To add a translation:

1. Add the locale to `docusaurus.config.js`:
   ```js
{
     'en',
     locales: ['en', 'de'],
   },
   ```
2. Generate the label skeletons:
   ```bash
   npm run docs:write-translations -- --locale de
   ```
   This creates the foldable label JSON files under `docs/i18n/de/`.
3. Translate the theme labels (navbar, footer) in `docs/i18n/de/docusaurus-theme-classic/` and the docs sidebar labels in `docs/i18n/de/docusaurus-plugin-content-docs/current.json`.
4. Translate page content by mirroring a page under `docs/i18n/de/docusaurus-plugin-content-docs/current/<path>.md`. Untranslated pages automatically fall back to the English version.
5. See the [Docusaurus i18n tutorial](https://docusaurus.io/docs/i18n/tutorial) for the full workflow.

## Style Guide

This documentation uses [Docusaurus](https://docusaurus.io/), which is built on top of [React](https://react.dev/). You can find more detailed information about available features on the [Docusaurus documentation](https://docusaurus.io/docs/markdown-features).

This style guide will be more focused towards common features and formatting we use in the Markdown files.

### Front Matter

Front matter defines metadata fields which can affect how the page is rendered. This is denoted using `---`, similar to a code block.

The most common front matter fields are:

- `title` — Overrides the page title.
- `description` — Overrides the page description.
- `sidebar_position` — Controls the sidebar order of the page within its section.
- `hide_table_of_contents` — Hides the table of contents (set to `true`).

Example:

```yaml
---
title: Getting Started
description: A guide to getting started with the project
sidebar_position: 1
---
```

#### Categories

Categories are folders within the documentation. They inherit titles and positional data from the `index.md` file. The sidebar order can be controlled using the `sidebar_position` front matter field or via the `sidebars.js` configuration.

### Titles

Titles are defined using up to six hashtags (`#`) to define each section. Titles should capitalize everything but unimportant words.

```markdown
# Guide For Contributing to This Documentation

### Building and Testing Your Project
```

### Diction

Spelling, grammar, and syntax should follow those in American English. Avoid using contractions in sentences; use two separate words ("is not" instead of "isn't"). Additionally, avoid using pronouns (e.g., I, me, you) when possible, unless you need to directly refer to the reader. Demonstratives (e.g., this, that, its) should be used sparingly to avoid confusing the reader. Prefer using the actual object or noun being referred to.

### Paragraphs

Paragraphs should be a continuous block, separated by a newline. Paragraphs should **not** have each sentence be on a new line.

This is my first paragraph. See how the next sentence is on the same line? You can use word wrapping in your editor to stop the line from going off the screen.

This is my next paragraph. It is separated by a new line.

### Indentation

When indenting lines, use two spaces instead of tabs. Most Markdown features require spaces to recognize indentation, so it allows consistency across the document.

```markdown
- Hello World
- Two Spaces In
```

### Emphasis

Emphasizing words should be done using bold or italics. Please use two asterisks (`**`) for bold and a single asterisk (`*`) or underscore (`_`) for italics to make the separation in Markdown more distinct.

This is a **bolded** word. This is an _italicized_ word.

### Code References

When referencing elements outside of code blocks, they should be surrounded with backticks (`` ` ``).

- Classes should use their simple name: `` `MyClass` ``
- Methods and fields should specify the class name followed by a `#`: `` `MyClass#foo` ``
- If the class name is implied, the method or field can simply be prefixed with `#`: `` `#SOME_CONSTANT` ``
- Inner classes should specify the name of the outer class followed by a `.`: `` `MyClass.InnerClass` ``

Code blocks should specify the language after the triple backtick (```). Docusaurus uses [Prism](https://prismjs.com/) for syntax highlighting, which supports a wide range of languages.

````markdown
```java
public void run() {
    // ...
}
```

```json
{
    "text": "Hiya"
}
```
````

### Links

All links should use reference-style links when possible, with the URL specified at the bottom of the page:

```markdown
There are [two] different types of [link references][linkref].

[two]: https://linkrefwithoutref.donotclick
[linkref]: https://linkref.donotclick
```

### Admonitions

Docusaurus supports [admonitions](https://docusaurus.io/docs/markdown-features/admonitions) using three colons (`:::`) and specifying its type:

```markdown
:::tip
This is a tip!
:::

:::warning
This is a warning.
:::

:::danger
This is a dangerous warning.
:::

:::note
This is a note.
:::
```

Output:

:::tip
This is a tip!
:::

:::warning
This is a warning.
:::

:::danger
This is a dangerous warning.
:::

:::note This is a note.
:::