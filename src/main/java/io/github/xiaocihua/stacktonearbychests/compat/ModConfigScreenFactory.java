package io.github.xiaocihua.stacktonearbychests.compat;

import io.github.xiaocihua.stacktonearbychests.gui.ModOptionsGui;
import io.github.xiaocihua.stacktonearbychests.gui.ModOptionsScreen;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Remplace ModMenuApiImpl (Fabric/ModMenu).
 * Enregistre l'écran de configuration du mod dans le menu Mods natif de NeoForge.
 *
 * À appeler depuis le constructeur de StackToNearbyChests :
 *   ModConfigScreenFactory.register();
 */
public class ModConfigScreenFactory {

    public static void register() {
        ModLoadingContext.get().registerExtensionPoint(
                IConfigScreenFactory.class,
                () -> (minecraft, parent) -> new ModOptionsScreen(new ModOptionsGui()) {
                    @Override
                    public void onClose() {
                        minecraft.setScreen(parent);
                    }
                }
        );
    }
}