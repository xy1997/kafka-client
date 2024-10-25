package cn.net.explorer.domain.request.kafka;

import cn.net.explorer.domain.request.PageRequest;
import lombok.Data;

@Data
public class ClusterRequest extends PageRequest {

    private String name;

    private String broker;

    private String username;

    private String password;

    private String id;

    private String remark;
}
