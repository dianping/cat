# Cat Client for C++

`cppcat` 同时支持 Linux (glibc 和 musl-libc) 和 OSX 两个平台。

下述列出的操作系统是经过测试可用的：

* OSX (>=10.13)
* Alpine linux
* CentOS 6
* CentOS 7
* Ubuntu 14.04 LTS
* Ubuntu 16.04 LTS
* Ubuntu 18.04 LTS

## 编译

你需要安装一个支持 C++11 的 C++ 编译器。

你同时还需要安装 `cmake` 和 `make`，这是我们用来构建动态或静态链接库以及可执行文件的工具。

当你准备好你的环境之后，编译安装 `cppcat` 就很容易了。

(在项目根目录下，就包含 CMakeList.txt 的那个)

```
mkdir -p cmake
cd cmake
make -j 4
```

你也可以尝试构建我们的测试用例。

由于我们使用了 [googletest](https://github.com/google/googletest) 作为我们的测试框架，你需要先安装它。

### 安装

下面的命令将会把 `libcatclient.so`（或者在 osx 下是 .dylib）安装到你的 `LD_LIBRARY_PATH` 目录下，大多数情况下是 `/usr/local/lib`。

```
make install
```

然后你就可以像使用一个内置动态库一样使用 `cppcat` 了。

```
gcc -lcatclient x.cpp
```

## 初始化

一些[准备工作](../_/preparations.zh-CN.md)需要在初始化 `cppcat` 之前完成。

当你完成这些准备工作后，在你的 c++ 代码中初始化 `cppcat` 就很简单了。

```c
#include <client.h>

cat::init("appkey");
```

> appkey 只能包含英文字母 (a-z, A-Z)、数字 (0-9)、下划线 (\_) 和中划线 (-)

注意，**采样**，内置**心跳**，**二进制**序列化在默认情况下是开启的，你可能会想要禁用他们。我们同时提供了一个 API 可以使你自定义启动参数，请参考 [API 文档](./docs/api.zh-CN.md)

## Documentation

[API 文档](./docs/api.zh-CN.md)
