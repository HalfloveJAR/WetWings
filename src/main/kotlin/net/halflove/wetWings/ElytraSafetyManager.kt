package net.halflove.wetWings

import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.block.Biome
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityToggleGlideEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.UUID

class ElytraSafetyManager(main: Main) : Listener {

    private val plugin = main
    val activeSlowFallPlayers = mutableSetOf<UUID>()

    @EventHandler
    fun onElytraToggle(event: EntityToggleGlideEvent) {
        val player = event.entity as? Player ?: return

        if (!event.isGliding) return

        if (isWet(player) || isBreachingWater(player)) {
            event.isCancelled = true
            sendWaterloggedMessage(player)
            applySafetySlowFalling(player)
            return
        }

        removeSafetySlowFalling(player)
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        val to = event.to ?: return
        val from = event.from
        val playerUUID = player.uniqueId

        if (activeSlowFallPlayers.contains(playerUUID)) {
            if (isServerSideOnGround(player) || player.isFlying || player.isDead) {
                removeSafetySlowFalling(player)
            }
        }

        if (!player.isGliding) return

        if (from.blockX == to.blockX && from.blockY == to.blockY && from.blockZ == to.blockZ) {
            return
        }

        if (isWet(player)) {
            player.isGliding = false
            sendWaterloggedMessage(player)
            applySafetySlowFalling(player)
        }
    }

    @EventHandler
    fun onPlayerTeleport(event: PlayerTeleportEvent) {
        if (activeSlowFallPlayers.contains(event.player.uniqueId)) {
            removeSafetySlowFalling(event.player)
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        activeSlowFallPlayers.remove(event.player.uniqueId)
    }

    // If the server reboots or the player disconnects before we can take away their
    // slow falling effect, this will remove it forcefully when they log back in.
    @EventHandler
    fun onPlayerJoin(event: org.bukkit.event.player.PlayerJoinEvent) {
        val player = event.player
        val effect = player.getPotionEffect(PotionEffectType.SLOW_FALLING)

        // When we apply slow falling, we set it to have no particles
        if (effect != null && !effect.hasParticles()) {
            player.removePotionEffect(PotionEffectType.SLOW_FALLING)
        }
    }

    private fun applySafetySlowFalling(player: Player) {
        if (!plugin.config.getBoolean("slowfall-when-deactivated", true)) return
        if (isServerSideOnGround(player)) return

        val slowFallingEffect = PotionEffect(
            PotionEffectType.SLOW_FALLING,
            -1,
            0,
            false,
            false,
            false
        )
        player.addPotionEffect(slowFallingEffect)
        activeSlowFallPlayers.add(player.uniqueId)
    }

    fun removeSafetySlowFalling(player: Player) {
        player.removePotionEffect(PotionEffectType.SLOW_FALLING)
        activeSlowFallPlayers.remove(player.uniqueId)
    }

    private fun isWet(player: Player): Boolean {
        val waterTriggerEnabled = plugin.config.getBoolean("triggers.water", true)
        if (waterTriggerEnabled && player.isInWater) return true

        // If rain triggers are disabled we don't need to do any more checks
        if (!plugin.config.getBoolean("triggers.rain", true)) return false

        val world = player.world
        if (!world.hasStorm()) return false

        val loc = player.location
        if (loc.blockY < world.getHighestBlockYAt(loc)) return false

        return canBiomeRain(loc.block.biome)
    }

    private fun isBreachingWater(player: Player): Boolean {
        val location = player.location
        val breachDistance = plugin.config.getInt("water-breach-distance", 2)

        for (i in 1..breachDistance) {
            val blockBelow = location.clone().subtract(0.0, i.toDouble(), 0.0).block
            if (blockBelow.type == org.bukkit.Material.WATER) {
                return true
            }
        }

        return false
    }

    private fun isServerSideOnGround(player: Player): Boolean {
        val location = player.location
        val blockBelow = location.clone().subtract(0.0, 0.1, 0.0).block

        return blockBelow.type.isSolid || location.block.type.isSolid || player.isInWater
    }

    private fun canBiomeRain(biome: Biome): Boolean {
        val name = biome.name()

        if (name.contains("DESERT") || name.contains("SAVANNA") || name.contains("BADLANDS")) return false
        if (name.contains("SNOWY") || name.contains("FROZEN") || name.contains("ICE")) return false

        return when (biome) {
            Biome.GROVE, Biome.JAGGED_PEAKS, Biome.THE_END, Biome.THE_VOID,
            Biome.NETHER_WASTES, Biome.SOUL_SAND_VALLEY, Biome.CRIMSON_FOREST,
            Biome.WARPED_FOREST, Biome.BASALT_DELTAS, Biome.END_BARRENS,
            Biome.END_HIGHLANDS, Biome.END_MIDLANDS, Biome.SMALL_END_ISLANDS -> false
            else -> true
        }
    }

    private fun sendWaterloggedMessage(player: Player) {
        player.spigot().sendMessage(
            ChatMessageType.ACTION_BAR,
            TextComponent(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("activation-actionbar") ?: "&3Your wings are waterlogged and too heavy to fly"))
        )
    }
}