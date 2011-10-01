package net.krinsoft.privileges.groups;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.krinsoft.privileges.Privileges;
import org.bukkit.World;

/**
 *
 * @author krinsdeath
 */
public class Group {

    private String name;
    private int rank;
    private List<String> globals = new ArrayList<String>();
    private HashMap<String, List<String>> worlds = new HashMap<String, List<String>>();

    public Group(String name, int rank) {
        this.name = name;
        this.rank = rank;
        List<String> tree = Privileges.instance.getPermissionManager().calculateGroupTree(this.name);
        for (String branch : tree) {
            for (String node : Privileges.instance.getGroupManager().getGroup(branch).getGlobals()) {
                if (node.startsWith("-")) {
                    globals.remove(node.substring(1));
                }
                globals.remove(node);
                globals.add(node);
            }
        }
        globals.addAll(calculateGlobals(tree));
        for (World world : Privileges.instance.getServer().getWorlds()) {
            List<String> worldNodes = new ArrayList<String>(globals);
            for (String node : calculateWorlds(tree, world.getName())) {
                worldNodes.remove(node);
                worldNodes.remove("-" + node);
                worldNodes.add(node);
            }
            worlds.put(world.getName(), worldNodes);
        }
    }

    private List<String> calculateGlobals(List<String> tree) {
        List<String> globalNodes = new ArrayList<String>();
        for (String group : tree) {
            for (String node : Privileges.instance.getGroupNode(group).getStringList("permissions", null)) {
                globalNodes.remove(node);
                globalNodes.remove("-" + node);
                globalNodes.add(node);
            }
        }
        return globalNodes;
    }

    private List<String> calculateWorlds(List<String> tree, String world) {
        List<String> worldNodes = new ArrayList<String>();
        for (String group : tree) {
            for (String node : Privileges.instance.getGroupNode(group).getStringList("worlds." + world, null)) {
                worldNodes.remove(node);
                worldNodes.remove("-" + node);
                worldNodes.add(node);
            }
        }
        return worldNodes;
    }

    protected List<String> getGlobals() {
        return this.globals;
    }

    protected List<String> getWorlds(String world) {
        return worlds.get(world);
    }

    /**
     * Gets the rank of this group.
     * @return the group's rank
     */
    public int getRank() {
        return this.rank;
    }

    /**
     * Gets the actual given name of this group
     * @return the group's name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Checks a group for a specific permission node on a specific world
     * @param node The node to check.
     * @param world The world's name to check, or null for global nodes
     * @return true if the group has that node, or false if not
     */
    public boolean hasPermission(String node, String world) {
        List<String> nodes = new ArrayList<String>(globals);
        if (world != null && worlds.get(world) != null) {
            nodes = worlds.get(world);
        }
        return (nodes.contains(node) && !node.startsWith("-"));
    }

    /**
     * Checks whether this group has the specified node set
     * @param node The permissions node to check
     * @param world The world name, or null for global nodes
     * @return true if the group has that node set, or false if not
     */
    public boolean isPermissionSet(String node, String world) {
        List<String> nodes = new ArrayList<String>(globals);
        if (world != null && worlds.get(world) != null) {
            nodes = worlds.get(world);
        }
        return (nodes.contains(node) || nodes.contains("-" + node));
    }

    @Override
    public String toString() {
        return "Group{name=" + this.name + ",rank=" + this.rank + "}";
    }

    @Override
    public int hashCode() {
        int hash = 19;
        hash = hash * 31 + (this.name.length() / 19);
        hash = hash * 31 + (this.rank * 19);
        return hash + this.toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!this.getClass().equals(obj.getClass())) {
            return false;
        }
        Group group = (Group) obj;
        if (this.hashCode() == group.hashCode()) {
            return true;
        } else {
            return false;
        }
    }

}
