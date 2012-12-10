package net.krinsoft.privileges.groups;

import net.krinsoft.privileges.Privileges;
import net.krinsoft.privileges.event.GroupPermissionAddEvent;
import net.krinsoft.privileges.event.GroupPermissionRemoveEvent;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 *
 * @author krinsdeath
 */
@SuppressWarnings("unused")
public class RankedGroup implements Group {
    // the name of this group
    private String name;
    // this group's rank
    private int rank;
    // this group's inheritance tree, as strings
    private List<String> tree;
    // this group's promotion
    private String promotion;
    // this group's demotion
    private String demotion;

    private Privileges plugin;

    public RankedGroup(Privileges plugin, String name, int rank, List<String> tree) {
        this.plugin = plugin;
        this.name = name;
        this.rank = rank;
        this.tree = tree;
        for (World world : plugin.getServer().getWorlds()) {
            Permission worldPerm = plugin.getServer().getPluginManager().getPermission("master." + name + "." + world.getName());
            if (worldPerm == null) {
                worldPerm = new Permission("master." + name + "." + world.getName());
            }
            worldPerm.setDefault(PermissionDefault.FALSE);
            worldPerm.getChildren().clear();
            LinkedHashMap<String, Boolean> children = new LinkedHashMap<String, Boolean>();
            for (String g : tree) {
                ConfigurationSection group = plugin.getGroupNode(g);
                if (group == null) { continue; }
                for (String node : group.getStringList("permissions")) {
                    if (node.startsWith("-")) {
                        children.remove(node.substring(1));
                        children.put(node.substring(1), false);
                    } else {
                        children.remove(node);
                        children.put(node, true);
                    }
                }
                for (String node : group.getStringList("worlds." + world.getName())) {
                    if (node.startsWith("-")) {
                        children.remove(node.substring(1));
                        children.put(node.substring(1), false);
                    } else {
                        children.remove(node);
                        children.put(node, true);
                    }
                }
            }
            if (!children.containsKey("group." + name)) {
                children.put("group." + name, true);
            }
            worldPerm.getChildren().putAll(children);
            if (plugin.getServer().getPluginManager().getPermission(worldPerm.getName()) == null) {
                plugin.getServer().getPluginManager().addPermission(worldPerm);
            }
            worldPerm.recalculatePermissibles();
        }
        promotion = plugin.getGroupNode(name).getString("data.promotion", null);
        demotion = plugin.getGroupNode(name).getString("data.demotion", null);
    }

    public List<String> getGroupTree() {
        if (this.tree == null) {
            this.tree = new ArrayList<String>(Arrays.asList(this.name));
        }
        return tree;
    }

    public int getRank() {
        return this.rank;
    }

    public String getName() {
        return this.name;
    }

    public boolean isMemberOf(String group) {
        return (this.tree.contains(group));
    }

    public boolean isMemberOf(Group group) {
        return (group != null && this.tree.contains(group.getName()));
    }

    public boolean hasPermission(String permission, String world) {
        Permission perm = plugin.getServer().getPluginManager().getPermission(getMasterPermission(world));
        if (perm != null && perm.getChildren().containsKey(permission)) {
            return perm.getChildren().get(permission);
        }
        return false;
    }

    public String getMasterPermission(String world) {
        return "master." + name + "." + world;
    }

    public boolean addPermission(String world, String node) {
        if (node != null) {
            ConfigurationSection config = plugin.getGroupNode(name);
            List<String> nodes;
            boolean value = !node.startsWith("-");
            boolean success = false;
            if (world != null && !world.equals("null")) {
                nodes = config.getStringList("worlds." + world);
                if (!nodes.contains(node)) {
                    success = nodes.add(node);
                    config.set("worlds." + world, nodes);
                }
            } else {
                nodes = config.getStringList("permissions");
                if (!nodes.contains(node)) {
                    success = nodes.add(node);
                    config.set("permissions", nodes);
                }
            }
            plugin.getServer().getPluginManager().callEvent(new GroupPermissionAddEvent(this.name, value ? node : node.substring(1), world, value));
            return success;
        }
        return false;
    }

    public boolean removePermission(String world, String node) {
        if (node != null) {
            ConfigurationSection config = plugin.getGroupNode(name);
            List<String> nodes;
            boolean value = !node.startsWith("-");
            boolean success;
            if (world != null && !world.equals("null")) {
                nodes = config.getStringList("worlds." + world);
                success = nodes.remove(node);
                config.set("worlds." + world, nodes);
            } else {
                nodes = config.getStringList("permissions");
                success = nodes.remove(node);
                config.set("permissions", nodes);
            }
            plugin.getServer().getPluginManager().callEvent(new GroupPermissionRemoveEvent(this.name, value ? node : node.substring(1), world));
            return success;
        }
        return false;
    }

    public boolean hasPromotion() {
        return promotion != null;
    }
    public String getPromotion() {
        return promotion;
    }

    public boolean hasDemotion() {
        return demotion != null;
    }

    public String getDemotion() {
        return demotion;
    }
    
    @Override
    public String toString() {
        return "RankedGroup{name=" + this.name + "}@" + this.tree.toString().hashCode();
    }
    
    @Override
    public int hashCode() {
        int hash = 7 * 19 + this.toString().hashCode();
        hash = hash * 19 + this.tree.size();
        return hash;
    }
    
    @Override
    public boolean equals(Object that) {
        if (that == null) { return false; }
        if (this == that) { return true; }
        if (this.getClass() != that.getClass()) { return false; }
        RankedGroup group = (RankedGroup) that;
        return this.toString().equals(group.toString());
    }

}
