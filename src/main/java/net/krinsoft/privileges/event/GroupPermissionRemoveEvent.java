package net.krinsoft.privileges.event;

import org.bukkit.event.HandlerList;

/**
 * @author krinsdeath
 */
public class GroupPermissionRemoveEvent extends GroupPermissionEvent {

    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private final String world;

    public GroupPermissionRemoveEvent(String group, String node) {
        this(group, node, null);
    }

    public GroupPermissionRemoveEvent(String group, String node, String world) {
        super(group, node);
        this.world = world;
    }

    /**
     * Returns the name of the world on which the new permission is to be applied
     * @return The world name, or null if the node is global
     */
    public String getWorld() {
        return this.world;
    }

}
