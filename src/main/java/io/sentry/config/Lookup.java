//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.sentry.config;

import io.sentry.dsn.Dsn;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

public final class Lookup {
    private static final Logger logger = LoggerFactory.getLogger(Lookup.class);
    private static final String CONFIG_FILE_NAME = "application.properties";
    private static Properties configProps;
    private static boolean checkJndi = true;

    private Lookup() {
    }

    private static String getConfigFilePath() {
        String filePath = System.getProperty("sentry.properties.file");
        if (filePath == null) {
            filePath = System.getenv("SENTRY_PROPERTIES_FILE");
        }

        if (filePath == null) {
            filePath = "application.properties";
        }

        return filePath;
    }

    private static InputStream getInputStream(String filePath) throws FileNotFoundException {
        File file = new File(filePath);
        if (file.isFile() && file.canRead()) {
            return new FileInputStream(file);
        } else {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            return classLoader.getResourceAsStream(filePath);
        }
    }

    public static String lookup(String key) {
        return lookup(key, (Dsn)null);
    }

    public static String lookup(String key, Dsn dsn) {
        String value = null;
        if (checkJndi) {
            try {
                Class.forName("javax.naming.InitialContext", false, Dsn.class.getClassLoader());
                value = JndiLookup.jndiLookup(key);
                if (value != null) {
                    logger.debug("Found {}={} in JNDI.", key, value);
                }
            } catch (NoClassDefFoundError | ClassNotFoundException var4) {
                logger.trace("JNDI is not available: " + var4.getMessage());
                checkJndi = false;
            }
        }

        if (value == null) {
            value = System.getProperty("sentry." + key.toLowerCase());
            if (value != null) {
                logger.debug("Found {}={} in Java System Properties.", key, value);
            }
        }

        if (value == null) {
            value = System.getenv("SENTRY_" + key.replace(".", "_").toUpperCase());
            if (value != null) {
                logger.debug("Found {}={} in System Environment Variables.", key, value);
            }
        }

        if (value == null && dsn != null) {
            value = (String)dsn.getOptions().get(key);
            if (value != null) {
                logger.debug("Found {}={} in DSN.", key, value);
            }
        }

        if (value == null && configProps != null) {
            value = configProps.getProperty(key);
            if (value != null) {
                logger.debug("Found {}={} in {}.", new Object[]{key, value, "sentry.properties"});
            }
        }

        return value != null ? value.trim() : null;
    }

    static {
        String filePath = getConfigFilePath();

        try {
            InputStream input = getInputStream(filePath);
            if (input != null) {
                configProps = new Properties();
                configProps.load(input);
                configProps.setProperty("dist",System.getProperty("java.runtime.version"));
                if(StringUtils.isEmpty(configProps.getProperty("environment"))){
                    configProps.setProperty("environment",configProps.getProperty("spring.profiles.active"));
                }
                if(StringUtils.isEmpty(configProps.getProperty("app.name"))){
                    configProps.setProperty("app.name",configProps.getProperty("spring.application.name"));
                }
                String tagsValue  = new StringBuffer("os_arch").append(":").append(System.getProperty("os.arch")).append(",").
                        append("os_name").append(":").append(System.getProperty("os.name")).append(",").
                        append("os_version").append(":").append(System.getProperty("os.version")).append(",").
                        append("app_name").append(":").append(configProps.getProperty("app.name")).toString();
                configProps.setProperty("tags",tagsValue);
            } else {
                logger.debug("Sentry configuration file not found in filesystem or classpath: '{}'.", filePath);
            }
        } catch (Exception var2) {
            logger.error("Error loading Sentry configuration file '{}': ", filePath, var2);
        }

    }
}
