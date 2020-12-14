package me.cooldcb.reportbook;

import net.md_5.bungee.api.chat.*;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.*;

public class ReportCmd implements CommandExecutor, Listener, TabCompleter {
    ReportBook plugin;

    public ReportCmd(ReportBook instance) {
        plugin = instance;
    }

    public String getFriendlyName(String name) {
        for (String nameLoc : ReportBook.dataManager.getPlayerList().values()) {
            if (name.toLowerCase().equals(nameLoc.toLowerCase())) {
                return nameLoc;
            }
        }
        return "false";
    }

    public void componentAdder(ComponentBuilder compBuilder, String reportedUserFriendly, String reportCategory) {
        compBuilder.append(new ComponentBuilder("\n§8§l▢ §8" + reportCategory)
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/report " + reportedUserFriendly + " " + reportCategory))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Report " + reportedUserFriendly + " for " + reportCategory).create())).create());
    }

    public void verifyBook(String reportedUserFriendly, String crime, String crimeShort, CommandSender sender, Player player) {

        if (reportedUserFriendly.equals("false")) {
            sender.sendMessage("§8§l[§d§lES§8§l] §7This user has not connected to the server.");
            return;
        }

        ItemStack reportBook = new ItemStack(Material.WRITTEN_BOOK, 1);
        BookMeta bookMeta = (BookMeta) reportBook.getItemMeta();

        ComponentBuilder pageComponents = new ComponentBuilder("Report " + reportedUserFriendly + " for " + crimeShort + ".");

        pageComponents.append(new ComponentBuilder("\n\n\n\n\n\n\n\n\n\n\n").create()).create();


        pageComponents.append(new ComponentBuilder("§a§lCONFIRM")
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/report " + reportedUserFriendly + " " + crime + " confirm"))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Report " + reportedUserFriendly + " for " + crime + ".").create())).create());

        pageComponents.append(new ComponentBuilder("     ").create())
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("").create()));


        pageComponents.append(new ComponentBuilder("§c§lCANCEL")
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ""))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Cancel Report.").create())).create());

        BaseComponent[] page = pageComponents.create();

        bookMeta.setTitle("§c§lReport Book");
        bookMeta.setAuthor("FabulousDave");
        bookMeta.spigot().addPage(page);
        reportBook.setItemMeta(bookMeta);

        player.openBook(reportBook);
        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
    }

    public void subCategoryBook(String reportedUser, Player player, String category) {

        FileConfiguration config = plugin.getConfig();
        List<String> subCategories = config.getConfigurationSection("SubCategories").getStringList(category);

        config.getConfigurationSection("SubCategories").getValues(false);

        ItemStack reportBook = new ItemStack(Material.WRITTEN_BOOK, 1);
        BookMeta bookMeta = (BookMeta) reportBook.getItemMeta();

        ComponentBuilder pageComponents = new ComponentBuilder("Report " + reportedUser + ":\n");

        for (String subcategory: subCategories) {
            componentAdder(pageComponents, reportedUser, subcategory);
        }

        BaseComponent[] page = pageComponents.create();

        bookMeta.setTitle("Report Book");
        bookMeta.setAuthor("The Reporter");
        bookMeta.spigot().addPage(page);
        reportBook.setItemMeta(bookMeta);

        player.openBook(reportBook);
        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        if (!sender.hasPermission("reportbook.report")) {
            sender.sendMessage("§8§l[§d§lES§8§l] §7You have insufficient permissions.");
            return true;
        }

        FileConfiguration config = plugin.getConfig();
        List<String> categoryList = config.getStringList("Categories");
        Map<String, Object> subCategories = config.getConfigurationSection("SubCategories").getValues(false);

        if (args.length == 0) {
            sender.sendMessage("§8§l[§d§lES§8§l] §7Incorrect Usage, try §c/report <username>");
            return true;
        }

        String reportedUser = args[0];
        String reportedUserFriendly = getFriendlyName(reportedUser);

        if (args.length == 1) {
            if (args[0].toLowerCase().equals("help")) {
                sender.sendMessage("§8§l[§d§lES§8§l] §7Report Help: \n§c/report <username> §7- Report a user \n§c/report <username> <reason> §7- Report a user for a reason not found in the report book.");
                if (sender.hasPermission("reportbook.report.notify")) {
                    sender.sendMessage("§c/reports §7- Get a list of current, open reports. \n§c/reports <username> §7- Get an in-depth list of all reports for a player.");
                }
                if (sender.hasPermission("reportbook.report.mute")) {
                    sender.sendMessage("§c/report mute <username> §7- Mutes a user.");
                    sender.sendMessage("§c/report unmute <username> §7- Unmutes a user.");
                }
                if (sender.hasPermission("reportbook.report.status")) {
                    sender.sendMessage("§c/reportstatus <username> <reportid> <status> §7- Change the status of a user's report.");
                }
                if (sender.hasPermission("reportbook.report.whitelist")) {
                    sender.sendMessage("§c/report whitelist add <username> §7- Add a user to the Whitelist.");
                    sender.sendMessage("§c/report whitelist remove <username> §7- Remove a user from the Whitelist.");
                    sender.sendMessage("§c/report whitelist list §7- View the Whitelist.");
                }
                if (sender.hasPermission("reportbook.reload")) {
                    sender.sendMessage("§c/report reload §7- Reload the config.");
                }
                sender.sendMessage("§c/uuid <username> §7- Get a player's UUID.");
                return true;
            }

            if (args[0].toLowerCase().equals("reload")) {
                if (!sender.hasPermission("reportbook.reload")) {
                    sender.sendMessage("§8§l[§d§lES§8§l] §7You have insufficient permissions.");
                    return true;
                }
                plugin.reloadConfig();
                sender.sendMessage("§8§l[§d§lES§8§l] §aConfig Reloaded!");
                return true;
            }

            if (args[0].toLowerCase().equals("whitelist")) {
                if (!sender.hasPermission("reportbook.report.whitelist")) {
                    sender.sendMessage("§8§l[§d§lES§8§l] §7You have insufficient permissions.");
                    return true;
                }
                sender.sendMessage("§8§l[§d§lES§8§l] §7Incorrect Usage, try §c/report whitelist add/remove/list <username>");
                return true;
            }

            if (args[0].toLowerCase().equals("mute")) {
                if (!sender.hasPermission("reportbook.report.mute")) {
                    sender.sendMessage("§8§l[§d§lES§8§l] §7You have insufficient permissions.");
                    return true;
                }
                sender.sendMessage("§8§l[§d§lES§8§l] §7Incorrect Usage, try §c/report mute <username>");
                return true;
            }

            if (args[0].toLowerCase().equals("unmute")) {
                if (!sender.hasPermission("reportbook.report.mute")) {
                    sender.sendMessage("§8§l[§d§lES§8§l] §7You have insufficient permissions.");
                    return true;
                }
                sender.sendMessage("§8§l[§d§lES§8§l] §7Incorrect Usage, try §c/report unmute <username>");
                return true;
            }

            List<String> mutelist = config.getStringList("Mute List");

            for (String uuid : mutelist) {
                if (((Player) sender).getUniqueId().toString().equals(uuid)) {
                    sender.sendMessage("§8§l[§d§lES§8§l] §7You are currently blocked from using /report.");
                    return true;
                }
            }

            if (reportedUser.toLowerCase().equals(sender.getName().toLowerCase())) {
                sender.sendMessage("§8§l[§d§lES§8§l] §7You cannot report yourself.");
                return true;
            }

            if (reportedUserFriendly.equals("false")) {
                sender.sendMessage("§8§l[§d§lES§8§l] §7This user has not connected to the server.");
                return true;
            }

            List<String> whitelist = config.getStringList("Whitelist");
            String reportedUserUUID = ReportBook.dataManager.nameToUUID(reportedUserFriendly).toString();

            for (String uuid : whitelist) {
                if (reportedUserUUID.equals(uuid)) {
                    sender.sendMessage("§8§l[§d§lES§8§l] §7You cannot report this user.");
                    return true;
                }
            }

            ItemStack reportBook = new ItemStack(Material.WRITTEN_BOOK, 1);
            BookMeta bookMeta = (BookMeta) reportBook.getItemMeta();

            ComponentBuilder pageComponents = new ComponentBuilder("Report " + reportedUserFriendly + ":\n");

            for (String category : categoryList) {
                componentAdder(pageComponents, reportedUserFriendly, category);
            }

            BaseComponent[] page = pageComponents.create();

            bookMeta.setTitle("Report Book");
            bookMeta.setAuthor("Author");
            bookMeta.spigot().addPage(page);
            reportBook.setItemMeta(bookMeta);

            player.openBook(reportBook);
            player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
            return true;
        }

        if (args.length >= 2) {
            if (args[0].toLowerCase().equals("mute")) {
                if (!sender.hasPermission("reportbook.report.mute")) {
                    sender.sendMessage("§8§l[§d§lES§8§l] §7You have insufficient permissions.");
                    return true;
                }
                String username = args[1];
                String usernameFriendly = getFriendlyName(username);
                if (usernameFriendly.equals("false")) {
                    sender.sendMessage("§8§l[§d§lES§8§l] §7User not found.");
                    return true;
                }
                String userUUID = ReportBook.dataManager.nameToUUID(usernameFriendly).toString();
                List<String> muteList = config.getStringList("Mute List");

                for (String uuid : muteList) {
                    if (userUUID.equals(uuid)) {
                        sender.sendMessage("§8§l[§d§lES§8§l] §7This user is already muted.");
                        return true;
                    }
                }

                List<String> muteListValues = config.getStringList("Mute List");
                muteListValues.add(userUUID);
                config.set("Mute List", muteListValues);
                plugin.saveConfig();
                plugin.reloadConfig();
                sender.sendMessage("§8§l[§d§lES§8§l] §7You have muted §c" + usernameFriendly);
                return true;
            }

            if (args[0].toLowerCase().equals("unmute")) {
                if (!sender.hasPermission("reportbook.report.mute")) {
                    sender.sendMessage("§8§l[§d§lES§8§l] §7You have insufficient permissions.");
                    return true;
                }
                String username = args[1];
                String usernameFriendly = getFriendlyName(username);
                if (usernameFriendly.equals("false")) {
                    sender.sendMessage("§8§l[§d§lES§8§l] §7User not found.");
                    return true;
                }
                String userUUID = ReportBook.dataManager.nameToUUID(usernameFriendly).toString();
                List<String> muteList = config.getStringList("Mute List");

                for (String uuid : muteList) {
                    if (userUUID.equals(uuid)) {
                        List<String> muteListValues = config.getStringList("Mute List");
                        muteListValues.remove(userUUID);
                        config.set("Mute List", muteListValues);
                        plugin.saveConfig();
                        plugin.reloadConfig();
                        sender.sendMessage("§8§l[§d§lES§8§l] §7You have unmuted §c" + usernameFriendly);
                        return true;
                    }
                }
            }

            if (args[0].toLowerCase().equals("whitelist")) {
                if (!sender.hasPermission("reportbook.report.whitelist")) {
                    sender.sendMessage("§8§l[§d§lES§8§l] §7You have insufficient permissions.");
                    return true;
                }
                if (args[1].toLowerCase().equals("add")) {
                    if (args.length >= 3) {
                        String username = args[2];
                        String usernameFriendly = getFriendlyName(username);
                        if (usernameFriendly.equals("false")) {
                            sender.sendMessage("§8§l[§d§lES§8§l] §7User not found.");
                            return true;
                        }
                        String userUUID = ReportBook.dataManager.nameToUUID(usernameFriendly).toString();
                        List<String> whitelist = config.getStringList("Whitelist");

                        for (String uuid : whitelist) {
                            if (userUUID.equals(uuid)) {
                                sender.sendMessage("§8§l[§d§lES§8§l] §7This user is already in the whitelist.");
                                return true;
                            }
                        }

                        List<String> whitelistValues = config.getStringList("Whitelist");
                        whitelistValues.add(userUUID);
                        config.set("Whitelist", whitelistValues);
                        plugin.saveConfig();
                        plugin.reloadConfig();
                        sender.sendMessage("§8§l[§d§lES§8§l] §7Added §c" + usernameFriendly + " §7to the whitelist.");
                        return true;
                    }
                }
                if (args[1].toLowerCase().equals("remove")) {
                    if (args.length >= 3) {
                        String username = args[2];
                        String usernameFriendly = getFriendlyName(username);
                        if (usernameFriendly.equals("false")) {
                            sender.sendMessage("§8§l[§d§lES§8§l] §7User not found.");
                            return true;
                        }
                        String userUUID = ReportBook.dataManager.nameToUUID(usernameFriendly).toString();
                        List<String> whitelist = config.getStringList("Whitelist");

                        for (String uuid : whitelist) {
                            if (userUUID.equals(uuid)) {
                                List<String> whitelistValues = config.getStringList("Whitelist");
                                whitelistValues.remove(userUUID);
                                config.set("Whitelist", whitelistValues);
                                plugin.saveConfig();
                                plugin.reloadConfig();
                                sender.sendMessage("§8§l[§d§lES§8§l] §c" + usernameFriendly + " §7has been removed from the whitelist.");
                                return true;
                            }
                        }
                    }
                    sender.sendMessage("§8§l[§d§lES§8§l] §7This user is not in the whitelist");
                    return true;
                }
                if (args[1].toLowerCase().equals("list")) {
                    List<String> whitelistValues = config.getStringList("Whitelist");
                    sender.sendMessage("§8§l[§d§lES§8§l] §7Whitelist:");
                    for (String value : whitelistValues) {
                        UUID whitelistUUID = UUID.fromString(value);
                        Player whitelistPlayer = Bukkit.getPlayer(whitelistUUID);
                        if (whitelistPlayer == null) {
                            continue;
                        }
                        String whitelistName = whitelistPlayer.getName();
                        String name = getFriendlyName(whitelistName);
                        sender.sendMessage("§7" + name);
                    }
                    return true;
                }
            }

            if (args[args.length - 1].toLowerCase().equals("confirm")) {
                String categoryArgs = "";

                for (int index = 1; index < args.length; index++) {
                    if (categoryArgs.equals("")) {
                        categoryArgs = args[index];
                    } else {
                        categoryArgs = categoryArgs + " " + args[index];
                    }
                }

                categoryArgs = StringUtils.removeEnd(categoryArgs, " confirm");
                String reportedUserUUID = ReportBook.dataManager.nameToUUID(reportedUser).toString();

                ReportBook.dataManager.writeToYML(reportedUserUUID, categoryArgs, player.getName());
                sender.sendMessage("§8§l[§d§lES§8§l] §7Reported §c§l" + reportedUser + " §7successfully. Thank you for your report.");
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);

                for (Player name : Bukkit.getOnlinePlayers()) {
                    if (name.hasPermission("reportbook.report.notify")) {
                        name.sendMessage("§8§l[§d§lES§8§l] §7New Report on §c§l" + reportedUser + ". §7Type §c/reports " + reportedUser + " §7to view the report.");
                        name.playSound(name.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                    }
                }

                return true;
            }

            String categoryArgs = "";

            for (int index = 1; index < args.length; index++) {
                if (categoryArgs.equals("")) {
                    categoryArgs = args[index];
                } else {
                    categoryArgs = categoryArgs + " " + args[index];
                }
            }

            if (subCategories.containsKey(categoryArgs)) {
                subCategoryBook(reportedUserFriendly, player, categoryArgs);
                return true;
            }

            String argumentsShort = categoryArgs;

            if (categoryArgs.length() > 18) {
                argumentsShort = categoryArgs.substring(0, 18) + "...";
            }

            verifyBook(reportedUserFriendly, categoryArgs, argumentsShort, sender, player);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {

        List<String> tabComplete = new ArrayList<>();
        List<String> wordCompletion = new ArrayList<>();
        boolean wordCompletionSuccess = false;

        if (!commandSender.hasPermission("reportbook.report")) {
            return tabComplete;
        }

        if (args.length == 1) {
            Player[] players = new Player[Bukkit.getServer().getOnlinePlayers().size()];
            Bukkit.getServer().getOnlinePlayers().toArray(players);
            for (Player player : players) {
                tabComplete.add(player.getName());
            }
            tabComplete.add("help");

            if (commandSender.hasPermission("reportbook.report.mute")) {
                tabComplete.add("mute");
                tabComplete.add("unmute");
            }

            if (commandSender.hasPermission("reportbook.report.whitelist")) {
                tabComplete.add("whitelist");
            }
        }

        if (args.length == 2) {
            if (args[0].toLowerCase().equals("whitelist")) {
                if (commandSender.hasPermission("reportbook.report.whitelist")) {
                    tabComplete.add("add");
                    tabComplete.add("remove");
                    tabComplete.add("list");
                }
            }
        }

        if (args.length == 3) {
            if (args[0].toLowerCase().equals("whitelist")) {
                if (commandSender.hasPermission("reportbook.report.whitelist")) {
                    if (args[1].toLowerCase().equals("add") || args[1].toLowerCase().equals("remove")) {
                        Player[] players = new Player[Bukkit.getServer().getOnlinePlayers().size()];
                        Bukkit.getServer().getOnlinePlayers().toArray(players);
                        for (Player player : players) {
                            tabComplete.add(player.getName());
                        }
                    }
                }
            }
        }

        for (String currTab : tabComplete) {
            int currArg = args.length - 1;
            if (currTab.startsWith(args[currArg])) {
                wordCompletion.add(currTab);
                wordCompletionSuccess = true;
            }
        }

        if (wordCompletionSuccess) {
            return wordCompletion;
        }
        return tabComplete;
    }
}