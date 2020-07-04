# Bazel BUILD files generator for JVM with package-level granularity

[![Build Status](https://api.cirrus-ci.com/github/cirruslabs/bazel-project-generator.svg)](https://cirrus-ci.com/github/cirruslabs/bazel-project-generator)

This project aims not to provide a silver bullet for converting your Gradle or Maven build configurations to Bazel but
instead it aims to help automate the most annoying 90+% of work required to support dependencies in your BUILD files 
for Kotlin/Java project. This Generator only relies on parsing your source code for figuring out package-level dependencies.

# Example Projects Using the Generator

* [fkorotkov/microservices](https://github.com/fkorotkov/microservices) - GRPC Kotlin micro services in a monorepo

# How to use

First of all a `WORKSPACE` file is needed to be created to define `rules_jvm_external` and `rules_kotlin` of your choice
(you can take a look at [`WORKSPACE`](https://github.com/cirruslabs/bazel-project-generator/blob/master/WORKSPACE)
file of this project or any other example projects from above).

## External Maven Dependencies

All external maven dependencies should be defined in `dependencies.json` file in the root of your repository.
Generator will translate `dependencies.json` to `maven_install` in `3rdparty/jvm/workspace.bzl` which you'll need
to load in your `WORKSPACE` file:

```python
load("//3rdparty/jvm:workspace.bzl", "jvm_dependencies")
jvm_dependencies()
```

`dependencies.json` file format is pretty simple and can be generated for your Gradle project via [this plugin](https://github.com/fkorotkov/gradle-libraries-plugin):

```json
{
  "repositories": [
    "https://repo1.maven.org/maven2"
  ],
  "libraries": [
    {
      "group": "com.google.code.gson",
      "name": "gson",
      "version": "2.8.6"
    }
  ]
}
```

## BUILD files generation

Please clone and build this project:

```bash
bazel build //:cmd 
```

Now you can try to generate BUILD files for your project:

```bash
bazel-bin/cmd --workspace-root ~/worspace/my-project --source-content-root module1/src --source-content-root module2/src
```

**Note:** if none of the `--source-content-root` are passed the generator will try to find recursively all `src` folders inside `--workspace-root`.

### Generation options

* `--source-content-root` - path to root of your project where [`WORKSPACE` file from above](#how-to-use) was created.
* `--source-content-root` - relative or absolute paths to folder where Generator will look for source roots (Generator expects Maven project structure ).
* `--dependencies` - path to a JSON file with dependencies (defaults to `dependencies.json`). 
* `--caching` - Downloaded jar files will be cached in `.cache/maven` folder.
* `--dry-run` - Run everything without generating BUILD files.

# Questions

Feel free to open issues with feature request and reports. Also don't hesitate to reach out on [Twitter](https://twitter.com/fedor).
