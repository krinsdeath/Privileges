package net.krinsoft.privileges.commands;

import java.util.List;
import net.krinsoft.privileges.Privileges;
import net.krinsoft.privileges.groups.GroupManager;
import org.bukkit.command.CommandSender;

/**
 *
 * @author krinsdeath
 */
public abstract class GroupCommand extends PrivilegesCommand {

    protected GroupManager groupManager;

    public GroupCommand(Privileges plugin) {
        super(plugin);
        this.groupManager = plugin.getGroupManager();
    }

    @Override
    public abstract void runCommand(CommandSender sender, List<String> args);
}
