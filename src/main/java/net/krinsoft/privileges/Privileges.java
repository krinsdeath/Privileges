package net.krinsoft.privileges;

import com.google.common.io.Files;
import com.pneumaticraft.commandhandler.CommandHandler;
import net.krinsoft.privileges.commands.BackupCommand;
import net.krinsoft.privileges.commands.BaseCommand;
import net.krinsoft.privileges.commands.CheckCommand;
import net.krinsoft.privileges.commands.DebugCommand;
import net.krinsoft.privileges.commands.DemoteCommand;
import net.krinsoft.privileges.commands.GroupBaseCommand;
import net.krinsoft.privileges.commands.GroupCheckCommand;
import net.krinsoft.privileges.commands.GroupCreateCommand;
import net.krinsoft.privileges.commands.GroupListCommand;
import net.krinsoft.privileges.commands.GroupOptionCommand;
import net.krinsoft.privileges.commands.GroupPermBaseCommand;
import net.krinsoft.privileges.commands.GroupPermRemoveCommand;
import net.krinsoft.privileges.commands.GroupPermSetCommand;
import net.krinsoft.privileges.commands.GroupRemoveCommand;
import net.krinsoft.privileges.commands.GroupRenameCommand;
import net.krinsoft.privileges.commands.GroupSetCommand;
import net.krinsoft.privileges.commands.InfoCommand;
import net.krinsoft.privileges.commands.ListCommand;
import net.krinsoft.privileges.commands.LoadCommand;
import net.krinsoft.privileges.commands.PermissionHandler;
import net.krinsoft.privileges.commands.ProfilingCommand;
import net.krinsoft.privileges.commands.PromoteCommand;
import net.krinsoft.privileges.commands.ReloadCommand;
import net.krinsoft.privileges.commands.RestoreCommand;
import net.krinsoft.privileges.commands.SaveCommand;
import net.krinsoft.privileges.commands.UserBaseCommand;
import net.krinsoft.privileges.commands.UserCleanCommand;
import net.krinsoft.privileges.commands.UserListCommand;
import net.krinsoft.privileges.commands.UserPermRemoveCommand;
import net.krinsoft.privileges.commands.UserPermSetCommand;
import net.krinsoft.privileges.commands.UserResetCommand;
import net.krinsoft.privileges.commands.VersionCommand;
import net.krinsoft.privileges.groups.GroupManager;
import net.krinsoft.privileges.importer.ImportManager;
import net.krinsoft.privileges.listeners.BlockListener;
import net.krinsoft.privileges.listeners.PlayerListener;
import net.krinsoft.privileges.players.PlayerManager;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author krinsdeath
 */
public class Privileges extends JavaPlugin {

    private boolean debug = false;
    private boolean on_start_clean = false;
    private int on_start_clean_period = 30;
    private boolean persist_default = true;
    private boolean read_only = false;

    // managers and handlers
    private PlayerManager playerManager;
    private GroupManager groupManager;
    private CommandHandler commandHandler;
    private FileConfiguration   configuration;
    private File                configFile;
    private FileConfiguration   users;
    private File                userFile;
    private FileConfiguration   groups;
    private File                groupFile;

    @Override
    public void onEnable() {
        registerConfiguration();
        registerPermissions();
        performImports();
        getServer().getScheduler().runTaskLater(this, new Runnable() {
            public void run() {
                boolean tmp = debug;
                debug = false;
                registerPermissions();
                updatePermissions();
                debug = tmp;
            }
        }, 5L);
        registerEvents();
        registerCommands();
        if (on_start_clean) {
            getServer().getScheduler().runTaskLater(this, new Runnable() {
                public void run() {
                    log("Removing old users from users.yml...");
                    long timeout = 1000L * 60L * 60L * 24L * on_start_clean_period;
                    for (OfflinePlayer player : getServer().getOfflinePlayers()) {
                        if (System.currentTimeMillis() - player.getLastPlayed() >= timeout || player.isBanned()) {
                            if ((getUsers().get(player.getName()) != null && !getUsers().getString(player.getName() + ".group").equals(getConfig().getString("default_group", "default"))) || player.isBanned()) {
                                getUsers().set(player.getName(), null);
                                debug("'" + player.getName() + "' removed from users.yml");
                            }
                        }
                    }
                    saveUsers();
                    log("... done!");
                }
            }, 1L);
        }
        try {
            getServer().getPluginManager().getPermission("privileges.*").setDefault(PermissionDefault.OP);
        } catch (NullPointerException e) {
            debug("Error setting default permission for 'privileges.*'");
        }

        try {
            // initialize the plugin metrics tracker
            Metrics metrics = new Metrics(this);
            // track the number of groups
            metrics.addCustomData(new Metrics.Plotter() {
                @Override
                public String getColumnName() {
                    return "Groups";
                }
                @Override
                public int getValue() {
                    ConfigurationSection groups = getGroups().getConfigurationSection("groups");
                    if (groups != null) {
                        return groups.getKeys(false).size();
                    }
                    return 0;
                }
            });
            log("[Metrics] Tracking total number of groups.");
            // track the number of users
            metrics.addCustomData(new Metrics.Plotter() {
                @Override
                public String getColumnName() {
                    return "Users";
                }
                @Override
                public int getValue() {
                    ConfigurationSection users = getUsers().getConfigurationSection("users");
                    if (users != null) {
                        return getUsers().getConfigurationSection("users").getKeys(false).size();
                    }
                    return 0;
                }
            });
            log("[Metrics] Tracking total number of users.");
            metrics.start();
        } catch (IOException e) {
            log("An error occurred while posting results to the Metrics.");
            warn(e.getLocalizedMessage());
        }
    }

