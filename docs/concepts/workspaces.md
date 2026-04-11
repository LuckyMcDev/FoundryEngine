# Concepts: Workspaces

## General Info

A Workspace is in the Foundry Engine context:

1. Either the folder inside the `.minecraft/FoundryEngine/bundles/bundleName` in this case called bundleName,
2. Or when using the TemplateBundle, the `src/main/groovy` & `src/main/resources` folders.

If I referer to "scripts directory", that can mean either one.
Same with Resource- and Data-Pack.

## Location of the `bundleName.bundles.toml` File

I will refer to the `bundleName.bundles.toml` as toml file.

When using The TemplateBundle way, the toml file is located in `src/main/resources/tomlFile`.

When using the `.minecraft` way, the toml file is located in `.minecraft/FoundryEngine/bundles/bundleName/tomlFile`.
