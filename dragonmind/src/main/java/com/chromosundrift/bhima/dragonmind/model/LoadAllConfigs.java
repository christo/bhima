package com.chromosundrift.bhima.dragonmind.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class LoadAllConfigs {
    private static final Logger logger = LoggerFactory.getLogger(LoadAllConfigs.class);

    /**
     * Exit code is the number of files with problems.
     *
     * @param args
     */
    public static void main(String[] args) {
        int code = 0;
        List<String> files = Arrays.asList(
                "dragonmind.config.json",
                "dragonmind-mini.config.json",
                "dragonmind-curved.config.json",
                "dragonmind-mini.config.json"
        );
        for (String file : files) {
            // will get sanity checked
            try {
                Config.load(file);
            } catch (RuntimeException | IOException e) {
                code++;
                e.printStackTrace();
            }
        }
        System.exit(code);
    }
}
