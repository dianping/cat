# Cat Client C & C++

## Building & Installation

The cat client can be compiled and used on Linux (both glibc and musl-libc) and OSX.

You need to have a C compiler (supporting C99) and a C++ compiler (supporting C++11) if you want to run test cases or build c++ version of cat client.

You also need to have `cmake` and `make` installed, which we use for building static or dynamic libraries, and executable binary files (like test or benchmark script).

Upon you get your environment ready, it's easy to build and install ccat.

(In the project root dir, which contains CMakeLists.txt)

```
mkdir -p cmake
cd cmake
make -j 4
```

Build test cases if you want. ([googletest]() and a c++ compiler is required)

```
make -j 4 -DBUILD_TEST=1
```

Build c++ version of cat client.

```
make -j 4 -DBUILD_TYPE=CPP
```

### Installation

This command will install libcatclient.so (or .dylib in osx) to `LD_LIBRARY_PATH`, which in most cases is `/usr/local/lib`

```
make install
```

and it can be used as a built-in shared library
```
gcc -lcatclient x.c
```

## Initialization

First of all, you have to create `/data/appdatas/cat` directory, read and write permission is required (0644).`/data/applogs/cat` is also required if you'd like to preserve a debug log, it can be very useful while debugging.

And create a config file `/data/appdatas/cat/client.xml` with the following contents.

```xml
<?xml version="1.0" encoding="utf-8"?>
<config xmlns:xsi="http://www.w3.org/2001/XMLSchema" xsi:noNamespaceSchemaLocation="config.xsd">
    <servers>
        <server ip="<cat server ip address>" port="2280" http-port="8080" />
    </servers>
</config>
```

Don't forget to change the `<cat server IP address>` to your own after you copy and paste the contents.

With all the preparations have done, It's easy to initialize it in your c codes.

```c
#include <ccat/client.h>

catClientInit("appkey");
```

> Only English characters, numbers, underscore and dash is allowed in appkey.

If you are using our cpp version, you can initialize it by following codes.

```cpp
#include <cppcat/client.h>

cat::init("appkey");
```

We also have many initialize options, like enable text-encoder (adapt to lower server version), disable sampling or heartbeat which is enabled by default.

See [this link]()

## Documentation

[ccat](./doc/api-c.md)

[cppcat](./doc/api-cpp.md)
