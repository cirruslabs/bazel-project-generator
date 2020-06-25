# How to regenerate BUILD files

```bash
bazel build //:cmd 
bazel-bin/cmd --source-content-root src
```
