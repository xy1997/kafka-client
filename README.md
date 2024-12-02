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
│   │   └── resources/
│   │       └── db
│   │       └── mapper
│   │       └── application.yml
│   │       └── logback-spring.xml
│
├── .gitignore
├── pom.xml
├── README.md