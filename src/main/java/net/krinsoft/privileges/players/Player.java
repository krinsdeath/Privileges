package net.krinsoft.privileges.players;

import net.krinsoft.privileges.groups.Group;

/**
 * Represents a player registered with the Privileges plugin
 * @author krinsdeath
 */
public interface Player {

    /**
     * Gets this player's group
     * @return The most powerful group of which this player is a member
     */
    public Group getGroup();

    /**
     * Gets an array of all groups of which this player is a member
     * @return The array of groups
     */
    public Group[] getGroups();

    /**
     * Adds the specified permission to this player's permission list
     * @param world The world to add the permission to, or null for the global list
     * @param node The name of the permission we're adding, prefixed by a - for 'false'
     * @return true if the permission added successfully, otherwise false
     */
    public boolean addPermission(String world, String node);

    /**
     * Removes the specified permission from this player's permission list
     * @param world The world to remove the permission from, or null for the global list
     * @param node The name of the permission being removed
     * @return true if the permission was removed successfully, otherwise false
     */
    public boolean removePermission(String world, String node);

    /**
     * Fetchs a string representing this player's master permission for the given world
     * @param world The world on which the player is currently playing
     * @return The name of the permission
     */
    public String getMasterPermission(String world);

}
