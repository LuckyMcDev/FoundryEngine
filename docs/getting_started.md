# Getting Started with Foundry Engine.

This Section guides you through setting up Your workspace and how to test
and run your Bundles.

If you do not know what is meant by Bundle, please read [This Article](concepts/bundles.md)

## Prerequisites

If you want to start developing a Bundle, it is recommended to read everything in the "Concepts"
tab, to get a feel for how Foundry Engine works. You can find that [Here](concepts/index.md)

## Setting Up the Workspace

For setting up a Workspace, id firstly recommend you to read up on [This](concepts/workspaces.md)
And then deciding which way you want to go, since there are two main Ways to go about this.

1. Copy the TemplateBundle found on GitHub. You can do this either by downloading the Zip file or cloning the project,
   or just using the template on GitHub.
2. Raw-Dog it and create a new folder in your .minecraft folder. This is a lot harder and does not give you some ease of
   use features of the TemplateBundle.

## Customizing your Bundles information

To change the bundles Information, you need to change
the [Toml File](concepts/workspaces.md#location-of-the-bundlenamebundlestoml-file).

The contents of this File should look similar to this:

````toml
[[bundles]]
bundleId = "templatebundle"
version = "0.0.1"
displayName = "Template Bundle"
displayURL = "change to your website URL"
authors = "YourName"
description = '''This is a Template Bundle'''
````

Most of the data is just for the "Mods" menu to be displayed visually.

## Building and Testing Your Bundle