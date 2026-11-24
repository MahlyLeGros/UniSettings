package io.github.mahlylegros.unisettings;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Choisit, au lancement, quelle variante du mixin appliquer. C'est ce qui permet d'avoir
 * un seul jar pour toutes les versions de Minecraft :
 * <ul>
 *   <li>Minecraft 1.14.4 à 1.21.11 : le jeu est obfusqué et Fabric lui donne des noms internes
 *   ("intermediary"), par exemple {@code net/minecraft/class_315} pour les réglages.
 *   On applique {@code mixin.OptionsMixin}.</li>
 *   <li>Minecraft 26.1 et plus : plus d'obfuscation, les classes gardent leur vrai nom.
 *   On applique {@code mixin.modern.OptionsMixin}.</li>
 * </ul>
 * En développement (./gradlew runClient), il n'y a pas de noms internes : c'est la variante
 * "modern" qui s'applique, compilée avec les mêmes noms que le jeu lancé, donc ça marche aussi.
 */
public class UniSettingsMixinPlugin implements IMixinConfigPlugin {

    /** La classe des réglages de Minecraft (Options) sous son nom interne Fabric. */
    private static final String INTERMEDIARY_OPTIONS = "net/minecraft/class_315.class";

    private boolean intermediary;

    @Override
    public void onLoad(String mixinPackage) {
        ClassLoader loader = UniSettingsMixinPlugin.class.getClassLoader();
        intermediary = loader != null && loader.getResource(INTERMEDIARY_OPTIONS) != null;
        Constants.LOG.info("UniSettings : variante {} du mixin",
                intermediary ? "1.14.4-1.21.11" : "26.x");
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        boolean modern = mixinClassName.contains(".mixin.modern.");
        return modern != intermediary;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
