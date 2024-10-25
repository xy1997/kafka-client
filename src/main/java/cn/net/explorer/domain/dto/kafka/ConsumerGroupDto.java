package cn.net.explorer.domain.dto.kafka;

import lombok.Data;

import java.util.List;

@Data
public class ConsumerGroupDto {

    /**
     * 消费者组ID
     */
    private String groupId;

    /**
     * 状态
     */
    private String state;

    /**
     * 消费者组的节点信息
     */
    private List<Member> members;

    public ConsumerGroupDto(){}

    public ConsumerGroupDto(String groupId, String state, List<Member> members) {
        this.groupId = groupId;
        this.state = state;
        this.members = members;
    }

    @Data
    public static class Member {
        private String consumerId;
        private String clientId;
        private String host;

        public Member() {
        }

        public Member(String consumerId, String clientId, String host) {
            this.consumerId = consumerId;
            this.clientId = clientId;
            this.host = host;
        }
    }
}
