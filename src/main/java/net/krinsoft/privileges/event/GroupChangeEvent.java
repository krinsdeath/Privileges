package net.krinsoft.privileges.event;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This event notifies other plugins that a player's group has just changed.
 * @author krinsdeath
 */
public class GroupChangeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private final OfflinePlayer player;

    private final String original;
    private final String group;

    public GroupChangeEvent(OfflinePlayer player, String original, String group) {
        this.player = player;
        this.original = original;
        this.group = group;
    }

    /**
     * Gets the player whose group has changed
     * @return The player
     */
    public OfflinePlayer getPlayer() {
        return this.player;
    }

    /**
     * Gets the player's original group
     * @return The player's group before it changed
     */
    public String getOriginalGroup() {
        return this.original;
    }

    /**
     * Gets the player's new group name
     * @return The name of the player's new group
     */
    public String getGroup() {
        return this.group;
    }

}
