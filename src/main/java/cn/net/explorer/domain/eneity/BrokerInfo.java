package cn.net.explorer.domain.eneity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@TableName(value = "broker_info")
@Data
public class BrokerInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Integer id;

    private String name;

    private String broker;

    private String username;

    private String password;

    private String remark;

    private Date createdTime;
}
