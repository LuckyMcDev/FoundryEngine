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

VitePress supports [tabs](https://vitepress.dev/guide/markdown#code-groups) via code groups:

````markdown
::: code-group

```java [Latest]
public void latestMethod() {
    // ...
}
```

```java [v2.x]
public void previousMethod() {
    // ...
}
```

```java [v1.x]
public void firstMethod() {
    // ...
}
```
:::
````

Output:

::: code-group

```java [Latest]
public void latestMethod() {
    // ...
}
```

```java [v2.x]
public void previousMethod() {
    // ...
}
```

```java [v1.x]
public void firstMethod() {
    // ...
}
```
:::

## Style Guide

This documentation uses [VitePress](https://vitepress.dev/), which is built on top of [Vue](https://vuejs.org/) and [Vite](https://vitejs.dev/). You can find more detailed information about available features on the [VitePress documentation](https://vitepress.dev/guide/what-is-vitepress).

This style guide will be more focused towards common features and formatting we use in the Markdown files.

### Front Matter

Front matter defines metadata fields which can affect how the page is rendered. This is denoted using `---`, similar to a code block.

The most common front matter fields are:

- `title` — Overrides the page title.
- `description` — Overrides the page description.
- `sidebar` — Controls sidebar behavior (e.g., `sidebar: auto` for automatic sidebar generation).
- `outline` — Controls the table of contents depth.

Example:

```yaml
---
title: Getting Started
description: A guide to getting started with the project
sidebar: auto
outline: deep
---
```

#### Categories

Categories are folders within the documentation. They inherit titles and positional data from the `index.md` file. The sidebar order can be controlled using the `sidebar` configuration in the VitePress config file or via front matter.

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

Code blocks should specify the language after the triple backtick (```). VitePress uses [Shiki](https://shiki.matsu.io/) for syntax highlighting, which supports a wide range of languages.

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

### Admonitions / Custom Containers

VitePress supports [custom containers](https://vitepress.dev/guide/markdown#custom-containers) using three colons (`:::`) and specifying its type:

```markdown
::: tip
This is a tip!
:::

::: warning
This is a warning.
:::

::: danger
This is a dangerous warning.
:::

::: details
This is a details block.
:::
```

Output:

::: tip
This is a tip!
:::

::: warning
This is a warning.
:::

::: danger
This is a dangerous warning.
:::

::: details
This is a details block.
:::