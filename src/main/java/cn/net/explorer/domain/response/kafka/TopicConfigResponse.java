package cn.net.explorer.domain.response.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.kafka.clients.admin.ConfigEntry;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopicConfigResponse {

    private String name;

    private String value;

    private  ConfigEntry.ConfigSource source;

    private  boolean isSensitive;

    private  boolean isReadOnly;

    private  List<ConfigEntry.ConfigSynonym> synonyms;

    private  ConfigEntry.ConfigType type;

    private  String documentation;
}
