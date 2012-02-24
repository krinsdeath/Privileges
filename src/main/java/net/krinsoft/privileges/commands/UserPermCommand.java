package net.krinsoft.privileges.commands;

import java.util.List;
import net.krinsoft.privileges.PermissionManager;
import net.krinsoft.privileges.Privileges;
import org.bukkit.command.CommandSender;

/**
 *
 * @author krinsdeath
 */
public abstract class UserPermCommand extends UserCommand {

    protected PermissionManager permManager;

    public UserPermCommand(Privileges plugin) {
        super(plugin);
        permManager = plugin.getPermissionManager();
    }

    @Override
    public abstract void runCommand(CommandSender sender, List<String> args);

}
