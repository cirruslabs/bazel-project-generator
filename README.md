# Bazel BUILD files generator for Kotlin

This project aims not to provide a silver bullet for converting your Gradle or Maven build configurations to Bazel but
instead it aims to help automate 99% of work required to support dependencies in your BUILD files for Kotlin/Java project.
This Generator only relies on parsing your source code for figuring out package-level dependencies.

# How to use

First of all a `WORKSPACE` file need to be created to define `rules_jvm_external` and `rules_kotlin` of your choice
(you can take a look at [`WORKSPACE`](https://github.com/cirruslabs/bazel-project-generator/blob/master/WORKSPACE)
file of this project).

## External Maven Dependencies

All external maven dependencies should be defined in `dependencies_jvm.json` file in the root of your repository.
Generator will translate `dependencies_jvm.json` to `maven_install` in `3rdparty/jvm/workspace.bzl` which you'll need
to load in your `WORKSPACE` file:

```python
load("//3rdparty/jvm:workspace.bzl", "jvm_dependencies")
jvm_dependencies()
```

`dependencies_jvm.json` file format is pretty simple:

```json
{
  "repositories": [
    "https://repo1.maven.org/maven2"
  ],
  "libraries": [
    {
      "group": "com.google.code.gson",
      "name": "gson",
      "version": "2.8.6",
      "packagePrefixes": [
        "com.google.gson"
      ]
    }
  ]
}
```

The only caveat is that you'll need to manually define `packagePrefixes` which a particular library contains. At some point
Generator will [support `maven_install.json`](https://github.com/cirruslabs/bazel-project-generator/issues/5) directly.

## BUILD files generation

Please clone and build this project:

```bash
bazel build //:cmd 
```

Now you can try to generate BUILD files for your project:

```bash
bazel-bin/cmd --workspace-root ~/worspace/my-project --source-content-root module1/src --source-content-root module2/src
```

### Generation options

* `--source-content-root` - path to root of your project where [`WORKSPACE` file from above](#how-to-use) was created.
* `--source-content-root` - relative or absolute paths to folder where Generator will look for source roots (Generator expects Maven project structure ).
* `--dependencies` - path to a JSON file with dependencies (defaults to `dependencies_jvm.json`). 

# Questions

Feel free to open issues with feature request and reports. Also don't hesitate to reach out on [Twitter](https://twitter.com/fedor).
