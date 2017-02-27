# Play Framework 接入 CAT 说明

## 出发点

由于当前版本的Play Framework与Servlet不兼容，实现了自己的一套HTTP Context，在网上寻找许久没有找到合适的两套Context互转的工具，于是决定将CAT的servlet的filter在Play Framework中重写一遍

## 文件结构

* filters 目录： 存放所有的handlers和Cat filter相关的定义
* META-INF ： 查了很多资料，没有找到Play Framework怎么在生成的jar包里添加META-INF里的信息，由于CAT依赖 META-INF/app.properties, 于是在项目的根目录（和build.sbt一层）添加该文件夹，编译之后再通过一个脚本把app.properties 添加进去
* cat-support.sh： 编译之后执行该脚本，将 app.properties添加进去， 主要正确修改这个文件中的 ‘项目名’，通常这个项目名是在build.sbt中的 [name]-[version]格式

## 操作
文件都引用之后：

1. 修改META-INF/app.properties里的app.name
2. 修改cat-support.sh中的项目名
3. 把CatFilter加到项目的Filter链中
4. 编译之后运行 cat-support.sh，压缩文件依然在target/universal/ 下。其他配置没有区别。