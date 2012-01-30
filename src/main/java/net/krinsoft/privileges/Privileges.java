package net.krinsoft.privileges;

import com.pneumaticraft.commandhandler.CommandHandler;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import net.krinsoft.privileges.commands.*;
import net.krinsoft.privileges.groups.GroupManager;
import net.krinsoft.privileges.importer.ImportManager;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author krinsdeath
 */
public class Privileges extends JavaPlugin {

    private final static Logger LOGGER = Logger.getLogger("Privileges");
    private boolean debug = false;
    public static Privileges instance;

    // managers and handlers
    private PermissionManager permissionManager;
    private GroupManager groupManager;
    private CommandHandler commandHandler;
    private FileConfiguration users;
    private FileConfiguration groups;

    @Override
    public void onEnable() {
        instance = this;
        if (!validateCommandHandler()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        registerConfiguration();
        performImports();
        registerPermissions();
        registerEvents();
        registerCommands();
        try {
            getServer().getPluginManager().getPermission("privileges.*").setDefault(PermissionDefault.OP);
        } catch (NullPointerException e) {
            debug("Error setting default permission for 'privileges.*'");
        }
        info("Is now enabled.");
    }

    @Override
    public void onDisable() {
        this.permissionManager.disable();
        info("Is now disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> allArgs = new ArrayList<String>();
        allArgs.addAll(Arrays.asList(args));
        allArgs.add(0, label);
        return commandHandler.locateAndRunCommand(sender, allArgs);
    }

    private boolean validateCommandHandler() {
        double chVersion = 1;
        try {
            commandHandler = new CommandHandler(this, null);
            if (this.commandHandler.getVersion() >= chVersion) {
                return true;
            } else {
                LOGGER.warning("A plugin with an outdated version of CommandHandler initialized before " + this + ".");
                LOGGER.warning(this + " needs CommandHandler v" + chVersion + " or higher, but CommandHandler v" + commandHandler.getVersion() + " was detected.");
                return false;
            }
        } catch (Throwable t) {
            LOGGER.warning("A plugin with an outdated version of CommandHandler initialized before " + this + ".");
            LOGGER.warning(this + " needs CommandHandler v" + chVersion + " or higher, but CommandHandler v" + commandHandler.getVersion() + " was detected.");
            return false;
        }
    }
    
    public void registerPermissions() {
        this.groupManager = new GroupManager(this);
        if (this.permissionManager != null) { this.permissionManager.disable(); }
        this.permissionManager = new PermissionManager(this);
    }

    public void registerConfiguration(boolean val) {
        if (val) {
            users = null;
            groups = null;
            registerConfiguration();
        }
    }
    
    public void registerConfiguration() {
        getUsers().setDefaults(YamlConfiguration.loadConfiguration(this.getClass().getResourceAsStream("/users.yml")));
        if (!new File(getDataFolder(), "users.yml").exists()) {
            getUsers().options().copyDefaults(true);
        }
        saveUsers();
        
        getGroups().setDefaults(YamlConfiguration.loadConfiguration(this.getClass().getResourceAsStream("/groups.yml")));
        if (!new File(getDataFolder(), "groups.yml").exists()) {
            getGroups().options().copyDefaults(true);
        }
        groups.options().header(
                "Group ranks determine the order they are promoted in.\n" +
                        "Lowest rank is 1, highest rank is 2,147,483,647.\n" +
                        "Visit https://github.com/krinsdeath/Privileges/wiki for help with configuration\n" +
                        "World nodes override global nodes for that group\n" +
                        "Inherited groups are calculated first. Each group in the tree overrides any nodes\n" +
                        "from the previous group.");
        saveGroups();

        getConfig();
        if (getConfig().get("default_group") == null) {
            getConfig().set("default_group", "default");
            getConfig().set("debug", false);
            saveConfig();
        }
        debug = getConfig().getBoolean("debug", false);
    }

    private void performImports() {
        new ImportManager(this);
    }

    private void registerEvents() {
        PluginManager manager = this.getServer().getPluginManager();
        PlayerListener pListener = new PlayerListener(this);
        BlockListener bListener = new BlockListener(this);
        manager.registerEvent(Type.PLAYER_JOIN, pListener, Priority.Lowest, this);
        manager.registerEvent(Type.PLAYER_QUIT, pListener, Priority.Monitor, this);
        manager.registerEvent(Type.PLAYER_KICK, pListener, Priority.Monitor, this);
        manager.registerEvent(Type.PLAYER_CHANGED_WORLD, pListener, Priority.Monitor, this);
        manager.registerEvent(Type.PLAYER_INTERACT, pListener, Priority.Lowest, this);
        manager.registerEvent(Type.BLOCK_PLACE, bListener, Priority.Lowest, this);
        manager.registerEvent(Type.BLOCK_BREAK, bListener, Priority.Lowest, this);
    }

    private void registerCommands() {
        PermissionHandler permissionHandler = new PermissionHandler();
        commandHandler = new CommandHandler(this, permissionHandler);
        commandHandler.registerCommand(new ReloadCommand(this));
        commandHandler.registerCommand(new VersionCommand(this));
        commandHandler.registerCommand(new DebugCommand(this));
        commandHandler.registerCommand(new ListCommand(this));
        commandHandler.registerCommand(new CheckCommand(this));
        commandHandler.registerCommand(new HelpCommand(this));
        commandHandler.registerCommand(new PromoteCommand(this));
        commandHandler.registerCommand(new GroupCreateCommand(this));
        commandHandler.registerCommand(new GroupRemoveCommand(this));
        commandHandler.registerCommand(new GroupSetCommand(this));
        commandHandler.registerCommand(new GroupShowCommand(this));
        commandHandler.registerCommand(new GroupPermSetCommand(this));
        commandHandler.registerCommand(new GroupPermRemoveCommand(this));
        commandHandler.registerCommand(new UserPermSetCommand(this));
        commandHandler.registerCommand(new UserPermRemoveCommand(this));
    }

    public ConfigurationSection getUserNode(String player) {
        if (getUsers().getConfigurationSection("users." + player) == null) {
            FileConfiguration c = getUsers();
            String path = "users." + player;
            c.set(path + ".permissions", null);
            c.set(path + ".group", getConfig().getString("default_group", "default"));
            for (World w : getServer().getWorlds()) {
                c.set(path + ".worlds." + w.getName(), null);
            }
            saveUsers();
            debug("Empty user node for '" + player + "' created.");
        }
        return getUsers().getConfigurationSection("users." + player);
    }

    public ConfigurationSection getGroupNode(String group) {
        if (getGroups().getConfigurationSection("groups." + group) == null) {
            debug("Empty group node '" + group + "' detected.");
            return null;
        }
        return getGroups().getConfigurationSection("groups." + group);
    }

    public void buildGroup(String group) {
        if (getGroups().getConfigurationSection("groups." + group) == null) {
            getGroups().set("groups." + group + ".permissions", null);
            getGroups().set("groups." + group + ".worlds", null);
            getGroups().set("groups." + group + ".inheritance", null);
            saveGroups();
        }
    }

    public FileConfiguration getUsers() {
        if (users == null) {
            users = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "users.yml"));
        }
        return users;
    }

