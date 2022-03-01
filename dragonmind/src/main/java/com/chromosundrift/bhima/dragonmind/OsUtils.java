package com.chromosundrift.bhima.dragonmind;

import static java.lang.String.format;

import static com.chromosundrift.bhima.dragonmind.ProcessingBase.isLinux;

public class OsUtils {
    /**
     * Returns a path to the os-specific expected location for mounted external disks.
     * @return the path.
     */
    public static String getMediaBaseDir() {
        return isLinux()
                ? format("/media/%s", System.getProperty("user.name"))
                : "/Volumes";
    }
}
