package cn.net.explorer.domain.response.kafka;

import lombok.Data;

import java.util.List;

@Data
public class ConsumerGroupResponse {

    private String groupId;

    private String state;

    private List<Member> members;

    public ConsumerGroupResponse(){}

    public ConsumerGroupResponse(String groupId, String state, List<Member> members) {
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
