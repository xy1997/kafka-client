package cn.net.explorer.domain.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.kafka.clients.admin.ConfigEntry;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfigDto {

    /**
     * 配置 key
     */
    private String name;

    /**
     * 配置value
     */
    private String value;

    /**
     * 配置类型   BROKER、TOPIC等
     */
    private ConfigEntry.ConfigSource source;

    private boolean isSensitive;

    private boolean isReadOnly;

    private List<ConfigEntry.ConfigSynonym> synonyms;

    private ConfigEntry.ConfigType type;

    private String documentation;
}
