package za.co.fd.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfigManager {
    private static final String CONFIG_FILE = "/home/andre/Projects/Java/FD_Vat/config.properties";
    private Map<String, String> configs = new HashMap<>();

    public void loadConfig() {
        try (Stream<String> stream = Files.lines(Paths.get(CONFIG_FILE))) {
            configs = stream
                    .map(s -> s.split(":"))
                    .collect(Collectors.groupingBy(a -> a[0],
                            Collectors.mapping(a -> a[1],
                                    Collectors.joining(" "))));

        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }

    public void storeConfig() {
        File file = new File(CONFIG_FILE);
        try (BufferedWriter bf = new BufferedWriter(new FileWriter(file))) {
            configs.forEach((k, v)
                            -> {
                        try {
                            bf.write(k);
                            bf.write(":");
                            bf.write(v);
                            bf.newLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
            );
            bf.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String get(final String key, final String defaultValue) {
        if (!configs.containsKey(key)) {
            configs.put(key, defaultValue);
        }
        return configs.get(key);
    }

    public void set(final String key, final String value) {
        configs.put(key, value);
    }
}
