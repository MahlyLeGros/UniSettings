package net.minecraftforge.fml.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Copie minimale de l'annotation {@code @Mod} de Forge, juste pour compiler.
 * Elle n'est PAS mise dans le jar : en jeu, c'est la vraie annotation de Forge qui est utilisée
 * (Forge la reconnaît à son nom, qui est le même de Minecraft 1.15 à 26.x).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Mod {
    String value();
}
