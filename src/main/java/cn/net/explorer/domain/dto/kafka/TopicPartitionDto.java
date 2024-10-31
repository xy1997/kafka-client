package cn.net.explorer.domain.dto.kafka;

import lombok.Data;

import java.util.List;

@Data
public class TopicPartitionDto {


    /**
     * 分区号
     */
    private int partition;

    /**
     * leader地址
     */
//    private Node leader;

    /**
     * ISR（In-Sync Replicas） 是一个重要的概念，它指的是一组副本（replicas）,这些副本与领导者副本（leader）保持同步
     * 简单一点: isr里面装的是replicas里面副本 与leader保持同步的副本
     */
   // private List<Node> isr;

    /**
     * 副本地址
     */
    private List<Node> replicas;


    @Data
    public static class Node {
        private int id;
        private String host;
        private int port;
        private String address;
        //如果是副本此字段有用  true代表正在和leader保持信息同步
        private Boolean inSync;
        private Boolean isLeader;

        public String getAddress() {
            return host + ":" + port;
        }

        public Node() {
        }

        public Node(int id, String host, int port,Boolean inSync,Boolean isLeader) {
            this.id = id;
            this.host = host;
            this.port = port;
            this.inSync =  inSync;
            this.isLeader = isLeader;
        }
    }
}
