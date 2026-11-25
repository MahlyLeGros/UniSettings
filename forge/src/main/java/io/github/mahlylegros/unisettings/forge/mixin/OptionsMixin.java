package io.github.mahlylegros.unisettings.forge.mixin;

import io.github.mahlylegros.unisettings.UniSettings;
import io.github.mahlylegros.unisettings.platform.ForgePlatformHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Le même branchement que sur Fabric : on applique le profil juste avant que Minecraft lise
 * options.txt, et on met le profil à jour juste après chaque sauvegarde des réglages.
 * <p>
 * Pourquoi tous ces noms ? Sous Forge et NeoForge, le jeu n'utilise pas les mêmes noms selon
 * la version. Au lieu de fabriquer un jar par version, on liste tous les noms possibles et
 * Mixin prend celui qui existe dans le jeu lancé :
 * <ul>
 *   <li>Forge 1.15.2 à 1.16.5 : classe {@code GameSettings}, lecture {@code func_74300_a},
 *   sauvegarde {@code func_74303_b} ;</li>
 *   <li>Forge 1.17 à 1.20.x et NeoForge 1.20.1 à 1.20.4 : classe {@code Options}, lecture
 *   {@code m_92140_}, sauvegarde {@code m_92169_} ;</li>
 *   <li>versions récentes (noms officiels de Mojang) : classe {@code Options}, lecture {@code load},
 *   sauvegarde {@code save}.</li>
 * </ul>
 * Ces noms viennent des tables de correspondance de Forge (MCPConfig) et sont vérifiés en lançant
 * le vrai jeu dans la CI. Rien n'est renommé à la compilation ({@code remap = false}), donc
 * pas besoin de compiler contre Minecraft.
 */
@Pseudo
@Mixin(targets = {"net.minecraft.client.Options", "net.minecraft.client.GameSettings"}, remap = false)
public abstract class OptionsMixin {

    @Inject(method = {"load()V", "m_92140_()V", "func_74300_a()V"}, at = @At("HEAD"), remap = false, require = 0)
    private void unisettings$beforeLoad(CallbackInfo ci) {
        UniSettings.beforeOptionsLoad(ForgePlatformHelper.optionsFile());
    }

    @Inject(method = {"save()V", "m_92169_()V", "func_74303_b()V"}, at = @At("TAIL"), remap = false, require = 0)
    private void unisettings$afterSave(CallbackInfo ci) {
        UniSettings.afterOptionsSave(ForgePlatformHelper.optionsFile());
    }
}