    public void saveUsers() {
        try {
            users.save(new File(this.getDataFolder(), "users.yml"));
        } catch (IOException ex) {
            debug(ex.getLocalizedMessage());
        }
    }

    public FileConfiguration getGroups() {
        if (groups == null) {
            groups = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "groups.yml"));
        }
        return groups;
    }

    public void saveGroups() {
        try {
            groups.save(new File(this.getDataFolder(), "groups.yml"));
        } catch (IOException ex) {
            debug(ex.getLocalizedMessage());
        }
    }

    public void info(Object message) {
        LOGGER.info(String.valueOf("[" + this + "] " + message));
    }

    public void log(String message) {
        message = "[" + this + "] " + message;
        LOGGER.info(message);
    }

    public void debug(String message) {
        if (debug) {
            message = "[" + this + "] [Debug] " + message;
            LOGGER.info(message);
        }
    }

    public void toggleDebug(String flip) {
        if (flip.equals("--flip")) {
            debug = !debug;
        } else {
            debug = Boolean.valueOf(flip);
        }
        getConfig().set("debug", debug);
        saveConfig();
        info("Debug mode is now " + (debug ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled") + ChatColor.WHITE + ".");
    }

    public PermissionManager getPermissionManager() {
        return this.permissionManager;
    }

    public GroupManager getGroupManager() {
        return this.groupManager;
    }

    /**
     * Warning! This method is probably inefficient!
     * Fetches a list of permissions nodes contained in groups.yml for the specified group
     * @param group The group whose nodes we're checking.
     * @param world The world (or null) to check nodes on
     * @return A list of the nodes available for the specified group (optional: on the given world)
     */
    public List<String> calculateNodeList(String group, String world) {
        List<String> tree = permissionManager.calculateGroupTree(group, "-");
        Set<String> nodes = new HashSet<String>();
        for (String g : tree) {
            Set<String> nodeList = new HashSet<String>(getGroupNode(g).getStringList("permissions"));
            for (String node : nodeList) {
                if (node.startsWith("-")) {
                    nodes.remove(node.substring(1));
                }
                nodes.add(node);
            }
            if (world != null) {
                nodeList = new HashSet<String>(getGroupNode(g).getStringList("worlds." + world));
                for (String node : nodeList) {
                    if (node.startsWith("-") && nodes.contains(node.substring(1))) {
                        nodes.remove(node.substring(1));
                    }
                    nodes.add(node);
                }
            }
        }
        return new ArrayList<String>(nodes);
    }
}
