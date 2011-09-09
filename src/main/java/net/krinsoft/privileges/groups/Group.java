package net.krinsoft.privileges.groups;

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
