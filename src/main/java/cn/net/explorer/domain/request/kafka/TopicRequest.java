package cn.net.explorer.domain.request.kafka;

import cn.net.explorer.domain.request.ValidationGroup;
import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Map;

@Data
public class TopicRequest {

    @NotNull(message = "brokerId 不能为空", groups = {ValidationGroup.save.class, ValidationGroup.delete.class})
    private Integer brokerId;

    @NotEmpty(message = "topicName 不能为空", groups = {ValidationGroup.save.class})
    private String topicName;

    @NotEmpty(message = "topicNames 不能为空", groups = {ValidationGroup.delete.class})
    @Size(min = 1,groups = {ValidationGroup.delete.class})
    private List<String> topicNames;

    /**
     * TOPIC所在的分区
     */
    @NotNull(message = "partition 不能为空", groups = ValidationGroup.save.class)
    private Integer partition;

    /**
     * Topic副本数量
     */
    @NotNull(message = "replication 不能为空", groups = ValidationGroup.save.class)
    private Short replication;

    /**
     * Topic的配置项
     */
    private Map<String, String> configs;

}
