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

    public Group(String name, int rank) {
        this.name = name;
        this.rank = rank;
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

}
