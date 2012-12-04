package net.krinsoft.privileges.event;

import org.bukkit.event.Event;

/**
 * @author krinsdeath
 */
public abstract class GroupPermissionEvent extends Event {

    private final String name;
    private final String permission;

    public GroupPermissionEvent(String group, String node) {
        this.name = group;
        this.permission = node;
    }

    /**
     * Gets the name of the group whose permission list just updated
     * @return The group name
     */
    public String getGroup() {
        return this.name;
    }

    /**
     * Gets the name of the permission that was just changed
     * @return The permission name
     */
    public String getPermission() {
        return this.permission;
    }

}