    @Override
    public void onDisable() {
        playerManager.disable();
        //permissionManager.disable();
    }

    @Override
    public FileConfiguration getConfig() {
        if (configuration == null) {
            configuration = YamlConfiguration.loadConfiguration(configFile);
        }
        return configuration;
    }

    @Override
    public void saveConfig() {
        try {
            debug("config.yml checksum: " + sha256(configFile));
            getConfig().save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> allArgs = new ArrayList<String>();
        allArgs.addAll(Arrays.asList(args));
        allArgs.add(0, label);
        return commandHandler.locateAndRunCommand(sender, allArgs);
    }

    public void reload() {
        //permissionManager.clean();
        playerManager.disable();
        groupManager.clean();
        configuration = null;
        configFile = null;
        groups = null;
        groupFile = null;
        users = null;
        userFile = null;
        registerConfiguration();
        registerPermissions();
        updatePermissions();
    }

    private void registerPermissions() {
        playerManager = new PlayerManager(this);
        groupManager = new GroupManager(this);
        registerDynamicPermissions();
    }

    private void registerDynamicPermissions() {
        Permission root = new Permission("privileges.*");
        if (getServer().getPluginManager().getPermission(root.getName()) == null) {
            getServer().getPluginManager().addPermission(root);
        }
        root.getChildren().put("privileges.admins", true);
        root.recalculatePermissibles();
    }

    private void updatePermissions() {
        groupManager.reload();
        playerManager.reload();
        //permissionManager.reload();
    }

    private void registerConfiguration() {
        configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            getConfig().setDefaults(YamlConfiguration.loadConfiguration(this.getClass().getResourceAsStream("/config.yml")));
            getConfig().options().copyDefaults(true);
            saveConfig();
        }

        userFile = new File(getDataFolder(), "users.yml");
        if (!userFile.exists()) {
            getUsers().setDefaults(YamlConfiguration.loadConfiguration(this.getClass().getResourceAsStream("/users.yml")));
            getUsers().options().copyDefaults(true);
            saveUsers();
        }

        groupFile = new File(getDataFolder(), "groups.yml");
        if (!groupFile.exists()) {
            getGroups().setDefaults(YamlConfiguration.loadConfiguration(this.getClass().getResourceAsStream("/groups.yml")));
            getGroups().options().header(
                    "Group ranks determine the order they are promoted in.\n" +
                            "Lowest rank is 1, highest rank is 2,147,483,647.\n" +
                            "Visit https://github.com/krinsdeath/Privileges/wiki for help with configuration\n" +
                            "World nodes override global nodes for that group\n" +
                            "Inherited groups are calculated first. Each group in the tree overrides any nodes\n" +
                            "from the previous group.");
            getGroups().options().copyDefaults(true);
            saveGroups();
        }

        if (getConfig().get("default_group") == null) {
            getConfig().set("default_group", "default");
            getConfig().set("debug", false);
            saveConfig();
        }
        debug = getConfig().getBoolean("debug", false);
        if (getConfig().get("users") == null) {
            getConfig().set("users.persist_default", true);
            getConfig().set("users.clean_old", true);
            getConfig().set("users.clean_after_days", 30);
            saveConfig();
        }
        read_only = getConfig().getBoolean("read_only", false);
        persist_default = getConfig().getBoolean("users.persist_default", true);
        on_start_clean = getConfig().getBoolean("users.clean_old", true);
        on_start_clean_period = getConfig().getInt("users.clean_after_days", 30);
    }

    private void performImports() {
        // broken until I can improve it
        new ImportManager(this);
    }

    private void registerEvents() {
        PluginManager manager = this.getServer().getPluginManager();
        PlayerListener pListener = new PlayerListener(this);
        BlockListener bListener = new BlockListener(this);
        manager.registerEvents(pListener, this);
        manager.registerEvents(bListener, this);
    }

