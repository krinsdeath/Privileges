package net.krinsoft.privileges.groups;

import net.krinsoft.privileges.Privileges;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

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

    private LinkedHashMap<String, LinkedHashMap<String, Boolean>>   permissions       = new LinkedHashMap<String, LinkedHashMap<String, Boolean>>();

    private Privileges plugin;

    public RankedGroup(Privileges plugin, String name, int rank, List<String> tree) {
        long time = System.nanoTime();
        this.plugin = plugin;
        this.name = name;
        this.rank = rank;
        this.tree = tree;
        for (World world : plugin.getServer().getWorlds()) {
            LinkedHashMap<String, Boolean> nodes = new LinkedHashMap<String, Boolean>();
            for (String g : tree) {
                ConfigurationSection group = plugin.getGroupNode(g);
                if (group == null) { continue; }
                for (String node : group.getStringList("permissions")) {
                    if (node.startsWith("-")) {
                        nodes.remove(node.substring(1));
                        nodes.put(node.substring(1), false);
                    } else {
                        nodes.remove(node);
                        nodes.put(node, true);
                    }
                }
                for (String node : group.getStringList("worlds." + world.getName())) {
                    if (node.startsWith("-")) {
                        nodes.remove(node.substring(1));
                        nodes.put(node.substring(1), false);
                    } else {
                        nodes.remove(node);
                        nodes.put(node, true);
                    }
                }
            }
            permissions.put(world.getName(), nodes);
        }
        time = System.nanoTime() - time;
        plugin.profile(name + " constructor took: " + time + "ns (" + (time / 1000000L) + "ms)");
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
        Map<String, Boolean> nodes = permissions.get(world);
        return (nodes.get(permission) != null ? nodes.get(permission) : false);
    }

    public LinkedHashMap<String, Boolean> getEffectivePermissions(String world) {
        LinkedHashMap<String, Boolean> nodes = permissions.get(world);
        return (nodes != null ? nodes : new LinkedHashMap<String, Boolean>());
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
