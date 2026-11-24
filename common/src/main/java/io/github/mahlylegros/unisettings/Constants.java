package io.github.mahlylegros.unisettings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * On utilise Log4j plutôt que SLF4J : Log4j est fourni par toutes les versions de Minecraft
 * depuis la 1.14, alors que SLF4J n'arrive qu'avec la 1.18.
 */
public final class Constants {

    public static final String MOD_ID = "unisettings";
    public static final String MOD_NAME = "UniSettings";
    public static final Logger LOG = LogManager.getLogger(MOD_NAME);

    private Constants() {}
}
