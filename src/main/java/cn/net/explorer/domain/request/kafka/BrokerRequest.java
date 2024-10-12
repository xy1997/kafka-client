package cn.net.explorer.domain.request.kafka;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotEmpty;

@Data
public class BrokerRequest {

    private Integer id;

    @NotEmpty(message = "broker名称不能为空")
    private String name;

    @NotEmpty(message = "broker地址不能为空")
    private String broker;

    private String username;

    private String password;

    private String remark;

    private void setName(String name){
        this.name = StringUtils.trim(name);
    }
}
