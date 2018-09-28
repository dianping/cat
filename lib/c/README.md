# Cat Client for C

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

You also need to have `cmake` and `make` installed, which we use for building static or dynamic libraries and executable binary files.

Once you have your environment ready, it's easy to build and install `ccat`.

(In the project root dir, which contains CMakeLists.txt)

```
mkdir -p cmake
cd cmake
make -j 4
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

With all the preparations have been done, it's easy to initialize `ccat` in your c codes.

```c
#include <client.h>

catClientInit("appkey");
```

> Only English characters (a-z, A-Z), numbers (0-9), underscore (\_) and dash (-) is allowed in appkey.

Note that `sampling`, built-in `heartbeat` and `binary` encoder is enabled by default, which you may want to disable it. We also offered an API to customize your initialization, please refer to our [API doc](./docs/api.md).

## Documentation

[API doc](./docs/api.md)
