package net.krinsoft.privileges.events;

import net.krinsoft.privileges.groups.Group;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

/**
 *
 * @author krinsdeath
 */
public class GroupEvent extends Event {
    protected Group group;
    protected Player player;

    public GroupEvent(Group group, Player player) {
        super("PrivilegesGroupEvent");
        this.group = group;
        this.player = player;
    }

}
