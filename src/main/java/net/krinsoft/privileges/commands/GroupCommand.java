package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.Privileges;
import net.krinsoft.privileges.groups.GroupManager;

/**
 *
 * @author krinsdeath
 */
public abstract class GroupCommand extends PrivilegesCommand {

    protected GroupManager groupManager;

    public GroupCommand(Privileges plugin) {
        super(plugin);
        groupManager = plugin.getGroupManager();
    }
}
