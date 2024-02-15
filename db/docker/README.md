# Build image

```
docker build --platform linux/x86_64 -t centyllion/db .
```

# Transfer image

```
docker save centyllion/db | bzip2 | ssh centyllion.com docker load
```
