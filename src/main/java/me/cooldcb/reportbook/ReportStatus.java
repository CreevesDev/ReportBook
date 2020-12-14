package me.cooldcb.reportbook;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ReportStatus implements CommandExecutor, Listener, TabCompleter {
    ReportBook plugin;

    public ReportStatus(ReportBook instance) {
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

    public List<String> listReportedUsersOpen(CommandSender sender) {
        List<String> reportedPlayerList = new ArrayList<>();
        File reports = new File(plugin.getDataFolder(), "reports.yml");
        YamlConfiguration reportYAML = YamlConfiguration.loadConfiguration(reports);
        Map<String, Object> reportedPlayers = reportYAML.getValues(false);


        for (Map.Entry<String, Object> reportedPlayer : reportedPlayers.entrySet()) {
            ConfigurationSection reportedPlayerSection = (ConfigurationSection) reportedPlayer.getValue();
            for (Map.Entry<String, Object> reportID : reportedPlayerSection.getValues(false).entrySet()) {
                ConfigurationSection reportIDSection = (ConfigurationSection) reportID.getValue();
                String reportedPlayerStr = reportedPlayer.getKey();
                UUID reportedPlayerUUID = UUID.fromString(reportedPlayerStr);
                String reportedPlayerName = Bukkit.getOfflinePlayer(reportedPlayerUUID).getName();
                reportedPlayerList.add(reportedPlayerName);
                break;
                }
            }
        return reportedPlayerList;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        if (!sender.hasPermission("reportbook.report.status")) {
            sender.sendMessage("§8§l[§d§lES§8§l] §7You have insufficient permissions.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§8§l[§d§lES§8§l] §7Incorrect usage, try §c/reportstatus <username> <reportid> <status>§7.");
            return true;
        }

        if (args.length == 1 || args.length == 2) {
            sender.sendMessage("§8§l[§d§lES§8§l] §7Incorrect usage, try §c/reportstatus <username> <reportid> <status>§7.");
            return true;
        }

        String username = args[0];
        String reportID = args[1];
        String status = args[2];

        int intReportID = Integer.parseInt(reportID);

        String usernameFriendly = getFriendlyName(username);

        String uuid = ReportBook.dataManager.nameToUUID(usernameFriendly).toString();

        if (intReportID <= 0) {
            sender.sendMessage("§8§l[§d§lES§8§l] §7A user cannot have §c0 §7or less reports.");
            return true;
        }

        File reports = new File(plugin.getDataFolder(),"reports.yml");
        YamlConfiguration reportYAML = YamlConfiguration.loadConfiguration(reports);
        int actualReportNum = reportYAML.getConfigurationSection(uuid).getKeys(false).size();
        if (intReportID > actualReportNum) {
            sender.sendMessage("§8§l[§d§lES§8§l] §7" + username + " only has §c" + actualReportNum + " §7reports.");
        }

        ConfigurationSection userReport = Objects.requireNonNull(reportYAML.getConfigurationSection(uuid)).getConfigurationSection(reportID);
        if (userReport != null) {
            userReport.set("status", status);
            try {
                reportYAML.save(reports);
                sender.sendMessage("§8§l[§d§lES§8§l] §7Status set to §c" + status + " §7to reopen the report do §c/reportstatus " + username + " " + intReportID + " open §7.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {

        List<String> empty = new ArrayList<>();

        if (!commandSender.hasPermission("reportbook.report.status")) {
            return empty;
        }

        File reports = new File(plugin.getDataFolder(), "reports.yml");
        YamlConfiguration reportYAML = YamlConfiguration.loadConfiguration(reports);
        List<String> reportID = new ArrayList<>();

        List<String> status = new ArrayList<>();

        status.add("open");
        status.add("in-progress");
        status.add("closed");


        if (args.length == 1) {
            return listReportedUsersOpen(commandSender);
        }
        if (args.length == 2) {
            String userFriendly = getFriendlyName(args[0]);
            if (userFriendly.equals("false")) {
                return empty;
            }

            String uuid = ReportBook.dataManager.nameToUUID(userFriendly).toString();

            if (!reportYAML.contains(uuid)) {
                return empty;
            }

            int actualReportNum = reportYAML.getConfigurationSection(uuid).getKeys(false).size();

            for (int index = 0; index < actualReportNum; index++) {
                String indexStr = Integer.toString(index + 1);
                reportID.add(indexStr);
            }
            return reportID;
        }

        if (args.length == 3) {
            return status;
        }

        return empty;
    }
}
