package net.krinsoft.privileges.groups;

import java.util.List;
import net.krinsoft.privileges.Privileges;

/**
 *
 * @author krinsdeath
 */
public class Group {

    private String name;
    private int rank;

    public Group(String name, int rank) {
        this.name = name;
        this.rank = rank;
    }

    public int getRank() {
        return this.rank;
    }

    public String getName() {
        return this.name;
    }

    /**
     * Checks a group for a specific permission node on a specific world
     * @param node The node to check.
     * @param world The world's name to check, or null
     * @return true if the group has that node, or false if not
     */
    public boolean has(String node, String world) {
        List<String> nodes = Privileges.instance.getGroupNode(this.name).getStringList("permissions", null);
        if (world != null) {
            for (String wNode : Privileges.instance.getGroupNode(this.name).getStringList("worlds." + world, null)) {
                nodes.remove(wNode);
                nodes.remove("-" + wNode);
                nodes.add(wNode);
            }
        }
        if (nodes.contains(node)) {
            return true;
        } else {
            return false;
        }
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
