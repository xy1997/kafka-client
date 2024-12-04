# Kafka Client Tool

该项目提供了一个简单易用的 Kafka 客户端工具，用于连接 Kafka 集群，发送和接收消息。旨在帮助开发者快速集成 Kafka 客户端操作，进行生产环境或测试环境的数据处理。

## 功能

- 连接 Kafka 集群
- 发送消息到 Kafka 主题
- 从 Kafka 主题接收消息
- 支持指定 Kafka 服务器和主题

## 项目结构
    offset-explorer/
    │ 
    ├── src/
    │   ├── main/ 
    │   │   ├── java/
    │   │   │   └── cn/ 
    │   │   │       └── net/
    │   │   │           └── explorer/
    │   │   │               ├── config
    │   │   │               ├── connector
    │   │   │               └── controller
    │   │   │               └── domain
    │   │   │               └── exception
    │   │   │               └── mapper
    │   │   │               └── service
    │   │   │               └── util
    │   │   │               └── ExplorerApplication.java
    │   └── resources/
    │       └── db
    │       └── mapper
    │       └── application.yml
    │       └── logback-spring.xml
    ├── .gitignore
    ├── pom.xml
    ├── README.md
## docker部署
    #拉取镜像
     docker pull crpi-uf6um9mal0ofi1in.cn-beijing.personal.cr.aliyuncs.com/min_tool/kafka-client:1.0.0

    #运行容器
    docker run -itd -p 8080:8080 -p 80:80 --name kafka-client -v /data/kafka-client/:/app/db/  -e ENV_USERNAME=admin -e ENV_PASSWORD=123456 kafka-client:1.0.0
   
    #**注**： _/app/db/ 目录下是sqlite文件，ENV_USERNAME、ENV_PASSWORD为系统登录用户和密码

