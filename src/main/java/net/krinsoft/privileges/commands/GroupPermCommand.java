package net.krinsoft.privileges.commands;

import java.util.List;
import net.krinsoft.privileges.PermissionManager;
import net.krinsoft.privileges.Privileges;
import org.bukkit.command.CommandSender;

/**
 *
 * @author krinsdeath
 */
public abstract class GroupPermCommand extends GroupCommand {

    public GroupPermCommand(Privileges plugin) {
        super(plugin);
    }

    @Override
    public abstract void runCommand(CommandSender sender, List<String> args);

}
