package cn.net.explorer.domain.response.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopicResponse {

    private String name;

    private Boolean isInternal;
}
