package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.Privileges;
import org.bukkit.command.CommandSender;

import java.util.List;

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
