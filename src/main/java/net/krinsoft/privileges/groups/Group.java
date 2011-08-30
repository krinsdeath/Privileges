package net.krinsoft.privileges.groups;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author krinsdeath
 */
public class Group {

    private String name;
    private int rank;
    private List<String> permissions = new ArrayList<String>();

    public Group(String name, int rank, List<String> nodes) {
        this.name = name;
        this.rank = rank;
        for (String node : nodes) {
            if (!node.startsWith("-")) {
                this.permissions.add(node);
            }
        }
    }

    public int getRank() {
        return this.rank;
    }

    public String getName() {
        return this.name;
    }

    public List<String> getNodes() {
        return this.permissions;
    }

    @Override
    public String toString() {
        return "Group{name=" + this.name + ",rank=" + this.rank + "}";
    }

    @Override
    public int hashCode() {
        int hash = 19;
        hash = hash * 31 + (this.permissions.size() / 19);
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
