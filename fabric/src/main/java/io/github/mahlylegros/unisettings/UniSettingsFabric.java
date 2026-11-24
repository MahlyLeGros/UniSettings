package io.github.mahlylegros.unisettings;

import net.fabricmc.api.ClientModInitializer;

/** Point d'entrée Fabric : il se contente d'appeler le code commun. */
public class UniSettingsFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        UniSettings.init();
    }
}
