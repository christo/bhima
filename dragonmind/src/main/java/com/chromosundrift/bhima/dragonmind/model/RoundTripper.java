package com.chromosundrift.bhima.dragonmind.model;

import java.io.IOException;

/**
 * Reads config file and writes it back to the canonical serialisation form. Uses command line arg if present,
 * otherwise default file name {@link Config#DEFAULT_CONFIG_FILE}.
 */
public class RoundTripper {
    public static void main(String[] args) {
        try {
            if (args.length == 1) {
                Config.load(args[0]).save(args[0]);
            } else {
                Config.load().save();
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }
}
