package net.krinsoft.privileges.event;

import org.bukkit.event.HandlerList;

/**
 * @author krinsdeath
 */
public class GroupPermissionAddEvent extends GroupPermissionEvent {

    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private final String world;
    private final boolean value;

    public GroupPermissionAddEvent(String group, String node, boolean val) {
        this(group, node, null, val);
    }

    public GroupPermissionAddEvent(String group, String node, String world, boolean val) {
        super(group, node);
        this.world = world;
        this.value = val;
    }

    /**
     * Returns the name of the world on which the new permission is to be applied
     * @return The world name, or null if the node is global
     */
    public String getWorld() {
        return this.world;
    }

    /**
      * Fetches the new value associated with the permission
      * @return The permission's new value
     */
    public boolean getValue() {
        return this.value;
    }

}
