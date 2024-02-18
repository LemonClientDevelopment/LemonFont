package dev.lemonclient.addon;

import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

public class LemonFont extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category MAIN = new Category("LemonFont");
    public static final HudGroup HUD_GROUP = new HudGroup("LemonFont");

    @Override
    public void onInitialize() {
        LOG.info("Initializing LemonFont");


    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(MAIN);
    }

    @Override
    public String getPackage() {
        return "dev.lemonclient.addon";
    }
}
