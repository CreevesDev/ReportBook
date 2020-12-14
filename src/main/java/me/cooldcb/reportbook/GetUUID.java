package me.cooldcb.reportbook;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class GetUUID implements CommandExecutor, Listener, TabCompleter {
    ReportBook plugin;

    public GetUUID(ReportBook instance) {
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

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.hasPermission("reportbook.command.uuid")) {
            sender.sendMessage("§8§l[§d§lES§8§l] §7You have insufficient permissions.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§8§l[§d§lES§8§l] §7Incorrect Usage, try §c/uuid <username>");
            return true;
        }

        String reportedUser = args[0];
        String reportedUserFriendly = getFriendlyName(reportedUser);

        if (reportedUserFriendly.equals("false")) {
            sender.sendMessage("§8§l[§d§lES§8§l] §7This user has not connected to the server.");
            return true;
        }

        UUID uuid = ReportBook.dataManager.nameToUUID(reportedUserFriendly);

        sender.sendMessage("§8§l[§d§lES§8§l] §c" + reportedUserFriendly +"§7's UUID is §c" + uuid);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {

        List<String> empty = new ArrayList<>();

        if (!commandSender.hasPermission("reportbook.report.notify")) {
            return empty;
        }

        if (args.length == 1) {
            return null;
        }

        return empty;
    }
}
