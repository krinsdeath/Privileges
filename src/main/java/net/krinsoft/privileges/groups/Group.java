package net.krinsoft.privileges.groups;

import java.util.List;
import java.util.Map;

/**
 *
 * @author krinsdeath 
 */
@SuppressWarnings("unused")
public interface Group {

    // Returns a list containing all groups in this group's inheritance tree
    public List<String> getGroupTree();

    // Returns this group's rank
    public int getRank();

    // Gets this group's actual name
    public String getName();

    // Returns whether this group's inheritance tree contains the specified group's name
    public boolean isMemberOf(String group);

    // Returns whether this group's inheritance tree contains the specified group's name
    public boolean isMemberOf(Group group);

    // Checks whether this group has the specified permission on the given world
    public boolean hasPermission(String permission, String world);

    // Gets a map of this group's permissions on the specified world
    public String getMasterPermission(String world);

    public boolean hasPromotion();

    public boolean hasDemotion();

    public String getPromotion();

    public String getDemotion();

}
