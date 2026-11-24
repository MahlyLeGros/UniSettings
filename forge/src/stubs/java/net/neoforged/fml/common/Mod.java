package net.neoforged.fml.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Copie minimale de l'annotation {@code @Mod} de NeoForge, juste pour compiler.
 * Elle n'est PAS mise dans le jar : en jeu, c'est la vraie annotation de NeoForge qui est utilisée.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Mod {
    String value();
}
