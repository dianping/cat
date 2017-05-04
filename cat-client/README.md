### 关于eclipse中设置环境

 mvn clean install -Dmaven.test.skip=true
 执行后，
 
 会在目录cat\cat-client\target\generated-sources\dal-model下面生成一些需要的类
 
 （为什么没在\src\main\java下生成这些类呢？）
 
需要在eclipse环境设置添加以上目录到Java Build Path中。


Java Build Path -> 添加文件夹路径->target/generated-sources/dal-model