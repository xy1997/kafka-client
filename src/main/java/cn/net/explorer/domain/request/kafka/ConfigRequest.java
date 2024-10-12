package cn.net.explorer.domain.request.kafka;

import cn.net.explorer.domain.request.ValidationGroup;
import lombok.Data;
import org.apache.kafka.clients.admin.AlterConfigOp;
import org.apache.kafka.common.config.ConfigResource;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Map;

@Data
public class ConfigRequest {


    @NotNull(message = "brokerId不能为空", groups = {ValidationGroup.select.class, ValidationGroup.save.class, ValidationGroup.update.class, ValidationGroup.delete.class})
    private Integer brokerId;

    /**
     * broker、topic
     */
    @NotEmpty(message = "name不能为空", groups = {ValidationGroup.select.class, ValidationGroup.save.class, ValidationGroup.update.class, ValidationGroup.delete.class})
    private String name;

    /**
     * 区分是broker还是topic
     */
    @NotNull(message = "type不能为空", groups = {ValidationGroup.select.class, ValidationGroup.save.class, ValidationGroup.update.class, ValidationGroup.delete.class})
    private ConfigResource.Type type;

    /**
     * 区分对配置的操作类型
     */
    @NotNull(message = "opType不能为空", groups = {ValidationGroup.save.class, ValidationGroup.update.class, ValidationGroup.delete.class})
    private AlterConfigOp.OpType opType;

    /**
     * 配置信息
     */
    @NotNull(message = "configs不能为空", groups = {ValidationGroup.save.class, ValidationGroup.update.class, ValidationGroup.delete.class})
    @Size(min = 1, message = "configs不能没有内容", groups = {ValidationGroup.save.class, ValidationGroup.update.class, ValidationGroup.delete.class})
    private Map<String, String> configs;


}
