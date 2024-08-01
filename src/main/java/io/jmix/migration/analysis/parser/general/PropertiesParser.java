package io.jmix.migration.analysis.parser.general;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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

        try (FileReader fileReader = new FileReader(file)) {
            properties.load(fileReader);
            return properties;
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File '" + file.getAbsolutePath() + "' not found", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
