package com.chromosundrift.bhima.dragonmind;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import static java.lang.String.format;
import static java.lang.System.getProperties;
import static java.lang.System.getProperty;

public final class SystemUtils {
    private static final Logger logger = LoggerFactory.getLogger(SystemUtils.class);

    private static final String OSX = "mac os x";
    private static final String OS_NAME = "os.name";
    private static final String USER_NAME = "user.name";

    /**
     * Returns a path to the os-specific expected location for mounted external disks.
     * @return the path.
     */
    public static String getMediaBaseDir() {
        return isLinux()
                ? format("/media/%s", getProperty(USER_NAME))
                : "/Volumes";
    }

    /**
     * Lowercase operating system name or "unknown".
     */
    public static String getOs() {
        return getProperties().getProperty(OS_NAME, "unknown").toLowerCase();
    }

    public static boolean isLinux() {
        return getOs().equalsIgnoreCase("linux");
    }

    public static boolean isNix(String osname) {
        return osname.matches(".+n[iu]x\\b]");
    }

    public static boolean isMac() {
        return getOs().equalsIgnoreCase(OSX);
    }

    /**
     * Sets system property key to value only if not already set.
     *
     * @param key   the key.
     * @param value the value.
     */
    static String sysPropDefault(String key, String value) {
        if (!getProperties().containsKey(key)) {
            logger.warn("Fallback sysprop {} = {}", key, value);
            System.setProperty(key, value);
        }
        return System.getProperty(key);
    }

    static boolean checkIsDir(String dir) {
        File f = new File(dir);
        if (!f.exists()) {
            logger.warn("{} does not exist", dir);
            return false;
        } else if (!f.isDirectory()) {
            logger.warn("{} is not a dir", dir);
            return false;
        }
        return true;
    }
}
