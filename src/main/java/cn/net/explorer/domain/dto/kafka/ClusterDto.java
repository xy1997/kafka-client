package cn.net.explorer.domain.dto.kafka;

import lombok.Data;

import java.util.List;

@Data
public class ClusterDto {

    private String clusterId;

    private String host;

    private Integer port;

    private List<Node> nodes;


    @Data
    public static class Node {

        private String host;

        private Integer port;

        public Node() {
        }

        public Node(String host, Integer port) {
            this.host = host;
            this.port = port;
        }
    }
}
