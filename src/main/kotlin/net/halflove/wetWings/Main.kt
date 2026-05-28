package net.halflove.wetWings

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {

    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(ElytraSafetyManager, this)
    }

    override fun onDisable() {
    }
}