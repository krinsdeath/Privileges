package net.krinsoft.privileges.events;

import net.krinsoft.privileges.groups.Group;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

/**
 *
 * @author krinsdeath
 */
public class GroupChangeEvent extends Event {
    protected Group group;
    protected Player player;

    public GroupChangeEvent(Group group, Player player) {
        super("PrivilegesGroupChangeEvent");
        this.group = group;
        this.player = player;
    }
    
    /**
     * Gets the new group associated with this event.
     * @return
     */
    public Group getGroup() {
        return this.group;
    }

    /**
     * Gets the player associated with this group event.
     * @return
     */
    public Player getPlayer() {
        return this.player;
    }

}
