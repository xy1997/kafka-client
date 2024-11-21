package cn.net.explorer.domain.request.kafka;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class ProducerRequest {

    @NotNull(message = "brokerId不能为空")
    private Integer brokerId;

    @NotNull(message = "topic不能为空")
    private String topic;

    private Integer partition;

    @NotEmpty(message = "message不能为空")
    private String message;
}
