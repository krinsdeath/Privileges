package net.krinsoft.privileges.commands;

import com.pneumaticraft.commandhandler.Command;
import net.krinsoft.privileges.Privileges;

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

}