    private void registerCommands() {
        PermissionHandler permissionHandler = new PermissionHandler();
        commandHandler = new CommandHandler(this, permissionHandler);
        // base & informational
        commandHandler.registerCommand(new BaseCommand(this));
        commandHandler.registerCommand(new GroupBaseCommand(this));
        commandHandler.registerCommand(new GroupPermBaseCommand(this));
        commandHandler.registerCommand(new UserBaseCommand(this));
        // miscellaneous commands
        commandHandler.registerCommand(new BackupCommand(this));
        commandHandler.registerCommand(new CheckCommand(this));
        commandHandler.registerCommand(new DebugCommand(this));
        commandHandler.registerCommand(new DemoteCommand(this));
        commandHandler.registerCommand(new InfoCommand(this));
        commandHandler.registerCommand(new ListCommand(this));
        commandHandler.registerCommand(new LoadCommand(this));
        commandHandler.registerCommand(new ProfilingCommand(this));
        commandHandler.registerCommand(new PromoteCommand(this));
        commandHandler.registerCommand(new ReloadCommand(this));
        commandHandler.registerCommand(new RestoreCommand(this));
        commandHandler.registerCommand(new SaveCommand(this));
        commandHandler.registerCommand(new VersionCommand(this));
        // group related commands
        commandHandler.registerCommand(new GroupCheckCommand(this));
        commandHandler.registerCommand(new GroupCreateCommand(this));
        commandHandler.registerCommand(new GroupOptionCommand(this));
        commandHandler.registerCommand(new GroupRemoveCommand(this));
        commandHandler.registerCommand(new GroupRenameCommand(this));
        commandHandler.registerCommand(new GroupSetCommand(this));
        commandHandler.registerCommand(new GroupListCommand(this));
        commandHandler.registerCommand(new GroupPermSetCommand(this));
        commandHandler.registerCommand(new GroupPermRemoveCommand(this));
        // user related commands
        commandHandler.registerCommand(new UserCleanCommand(this));
        commandHandler.registerCommand(new UserListCommand(this));
        commandHandler.registerCommand(new UserPermSetCommand(this));
        commandHandler.registerCommand(new UserPermRemoveCommand(this));
        commandHandler.registerCommand(new UserResetCommand(this));
    }

    public ConfigurationSection getUserNode(String player) {
        ConfigurationSection user = getUsers().getConfigurationSection("users." + player);
        if (!player.equals(player.toLowerCase()) && user != null) {
            getUsers().set("users." + player.toLowerCase() + ".group", user.getString("group"));
            getUsers().set("users." + player.toLowerCase() + ".permissions", user.getStringList("permissions"));
            for (World w : getServer().getWorlds()) {
                getUsers().set("users." + player.toLowerCase() + ".worlds." + w.getName(), user.getStringList("worlds." + w.getName()));
            }
            getUsers().set("users." + player, null);
            saveUsers();
            debug("User node for '" + player + "' converted to lower case.");
            return getUsers().getConfigurationSection("users." + player.toLowerCase());
        }
        user = getUsers().getConfigurationSection("users." + player.toLowerCase());
        if (user == null || user.getString("group") == null) {
            String path = "users." + player.toLowerCase();
            ConfigurationSection node = new MemoryConfiguration();
            node.set("group", getConfig().getString("default_group", "default"));
            node.set("permissions", null);
            for (World w : getServer().getWorlds()) {
                node.set("worlds." + w.getName(), null);
            }
            if (persist_default) {
                getUsers().set("users." + player.toLowerCase(), node);
                saveUsers();
            }
            debug("New user node for '" + player + "' created with default group '" + getConfig().getString("default_group", "default") + "'.");
            user = node;
        }
        return user;
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
            users = YamlConfiguration.loadConfiguration(userFile);
        }
        return users;
    }

    private boolean saving = false;

    public void saveUsers() {
        if (read_only) { return; }
        if (!saving) {
            saving = true;
            try {
                debug("users.yml checksum: " + sha256(userFile));
                getUsers().save(userFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            saving = false;
        }
    }

    public FileConfiguration getGroups() {
        if (groups == null) {
            groups = YamlConfiguration.loadConfiguration(groupFile);
        }
        return groups;
    }

    public void saveGroups() {
        if (read_only) { return; }
        try {
            debug("groups.yml checksum: " + sha256(groupFile));
            getGroups().save(groupFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void log(String message) {
        getLogger().info(message);
    }
    
    public void warn(String message) {
        getLogger().warning(message);
    }
    
    public void debug(String message) {
        if (debug) {
            message = "[Debug] " + message;
            getLogger().info(message);
        }
    }

    public void toggleDebug(boolean val) {
        debug = !val;
        toggleDebug();
    }
    
    public void toggleDebug() {
        debug = !debug;
        getConfig().set("debug", debug);
        saveConfig();
        log("Debug mode is now " + (debug ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled") + ChatColor.WHITE + ".");
    }

    public PlayerManager getPlayerManager() {
        return this.playerManager;
    }

    public GroupManager getGroupManager() {
        return this.groupManager;
    }

    /**
     * Creates a sha-256 hash of the specified file
     * @param file The file we're creating a sha-256 hash for
     * @return The hash, as a string
     */
    private String sha256(File file) {
        try {
            byte[] bytes = Files.getDigest(file, MessageDigest.getInstance("SHA-256"));
            StringBuilder checksum = new StringBuilder();
            for (byte b : bytes) {
                checksum.append(b);
            }
            return checksum.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            if (e instanceof FileNotFoundException) {
                return e.getLocalizedMessage();
            }
            e.printStackTrace();
        }
        return null;
    }

}
