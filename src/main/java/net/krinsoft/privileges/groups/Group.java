package net.krinsoft.privileges.groups;

import java.util.List;

/**
 *
 * @author krinsdeath 
 */
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

    // Returns whether this group has the specified permission
    // please don't use this method
    public boolean has(String node);
    
    // returns whether this group has the specified permission that world
    // please don't use this method
    public boolean has(String node, String world);
    
}
