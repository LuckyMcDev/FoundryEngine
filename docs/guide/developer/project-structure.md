# Project Structure

The Project Structure of a Bundle looks like this:

```
├── src
│   ├── main
│   │   ├── groovy
│   │   │   └── com
│   │   │       └── example
│   │   │           └── mybundle
│   │   │               └── MyBundle.groovy
│   │   └── resources
│   │       └── assets
│   │           └── ...
│   │       └── data
│   │           └── ...
│   │       └── mybundle.bundles.toml
├── build.gradle
└── gradle.properties
```

- `src/main/groovy` - This is where you will write your Bundle's code. You can create as many packages and classes as
  you need here.
- `src/main/resources` - This is where you will put any assets or data files that your Bundle needs. The
  `assets` folder acts as a minecraft resource pack, and the `data` folder acts as a minecraft datapack.
  The resources folder also contains the `${bundle name}.bundles.toml` file, which is
  the [Configuration](configuration.md) file for your Bundle. 