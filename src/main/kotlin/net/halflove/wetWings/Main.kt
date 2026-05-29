package net.halflove.wetWings

import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {

    lateinit var elytraSafetyManager: ElytraSafetyManager

    override fun onEnable() {
        reloadPlugin()
        getCommand("wetwings")?.setExecutor(Command(this))
    }

    override fun onDisable() {
    }

    private fun setupConfig() {
        saveDefaultConfig()
        reloadConfig()
        saveConfig()
    }

    private fun setupPlugin() {
        elytraSafetyManager = ElytraSafetyManager(this)
        Bukkit.getPluginManager().registerEvents(elytraSafetyManager, this)
    }

    fun reloadPlugin() {
        HandlerList.unregisterAll(this)
        setupConfig()
        setupPlugin()
    }
}