package net.krinsoft.privileges.players;

import net.krinsoft.privileges.Privileges;
import net.krinsoft.privileges.groups.Group;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author krinsdeath
 */
public class PrivilegesPlayer implements Player {
    private final Privileges plugin;
    private final String name;
    private Group group;

    public PrivilegesPlayer(Privileges plugin, OfflinePlayer player) {
        if (player == null) {
            throw new NullPointerException("The specified player doesn't exist.");
        }
        this.plugin = plugin;
        this.name = player.getName().toLowerCase();
        this.group = plugin.getGroupManager().getGroup(player);
        Map<String, Boolean> globals = new HashMap<String, Boolean>();
        ConfigurationSection user = plugin.getUserNode(player.getName());
        for (String global : user.getStringList("permissions")) {
            boolean val = true;
            if (global.startsWith("-")) {
                global = global.substring(1);
                val = false;
            }
            globals.put(global, val);
        }
        for (World world : plugin.getServer().getWorlds()) {
            Map<String, Boolean> worlds = new HashMap<String, Boolean>();
            worlds.putAll(globals);
            for (String node : user.getStringList("worlds." + world.getName())) {
                boolean val = true;
                if (node.startsWith("-")) {
                    node = node.substring(1);
                    val = false;
                }
                worlds.put(node, val);
            }
            Permission perm = new Permission("player." + this.name + "." + world.getName(), PermissionDefault.FALSE, worlds);
            plugin.getServer().getPluginManager().removePermission(perm);
            perm.getChildren().clear();
            perm.getChildren().putAll(worlds);
            plugin.getServer().getPluginManager().addPermission(perm);
            perm.recalculatePermissibles();
        }
    }

    public Group getGroup() {
        return this.group;
    }

    public Group[] getGroups() {
        Group[] groups = new Group[getGroup().getGroupTree().size()];
        int i = 0;
        for (String group : getGroup().getGroupTree()) {
            groups[i] = plugin.getGroupManager().getGroup(group);
            i++;
        }
        return groups;
    }

    public boolean addPermission(String world, String node) {
        ConfigurationSection user = plugin.getUserNode(this.name);
        if (user != null) {
            List<String> nodes = new ArrayList<String>();
            boolean success = false;
            if (world != null) {
                nodes.addAll(user.getStringList("worlds." + world));
                if (!nodes.contains(node)) {
                    success = nodes.add(node);
                    user.set("worlds." + world, nodes);
                }
            } else {
                nodes.addAll(user.getStringList("permissions"));
                if (!nodes.contains(node)) {
                    success = nodes.add(node);
                    user.set("permissions", nodes);
                }
            }
            // TODO: PlayerPermissionAddEvent
            return success;
        }
        return false;
    }

    public boolean removePermission(String world, String node) {
        ConfigurationSection user = plugin.getUserNode(this.name);
        if (user != null) {
            List<String> nodes = new ArrayList<String>();
            boolean success;
            if (world != null) {
                nodes.addAll(user.getStringList("worlds." + world));
                success = nodes.remove(node);
                user.set("worlds." + world, nodes);
            } else {
                nodes.addAll(user.getStringList("permissions"));
                success = nodes.remove(node);
                user.set("permissions", nodes);
            }
            // TODO: PlayerPermissionRemoveEvent
            return success;
        }
        return false;
    }

    public String getMasterPermission(String world) {
        return "player." + this.name + "." + world;
    }

}
