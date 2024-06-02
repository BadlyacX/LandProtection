package com.badlyac;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.util.BoundingBox;

import java.util.HashMap;
import java.util.Map;

public class LandProtectionCommand implements CommandExecutor, Listener {

    private static final Map<String, ProtectionArea> protectedAreas = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (label.equalsIgnoreCase("landprotection")) {
                if (args.length != 5) {
                    player.sendMessage(ChatColor.YELLOW + "Usage: /landprotection <area name> <x> <y> <z> <radius>");
                    return false;
                }

            String areaName = args[0];
            int x, y, z, radius;
            try {
                x = Integer.parseInt(args[1]);
                y = Integer.parseInt(args[2]);
                z = Integer.parseInt(args[3]);
                radius = Integer.parseInt(args[4]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Please enter valid numbers for the coordinates and radius.");
                return false;
            }

            World world = player.getWorld();
            Location center = new Location(world, x, y, z);

            protectedAreas.put(areaName, new ProtectionArea(center, radius));
            saveAreaToConfig(areaName, center, radius);

            player.sendMessage(ChatColor.GREEN + "Area '" + areaName + "' has been protected at ("
                    + x + ", " + y + ", " + z + ") with a radius of " + radius + ".");
        }
            else if (label.equalsIgnoreCase("removeprotection")) {
                if (args.length != 1) {
                    player.sendMessage(ChatColor.YELLOW + "Usage: /removeprotection <area name>");
                    return false;
                }

                return removeProtectionArea(player, args[0]);
            }

            return true;
        } else {
            sender.sendMessage(ChatColor.YELLOW + "This command can only be used by players.");
            return false;
        }
    }


    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.getEntityType() == EntityType.PRIMED_TNT) {
            Location explosionLocation = event.getLocation();
            for (ProtectionArea area : protectedAreas.values()) {
                if (area.isInside(explosionLocation)) {
                    event.blockList().clear();
                    break;
                }
            }
        }
    }
    private void saveAreaToConfig(String areaName, Location center, int radius) {
        FileConfiguration config = LandProtection.getInstance().getConfig();
        String path = "protectedAreas." + areaName;
        config.set(path + ".center.world", center.getWorld().getName());
        config.set(path + ".center.x", center.getX());
        config.set(path + ".center.y", center.getY());
        config.set(path + ".center.z", center.getZ());
        config.set(path + ".radius", radius);
        LandProtection.getInstance().saveConfig();
    }
    public static void loadProtectedAreas() {
        FileConfiguration config = LandProtection.getInstance().getConfig();
        ConfigurationSection section = config.getConfigurationSection("protectedAreas");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                World world = Bukkit.getWorld(section.getString(key + ".center.world"));
                double x = section.getDouble(key + ".center.x");
                double y = section.getDouble(key + ".center.y");
                double z = section.getDouble(key + ".center.z");
                int radius = section.getInt(key + ".radius");
                Location center = new Location(world, x, y, z);
                protectedAreas.put(key, new ProtectionArea(center, radius));
            }
        }
    }

    private void removeAreaFromConfig(String areaName) {
        FileConfiguration config = LandProtection.getInstance().getConfig();
        String path = "protectedAreas." + areaName;
        config.set(path, null);
        LandProtection.getInstance().saveConfig();
    }
    private boolean removeProtectionArea(Player player, String areaName) {
        if (!protectedAreas.containsKey(areaName)) {
            player.sendMessage(ChatColor.RED + "No such protection area: " + areaName);
            return false;
        }

        protectedAreas.remove(areaName);
        removeAreaFromConfig(areaName);
        player.sendMessage(ChatColor.GREEN + "Protection area '" + areaName + "' has been removed.");
        return true;
    }

    private static class ProtectionArea {
        private final BoundingBox boundingBox;

        public ProtectionArea(Location center, int radius) {
            this.boundingBox = new BoundingBox(
                    center.getX() - radius, center.getY() - radius, center.getZ() - radius,
                    center.getX() + radius, center.getY() + radius, center.getZ() + radius
            );
        }


        public boolean isInside(Location location) {
            return boundingBox.contains(location.getX(), location.getY(), location.getZ());
        }
    }
}
