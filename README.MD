# 说明

# 目录结构
````
root
|--distribution //打包发布子模块
|    |--benchmark
|    |--bin  //启停脚本,crontab
|    |--conf //配置文件,日志
|    |--pom.xml
|
|--server       //功能子模块
|    |--src
|    |--pom.xml
|    
|--pom.xml

````
# 打包和部署
## 全量发布
首次部署时，需全量打包，如下
````
mvn -Prelease-all clean package -Dmaven.test.skip=true
````
打包生成的文件为{root}/distribution/target/st.tar.gz，文件内部已组织好部署所需的bin,conf,lib目录，首次部署直接拷贝解压即可。
## 仅发布server
更新发布时，只需重打包st-server.jar即可
````
mvn clean package -Dmaven.test.skip=true

# 如果依赖包有变更，需重新获取依赖包，命令如下
#mvn clean dependency:copy-dependencies  -DoutputDirectory=target/lib package -Dmaven.test.skip=true
````
打包生成文件为{root}/server/target/st-server-1.0-SNAPSHOT.jar