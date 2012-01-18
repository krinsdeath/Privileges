package net.krinsoft.privileges.groups;

import net.krinsoft.privileges.Privileges;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author krinsdeath
 */
public class RankedGroup implements Group {
    protected static Privileges plugin;

    // the name of this group
    private String name;
    // this group's rank
    private int rank;
    // this group's inheritance tree, as strings
    private List<String> tree;

    public RankedGroup(String name, int rank, List<String> tree) {
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

}
