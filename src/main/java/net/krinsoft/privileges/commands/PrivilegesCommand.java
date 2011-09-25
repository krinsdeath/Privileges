package net.krinsoft.privileges.commands;

import com.pneumaticraft.commandhandler.Command;
import java.util.List;
import net.krinsoft.privileges.Privileges;
import org.bukkit.command.CommandSender;

/**
 *
 * @author krinsdeath
 */
public abstract class PrivilegesCommand extends Command {

    protected Privileges plugin;
    
    public PrivilegesCommand(Privileges plugin) {
        super(plugin);
        this.plugin = (Privileges) plugin;
    }

    @Override
    public abstract void runCommand(CommandSender sender, List<String> args);

}
