# Cat Client for C++

[中文文档](./README.zh-CN.md)

The `cppcat` can be compiled and used both on Linux (both glibc and musl-libc) and OSX.

The following Operating Systems are tested:

* OSX (>=10.13)
* Alpine linux
* CentOS 6
* CentOS 7
* Ubuntu 14.04 LTS
* Ubuntu 16.04 LTS
* Ubuntu 18.04 LTS

## Compilation

You need to have a C++ compiler (supporting C++11) installed.

You also need to have `cmake` and `make` installed, which are used for building static or dynamic libraries and executable binary files.

Once you have your environment ready, it's easy to build and install `cppcat`.

(In the project root dir, which contains CMakeLists.txt)

```
mkdir -p cmake
cd cmake
make -j 4
```

Build test cases if you want.

Since we use [googletest](https://github.com/google/googletest) as the test framework, it has to be installed first.

```
make -j 4 -DBUILD_TEST=1
```

### Installation

The following command will install libcatclient.so (or .dylib in osx) to your `LD_LIBRARY_PATH`, which is `/usr/local/lib` in most cases.

```
make install
```

Now it can be used as a built-in shared library.
```
g++ -lcatclient x.cpp
```

## Initialization

Some [preparations](../_/preparations.md) needs to be done before initializing `cppcat`.

With all the preparations done, it's easy to initialize `cppcat` in your c++ codes.

```cpp
#include <client.h>

cat::init("appkey");
```

> Only English characters (a-z, A-Z), numbers (0-9), underscore (\_) and dash (-) are allowed in appkey.

Note that `sampling`, built-in `heartbeat` and `binary` encoder are enabled by default, which you may want to disable it. We also offered an API to customize your initialization, please refer to our [API doc](./docs/api.md).

## Documentation

[API doc](./docs/api.md)
