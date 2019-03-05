# Cat Client for C

Current latest version: 3.1.0 [changelog](./CHANGELOG.md)

[中文文档](./README.zh-CN.md)

The `ccat` can be compiled and used on both Linux (both glibc and musl-libc) and OSX.

The following Operating Systems are tested:

* OSX (>=10.13)
* Alpine linux
* CentOS 6
* CentOS 7
* Ubuntu 14.04 LTS
* Ubuntu 16.04 LTS
* Ubuntu 18.04 LTS

We also offered a cpp version, please refer to [cppcat](../cpp) for further information.

## Compilation

You need to have a C compiler (supporting C99) installed.

You also need to have `cmake` and `make` installed, which are used for building static or dynamic libraries and executable binary files.

Once you have your environment ready, it's easy to build and install `ccat`.

(In the project root dir, which contains CMakeLists.txt)

```
mkdir -p cmake
cd cmake && cmake .. && make -j
```

### Docker integration example

In the ccat project root directory. (current directory)

```bash
docker build -f docker/alpine.df . -t ccat:alpine
docker build -f docker/centos6.df . -t ccat:centos6
docker build -f docker/centos7.df . -t ccat:centos7
docker build -f docker/ubuntu1404.df . -t ccat:ubuntu14.04
docker build -f docker/ubuntu1604.df . -t ccat:ubuntu16.04
docker build -f docker/ubuntu1804.df . -t ccat:ubuntu18.04
```

### Installation

The following command will install libcatclient.so (or .dylib in osx) to your `LD_LIBRARY_PATH`, which is `/usr/local/lib` in most cases.

```
make install
```

Now it can be used as a built-in shared library.
```
gcc -lcatclient x.c
```

## Initialization

Some [preparations](../_/preparations.md) needs to be done before initializing `ccat`.

With all the preparations done, it's easy to initialize `ccat` in your c codes.

```c
#include <client.h>

catClientInit("appkey");
```

> Only English characters (a-z, A-Z), numbers (0-9), underscore (\_) and dash (-) are allowed in appkey.

Note that `sampling`, built-in `heartbeat` and `binary` encoder are enabled by default, which you may want to disable it. We also offered an API to customize your initialization, please refer to our [API doc](./docs/api.md).

## Documentation

[API doc](./docs/api.md)
