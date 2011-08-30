package net.krinsoft.privileges;

import com.pneumaticraft.commandhandler.CommandHandler;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import net.krinsoft.privileges.commands.CheckCommand;
import net.krinsoft.privileges.commands.GroupCommand;
import net.krinsoft.privileges.commands.GroupCreateCommand;
import net.krinsoft.privileges.commands.GroupRemoveCommand;
import net.krinsoft.privileges.commands.ListCommand;
import net.krinsoft.privileges.commands.ReloadCommand;
import net.krinsoft.privileges.groups.GroupManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;

/**
 *
 * @author krinsdeath
 */
public class Privileges extends JavaPlugin {

    private final static Logger LOGGER = Logger.getLogger("Privileges");
    private boolean debug = false;

    // managers and handlers
    private PermissionManager permissionManager;
    private GroupManager groupManager;
    private CommandHandler commandHandler;
    private PermissionHandler permissionHandler;
    private Configuration users;
    private Configuration groups;

    @Override
    public void onEnable() {
        registerConfiguration();
        registerPermissions();
        registerEvents();
        registerCommands();
        LOGGER.info(this + " is now enabled.");
    }

    @Override
    public void onDisable() {
        this.permissionManager.disable();
        LOGGER.info(this + " is now disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> allArgs = new ArrayList<String>();
        allArgs.addAll(Arrays.asList(args));
        allArgs.add(0, label);
        return commandHandler.locateAndRunCommand(sender, allArgs);
    }

    public void registerPermissions() {
        this.groupManager = new GroupManager(this);
        if (this.permissionManager != null) { this.permissionManager.disable(); }
        this.permissionManager = new PermissionManager(this);
    }

    private void registerConfiguration() {
        users = new Configuration(new File(this.getDataFolder(), "users.yml"));
        users.load();
        groups = new Configuration(new File(this.getDataFolder(), "groups.yml"));
        groups.load();
        Configuration config = getConfiguration();
        if (config.getProperty("default_group") == null) {
            config.setProperty("default_group", "default");
            config.setProperty("debug", false);
            groups.setHeader(
                    "# Group ranks determine the order they are promoted in.",
                    "# Lowest rank is 1, highest rank is 2,147,483,647.",
                    "# Visit https://github.com/krinsdeath/Privileges/wiki for help with configuration");
            groups.setProperty("groups.default.rank", 1);
            groups.setProperty("groups.default.permissions", Arrays.asList("-privileges.build", "-privileges.interact"));
            groups.setProperty("groups.default.worlds.world", Arrays.asList("-example.basic.node2"));
            groups.setProperty("groups.default.worlds.world_nether", Arrays.asList("-example.basic.node1"));
            groups.setProperty("groups.default.inheritance", new ArrayList<String>());
            groups.setProperty("groups.user.rank", 2);
            groups.setProperty("groups.user.permissions", Arrays.asList("privileges.build", "privileges.check"));
            groups.setProperty("groups.user.inheritance", Arrays.asList("default"));
            groups.setProperty("groups.admin.rank", 3);
            groups.setProperty("groups.admin.permissions", Arrays.asList("privileges.promote"));
            groups.setProperty("groups.admin.inheritance", Arrays.asList("user"));
            groups.save();
            config.save();
        }
        debug = config.getBoolean("debug", false);
    }

    private void registerEvents() {
        PluginManager manager = this.getServer().getPluginManager();
        PlayerListener pListener = new PlayerListener(this);
        BlockListener bListener = new BlockListener(this);
        manager.registerEvent(Type.PLAYER_JOIN, pListener, Priority.Lowest, this);
        manager.registerEvent(Type.PLAYER_QUIT, pListener, Priority.Monitor, this);
        manager.registerEvent(Type.PLAYER_KICK, pListener, Priority.Monitor, this);
        manager.registerEvent(Type.PLAYER_TELEPORT, pListener, Priority.Monitor, this);
        manager.registerEvent(Type.PLAYER_PORTAL, pListener, Priority.Monitor, this);
        manager.registerEvent(Type.PLAYER_INTERACT, pListener, Priority.Lowest, this);
        manager.registerEvent(Type.BLOCK_PLACE, bListener, Priority.Lowest, this);
        manager.registerEvent(Type.BLOCK_BREAK, bListener, Priority.Lowest, this);
    }

    private void registerCommands() {
        permissionHandler = new PermissionHandler();
        commandHandler = new CommandHandler(this, permissionHandler);
        commandHandler.registerCommand(new ReloadCommand(this));
        commandHandler.registerCommand(new ListCommand(this));
        commandHandler.registerCommand(new CheckCommand(this));
        commandHandler.registerCommand(new GroupCommand(this));
        commandHandler.registerCommand(new GroupCreateCommand(this));
        commandHandler.registerCommand(new GroupRemoveCommand(this));
    }

    public ConfigurationNode getUserNode(String player) {
        if (getUsers().getNode("users." + player) == null) {
            Configuration c = getUsers();
            String path = "users." + player;
            c.setProperty(path + ".permissions", null);
            c.setProperty(path + ".groups", Arrays.asList(getConfiguration().getString("default_group", "default")));
            c.save();
            debug("Empty user node for '" + player + "' created.");
        }
        return getUsers().getNode("users." + player);
    }

    public ConfigurationNode getGroupNode(String group) {
        if (getGroups().getNode("groups." + group) == null) {
            debug("Empty group node detected.");
            return null;
        }
        return getGroups().getNode("groups." + group);
    }

    public Configuration getUsers() {
        return users;
    }

    public Configuration getGroups() {
        return groups;
    }

    protected void debug(String message) {
        if (debug) {
            message = "[" + this + "] [Debug] " + message;
            LOGGER.info(message);
        }
    }

    public PermissionHandler getPermissionHandler() {
        return this.permissionHandler;
    }

    public PermissionManager getPermissionManager() {
        return this.permissionManager;
    }

    public GroupManager getGroupManager() {
        return this.groupManager;
    }
}
