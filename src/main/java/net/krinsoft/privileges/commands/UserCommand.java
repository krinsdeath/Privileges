package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.Privileges;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * @author krinsdeath
 */
public abstract class UserCommand extends PrivilegesCommand {

    public UserCommand(Privileges instance) {
        super(instance);
    }

    public abstract void runCommand(CommandSender sender, List<String> args);

}
