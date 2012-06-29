package net.krinsoft.privileges;

import net.krinsoft.privileges.groups.Group;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.PermissionAttachment;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author krinsdeath
 */
public class PermissionManager {

    private Privileges plugin;
    private HashMap<String, String> players = new HashMap<String, String>();
    private HashMap<String, PermissionAttachment> perms = new HashMap<String, PermissionAttachment>();

    public PermissionManager(Privileges plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        long time = System.nanoTime();
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            registerPlayer(p.getName());
        }
        time = System.nanoTime() - time;
        plugin.profile("Registration of ALL players took: " + time + "ns (" + (time / 1000000L) + "ms)");
    }


    final public boolean registerPlayer(String player) {
        long time = System.nanoTime();
        // validate the player
        Player ply = plugin.getServer().getPlayer(player);
        if (ply == null) {
            plugin.debug("Player '" + player + "' couldn't be found.");
            return false;
        }
        player = ply.getName();
        // build attachment
        PermissionAttachment attachment = ply.addAttachment(plugin);
        if (perms.containsKey(player)) {
            attachment = perms.get(player);
        }
        if (!players.containsKey(player)) {
            players.put(player, ply.getWorld().getName());
        }
        if (attachment == null) { // make sure nothing has gone awry
            plugin.debug("Attachment cannot be null.");
            return false;
        }
        try {
            Field f = org.bukkit.craftbukkit.entity.CraftHumanEntity.class.getDeclaredField("perm");
            f.setAccessible(true);
            PermissibleBase permissible = (PermissibleBase) f.get(ply);
            permissible.clearPermissions();
        } catch (NoSuchFieldException e) {
            plugin.warn("Unknown field: " + e.getMessage());
        } catch (IllegalAccessException e) {
            plugin.warn("Illegal access: " + e.getMessage());
        }
        // iterate through the player's groups, and add them to a list
        String g = plugin.getUserNode(player).getString("group");
        Group group = plugin.getGroupManager().addPlayer(player, g);
        plugin.debug("Group tree: " + group.getGroupTree().toString());
        // calculate group's permissions
        for (Map.Entry<String, Boolean> node : group.getEffectivePermissions(ply.getWorld().getName()).entrySet()) {
            attachNode(attachment, node.getKey(), node.getValue());
        }
        // calculate player's permissions
        // overrides group and world permissions
        calculatePlayerPermissions(attachment, player);
        ply.recalculatePermissions();
        perms.put(player, attachment);
        time = System.nanoTime() - time;
        plugin.profile("Player registration (" + ply.getEffectivePermissions().size() + " nodes) took: " + time + "ns (" + (time / 1000000L) + "ms)");
        return true;
    }

    public List<String> calculateGroupTree(String group, String next) {
        plugin.debug(next + "> " + group);
        List<String> tree = new ArrayList<String>();
        tree.add(0, group);
        List<String> inheritance;
        try {
            inheritance = plugin.getGroupNode(group).getStringList("inheritance");
        } catch (NullPointerException e) {
            return tree;
        }
        for (String top : inheritance) {
            if (top.equalsIgnoreCase(group)) { continue; }
            for (String trunk : calculateBackwardTree(top, next + "-")) {
                tree.add(0, trunk);
            }
        }
        return tree;
    }
    
    private List<String> calculateBackwardTree(String group, String next) {
        plugin.debug(next + "> " + group);
        List<String> tree = new ArrayList<String>();
        tree.add(group);
        List<String> inheritance;
        try {
            inheritance = plugin.getGroupNode(group).getStringList("inheritance");
        } catch (NullPointerException e) {
            return tree;
        }
        for (String top : inheritance) {
            if (top.equalsIgnoreCase(group)) { continue; }
            for (String trunk : calculateBackwardTree(top, next + "-")) {
                tree.add(trunk);
            }
        }
        return tree;
    }

    public void unregisterPlayer(String player) {
        if (players.containsKey(player)) {
            PermissionAttachment att = perms.get(player); // stupid, this reference has to exist to unset permissions on reload :|
            if (att == null) {
                plugin.debug("'" + player + "' unregistering: Attachment is null");
            } else {
                for (String key : att.getPermissions().keySet()) {
                    att.unsetPermission(key);
                }
            }
            players.remove(player);
            perms.remove(player);
        } else {
            plugin.debug("Unregistering '" + player + "': player isn't registered.");
        }
    }

    public void disable() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            unregisterPlayer(player.getName());
        }
    }

    public void updatePlayerWorld(String player, String world) {
        if (players.containsKey(player)) {
            String w = players.get(player);
            if (!world.equals(w)) {
                plugin.debug(player + " moved to world '" + world + "'... recalculating");
                unregisterPlayer(player);
                players.put(player, world);
                registerPlayer(player);
            }
        }
    }

    private void calculatePlayerPermissions(PermissionAttachment attachment, String player) {
        plugin.debug("Calculating player-specific permissions...");
        try {
            for (String node : plugin.getUserNode(player).getStringList("permissions")) {
                attachNode(attachment, node);
            }
        } catch (NullPointerException e) {
            plugin.debug("Encountered null path at '" + player + ".permissions' in users.yml");
        }
        plugin.debug("Calculating player-specific world permissions...");
        try {
            if (plugin.getUserNode(player).getConfigurationSection("worlds").getKeys(false) == null) {
                for (World w : plugin.getServer().getWorlds()) {
                    plugin.getUsers().set("users." + player + ".worlds." + w.getName(), new ArrayList<String>(0));
                }
                plugin.saveUsers();
            }
        } catch (NullPointerException e) {
            plugin.debug("Encountered null path at '" + player + ".worlds' in users.yml");
        }
        try {
            for (String world : plugin.getUserNode(player).getConfigurationSection("worlds").getKeys(false)) {
                try {
                    for (String node : plugin.getUserNode(player).getStringList("worlds." + world)) {
                        attachNode(attachment, node);
                    }
                } catch (NullPointerException e) {
                    plugin.debug("Encountered null path at '" + player + ".worlds." + world + "' in users.yml");
                }
            }
        } catch (NullPointerException e) {
            plugin.debug("Encounter null path at '" + player + ".worlds' in users.yml");
        }
    }

    private void attachNode(PermissionAttachment attachment, String node) {
        boolean val;
        String debug;
        if (node.startsWith("-")) {
            val = false;
            debug = node.substring(1);
        } else {
            val = true;
            debug = node;
        }
        String mod = (attachment.getPermissions().containsKey(debug) ? "overriding" : "setting");
        attachment.unsetPermission(debug);
        attachment.setPermission(debug, val);
        debug = mod + " " + debug + " to " + val;
        plugin.debug(debug);
    }

    private void attachNode(PermissionAttachment attachment, String node, Boolean value) {
        String mod = (attachment.getPermissions().containsKey(node) ? "overriding" : "setting");
        String msg = mod + " " + node + " to " + value;
        attachment.unsetPermission(node);
        attachment.setPermission(node, value);
        plugin.debug(msg);
    }

}
