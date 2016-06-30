## 框架概况
LTS 有主要有以下四种节点：

* JobClient：主要负责提交任务, 并接收任务执行反馈结果。
* JobTracker：负责接收并分配任务，任务调度。
* TaskTracker：负责执行任务，执行完反馈给JobTracker。
* LTS-Admin：（管理后台）主要负责节点管理，任务队列管理，监控管理等。

其中JobClient，JobTracker，TaskTracker节点都是`无状态`的。
可以部署多个并动态的进行删减，来实现负载均衡，实现更大的负载量, 并且框架采用FailStore策略使LTS具有很好的容错能力。 

LTS注册中心提供多种实现（Zookeeper，redis等），注册中心进行节点信息暴露，master选举。(Mongo or Mysql)存储任务队列和任务执行日志, netty or mina做底层通信, 并提供多种序列化方式fastjson, hessian2, java等。

LTS支持任务类型：

* 实时任务：提交了之后立即就要执行的任务。
* 定时任务：在指定时间点执行的任务，譬如 今天3点执行（单次）。
* Cron任务：CronExpression，和quartz类似（但是不是使用quartz实现的）譬如 0 0/1 * * * ?

支持动态修改任务参数,任务执行时间等设置,支持后台动态添加任务,支持Cron任务暂停,支持手动停止正在执行的任务(有条件),支持任务的监控统计,支持各个节点的任务执行监控,JVM监控等等.

## 架构图

![LTS architecture](http://git.oschina.net/hugui/light-task-scheduler/raw/master/docs/LTS_architecture.png?dir=0&filepath=docs%2FLTS_architecture.png&oid=262a5234534e2d9fa8862f3e632c5551ebd95e21&sha=d01be5d59e8d768f49bbdc66c8334c37af8f7af5)

## 概念说明

###节点组
1. 英文名称 NodeGroup,一个节点组等同于一个小的集群，同一个节点组中的各个节点是对等的，等效的，对外提供相同的服务。
2. 每个节点组中都有一个master节点，这个master节点是由LTS动态选出来的，当一个master节点挂掉之后，LTS会立马选出另外一个master节点，框架提供API监听接口给用户。

###FailStore
1. 顾名思义，这个主要是用于失败了存储的，主要用于节点容错，当远程数据交互失败之后，存储在本地，等待远程通信恢复的时候，再将数据提交。
2. FailStore主要用户JobClient的任务提交，TaskTracker的任务反馈，TaskTracker的业务日志传输的场景下。
3. FailStore目前提供几种实现：leveldb,rocksdb,berkeleydb,mapdb,ltsdb，用于可以自由选择使用哪种,用户也可以采用SPI扩展使用自己的实现。


## 流程图
下图是一个标准的实时任务执行流程。

![LTS progress](http://git.oschina.net/hugui/light-task-scheduler/raw/master/docs/LTS_progress.png?dir=0&filepath=docs%2FLTS_progress.png&oid=22f60a83b51b26bac8dabbb5053ec9913cefc45c&sha=774aa73d186470aedbb8f4da3c04a86a6022be05)

