package io.jmix.migration.core.scan;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Properties;

public class PropertiesParser {

    public Properties parsePropertiesFile(Path filePath) {
        return parsePropertiesFile(filePath.toFile());
    }

    public Properties parsePropertiesFile(File file) {
        Properties properties = new Properties();

        if (!file.exists()) {
            throw new RuntimeException("Properties file '" + file.getAbsolutePath() + "' not found");
        }

        // CUBA tooling writes properties files in UTF-8 (a superset of ASCII), so the read
        // must not depend on the JVM default charset. InputStreamReader replaces malformed
        // sequences instead of failing, which keeps rare legacy-encoded files readable.
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            properties.load(reader);
            return properties;
        } catch (IOException e) {
            throw new RuntimeException("Unable to read properties file '" + file.getAbsolutePath() + "'", e);
        }
    }
}
