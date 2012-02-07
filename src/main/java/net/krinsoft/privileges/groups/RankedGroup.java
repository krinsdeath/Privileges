package net.krinsoft.privileges.groups;

import net.krinsoft.privileges.Privileges;

import java.util.ArrayList;
import java.util.Arrays;
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
    
    private Privileges plugin;

    public RankedGroup(Privileges plugin, String name, int rank, List<String> tree) {
        this.plugin = plugin;
        this.name = name;
        this.rank = rank;
        this.tree = tree;
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
        return (this.tree.contains(group.getName()));
    }
    
    public boolean has(String node) {
        return has(node, "permissions");
    }
    
    public boolean has(String node, String world) {
        List<String> nodes;
        if (world.equals("permissions")) {
            nodes = plugin.calculateNodeList(name, null);
        } else {
            nodes = plugin.calculateNodeList(name, world);
        }
        return nodes.contains(node);
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
        if (this == that) { return true; }
        if (this.getClass() != that.getClass()) { return false; }
        RankedGroup group = (RankedGroup) that;
        return this.toString().equals(group.toString());
    }

}
