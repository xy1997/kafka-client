package cn.net.explorer.domain.response.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopicResponse {

    /**
     * 主题名称
     */
    private String name;

    /**
     * 是否是内部主题
     */
    private Boolean isInternal;

    /**
     * 主题所在分区信息
     */
    List<TopicPartitionResponse> partitions;


}
