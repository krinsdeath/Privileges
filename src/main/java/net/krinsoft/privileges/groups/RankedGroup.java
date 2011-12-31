package net.krinsoft.privileges.groups;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author krinsdeath
 */
public class RankedGroup implements Group {

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

    public boolean isMemberOf(String group) {
        return (this.tree.contains(group));
    }

    public boolean isMemberOf(Group group) {
        return (this.tree.contains(group.getName()));
    }

}
