package net.krinsoft.privileges.events;

import net.krinsoft.privileges.groups.Group;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author krinsdeath
 */
@SuppressWarnings("unused")
public class GroupChangeEvent extends Event {
    protected Group group;
    protected Player player;
    
    private final static HandlerList HANDLERS = new HandlerList();

    public GroupChangeEvent(Group group, Player player) {
        super("PrivilegesGroupChangeEvent");
        this.group = group;
        this.player = player;
    }
    
    /**
     * Gets the new group associated with this event.
     * @return the group
     */
    public Group getGroup() {
        return this.group;
    }

    /**
     * Gets the player associated with this group event.
     * @return the player
     */
    public Player getPlayer() {
        return this.player;
    }
    
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
    
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
