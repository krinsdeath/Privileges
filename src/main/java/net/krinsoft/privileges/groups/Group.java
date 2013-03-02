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

    /**
     * Checks whether this group has the specified permission node on the given world
     * @param node The permission node
     * @param world The name of the world on which we're checking the permission
     * @return true if the group has the permission, otherwise false
     */
    public boolean hasPermission(String node, String world);

    /**
     * Returns the master permission string for this group on the specified world
     * @param world The name of the world we're fetching the permission master for
     * @return A string consisting of this group's name and the specified world, prefixed by master -> "master.[group name].[world]"
     */
    public String getMasterPermission(String world);

    /**
     * Adds the specified permission to the list for the specified world. If world is null, adds to the group's global permission list
     * @param world The name of the world we're attaching the node to
     * @param node The name of the permission node
     * @return true if the permission was attached successfully, otherwise false
     */
    public boolean addPermission(String world, String node);

    /**
     * Removes the specified permission node from the list for the specified world. If world is null, removes from the group's global list.
     * @param world The name of the world we're removing the node from
     * @param node The name of the permission node
     * @return true if the node is removed successfully, otherwise false
     */
    public boolean removePermission(String world, String node);

    /**
     * Checks whether this group has an explicit promotion set in groups.yml
     * @return true if there is a promotion, otherwise false
     */
    public boolean hasPromotion();

    /**
     * Checks whether this group has an explicit demotion set in groups.yml
     * @return true if there is a demotion, otherwise false
     */
    public boolean hasDemotion();

    /**
     * Gets this group's promoted group name, if applicable
     * @return The promoted group's name
     */
    public String getPromotion();

    /**
     * Gets this group's demoted group name, if applicable
     * @return The demoted group's name
     */
    public String getDemotion();

}
