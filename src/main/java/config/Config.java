package config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Setter
@Getter
@ToString
public class Config {

    @Data
    public static class DispatcherConfig {
        private int threads;
    }

    @Data
    public static class VwapConfig {
        private int vwapIntervalInMs;
    }

    private DispatcherConfig dispatcherConfig;
    private VwapConfig vwapConfig;

    public static Config loadConfig(String filePath) throws IOException {
        Yaml yaml = new Yaml();
        Config config;
        try (InputStream inputStream = Config.class.getClassLoader().getResourceAsStream(filePath)) {
            if (inputStream == null) {
                config = loadFromFilePath(filePath, yaml);
            } else {
                config = load(yaml, inputStream);
            }
        }
        return config;
    }

    private static Config loadFromFilePath(String filePath, Yaml yaml) throws IOException {
        Config config;
        try (FileInputStream inputStream = new FileInputStream(filePath)) {
            config = load(yaml, inputStream);
        }
        return config;
    }

    private static Config load(Yaml yaml, InputStream inputStream) {
        Config config;
        Map<String, Object> obj = yaml.load(inputStream);
        config = new Config();

        Map<String, Object> map = (Map<String, Object>) obj.get("dispatcher");
        DispatcherConfig dispatcherConfig = new DispatcherConfig();
        dispatcherConfig.setThreads((Integer) map.get("threads"));
        config.setDispatcherConfig(dispatcherConfig);

        map = (Map<String, Object>) obj.get("vwap");
        VwapConfig vwapConfig = new VwapConfig();
        vwapConfig.setVwapIntervalInMs((Integer) map.get("vwapIntervalInMs"));
        config.setVwapConfig(vwapConfig);
        return config;
    }
}