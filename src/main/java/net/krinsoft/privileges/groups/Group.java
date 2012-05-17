package net.krinsoft.privileges.groups;

import java.util.List;

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

}
