# vline

多数据源 数据采集、传输中间件, 轻量级 边缘设备场景 ⚓

Not ETL

![alt text](/images/vline.svg)

## dependency

> JDK21 springboot3.4.1 其它版本未测试 repository尚未推送

```gradle
    implementation project("com.codestepfish.vline:vline-spring-boot-starter:${version}")
    
    // 对应数据源依赖
    implementation project("com.codestepfish.vline:vline-tcp:${version}")
    implementation project("com.codestepfish.vline:vline-http:${version}")
    .....
```

## ToDo

| module             | progress |
|--------------------|----------|
| tcp                | ✅        |
| http               | ➖        |
| redis              | ➖        |
| mysql              | ➖        |
| postgresql         | ➖        |
| sql-server-2008-r2 | ➖        |
| sql-server-2000    | ➖        |
| serial-port        | ➖        |

## desc

1. ~~msg data持久化 保证数据传递不丢失~~  由上层应用层实现

## module

| module                    | desc                                           |
|---------------------------|------------------------------------------------|
| vline-core                | 定义Node基本信息、属性, Node可以理解为入口/出口对应的数据源            |
| vline-tcp                 | netty tcp                                      |
| vline-http                | forest 实现                                      |
| vline-spring-boot-starter | spring boot starter : yml解析 初始化  event bus事件监听 |
| examples                  | 示例                                             |

## yaml config

> com.codestepfish.vline.spring.boot.starter.VLineProperties

### basic config

```yaml
vline:
  nodes:
    - name: t1
      type: tcp
      tags:
        - xx
        - oo
      # protocol:
      #  - property:
      #  - property:
  struct:
    t1:
      - t2
```

| key         | 必填 | desc                                                                         |
|-------------|----|------------------------------------------------------------------------------|
| nodes       | Y  | 节点定义 com.codestepfish.vline.core.Node                                        |
| -name       | Y  | 节点名称 全局唯一                                                                    |
| -type       | Y  | 节点类型 com.codestepfish.vline.core.NodeType                                    |
| -tags       | N  | 节点标签                                                                         |
| -Properties | Y  | com.codestepfish.vline.core.Node 每种类型属性                                      |
| struct      | Y  | 数据传输拓扑结构 key: 入口节点 value: 出口节点集合    Map<String, List<String>> k/v都是Node name |

### tcp node

> com.codestepfish.vline.core.tcp.TcpProperties

| key             | 必填 | desc                                                                      |
|-----------------|----|---------------------------------------------------------------------------|
| mode            | Y  | tcp节点类型: SERVER/CLIENT com.codestepfish.vline.core.tcp.TcpProperties.Mode |
| host            | Y  | client/server 客户端IP                                                       |
| port            | Y  | client/server 端口                                                          |
| reconnect-delay | N  | client节点有效 重连间隔时间                                                         |
| child-handler   | Y  | childHandler ChannelInitializer<SocketChannel>                            |                                                             |