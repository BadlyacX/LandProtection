package com.badlyac;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class LandProtection extends JavaPlugin {
    private static LandProtection instance;
    @Override
    public void onEnable() {
        instance = this;
        LandProtectionCommand.loadProtectedAreas();
        LandProtectionCommand commandExecutor = new LandProtectionCommand();
        this.getCommand("LandProtection").setExecutor(commandExecutor);
        this.getCommand("removeprotection").setExecutor(commandExecutor);
        Bukkit.getPluginManager().registerEvents(commandExecutor, this);
    }
    public static LandProtection getInstance() {
        return instance;
    }
}
