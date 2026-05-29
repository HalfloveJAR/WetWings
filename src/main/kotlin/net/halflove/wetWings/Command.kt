package net.halflove.wetWings

import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class Command(main: Main): CommandExecutor {
    private val plugin = main
    override fun onCommand(sender: CommandSender, cmd: Command, s: String, args: Array<out String>): Boolean {
        if (sender !is Player) return true

        if (cmd.name.equals("wetwings", ignoreCase = true)) {
            if (sender.hasPermission("ww.admin")) {
                if (args.isNotEmpty() && args[0].equals("reload", true)) {
                    plugin.reloadPlugin()
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aReloaded Wet Wings configuration options."))
                    return true
                }
                adminHelp(sender)
            }
            return true
        }

        return true
    }

    private fun adminHelp(sender: CommandSender) {
        sender.sendMessage(Utils.translateColorCodes("&3&lWet Wings"))
        sender.sendMessage(Utils.translateColorCodes(""))
        sender.sendMessage(Utils.translateColorCodes("&f/ww reload &7- Reloads the plugin"))
    }
}