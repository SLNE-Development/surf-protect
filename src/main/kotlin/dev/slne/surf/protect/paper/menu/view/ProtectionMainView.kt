package dev.slne.surf.protect.paper.menu.view

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.sksamuel.aedile.core.expireAfterWrite
import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.protect.paper.menu.util.closeItem
import dev.slne.surf.protect.paper.menu.util.outlineItem
import dev.slne.surf.protect.paper.menu.util.playGeneralClickSound
import dev.slne.surf.protect.paper.menu.util.protectColored
import dev.slne.surf.protect.paper.menu.view.list.ProtectionListView
import dev.slne.surf.protect.paper.plugin
import dev.slne.surf.protect.paper.region.ProtectionRegion
import dev.slne.surf.protect.paper.region.visual.visualizer.ProtectionVisualizerManager
import dev.slne.surf.protect.paper.user.ProtectionUser
import dev.slne.surf.protect.paper.util.hasBorderCrossingMessagesEnabled
import dev.slne.surf.protect.paper.util.setBorderCrossingMessagesEnabled
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.inventory.ItemType
import java.util.*
import kotlin.time.Duration.Companion.seconds

@Suppress("UnstableApiUsage")
object ProtectionMainView : View() {
    private val visualizerCooldown = Caffeine.newBuilder()
        .expireAfterWrite(1.seconds)
        .build<UUID, Boolean>()

    override fun onInit(config: ViewConfigBuilder) {
        config
            .titleBuilder {
                protectColored("Grundstücke".toSmallCaps(), TextDecoration.BOLD)
            }
            .size(5)
            .layout(
                "OOOOOOOOO",
                "O       O",
                "O LA VP O",
                "O       O",
                "OOOOCOOOO"
            )
            .cancelInteractions()
    }

    override fun onFirstRender(render: RenderContext) {
        render.layoutSlot('C', closeItem).onClick { click ->
            click.playGeneralClickSound()
            click.closeForPlayer()
        }

        render.layoutSlot('O', outlineItem)
        render.layoutSlot('L', protectListItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(ProtectionListView::class.java)
        }
        render.layoutSlot('V', visualizeItem).onClick { click ->
            val player = click.player

            val onCooldown = visualizerCooldown.getIfPresent(player.uniqueId) ?: false
            if (onCooldown) {
                player.sendText {
                    appendErrorPrefix()
                    error("Bitte warte einen Augenblick.")
                }
                return@onClick
            }
            visualizerCooldown.put(player.uniqueId, true)

            val isVisualizing = ProtectionVisualizerManager.switchVisualizing(player)
            player.sendText {
                appendSuccessPrefix()
                if (isVisualizing) {
                    success("Du hast den Visualizer aktiviert.")

                } else {
                    success("Du hast den Visualizer deaktiviert.")
                }
            }

            click.closeForPlayer()
        }
        render.layoutSlot('A', createItem).onClick { click ->
            val player = click.player

            click.closeForPlayer()

            plugin.launch(plugin.entityDispatcher(player)) {
                val user = ProtectionUser.getProtectionUser(player)
                val regionCreation = ProtectionRegion(user, player, player.inventory.contents)
                val success = user.startRegionCreation(regionCreation)

                if (success) {
                    player.sendText {
                        appendSuccessPrefix()
                        success("Du befindest dich nun im Protection-Mode. ")
                    }
                } else {
                    player.sendText {
                        appendErrorPrefix()
                        error("Es ist ein Fehler aufgetreten. Versuche es später erneut. ")
                    }
                }
            }
        }
        render.layoutSlot('P', plotMessagesItem).onClick { click ->
            click.playGeneralClickSound()

            val player = click.player
            val oldState = player.hasBorderCrossingMessagesEnabled()
            val newState = !oldState

            player.setBorderCrossingMessagesEnabled(newState)
            player.sendText {
                appendSuccessPrefix()
                success("Du hast die Grundstücks Nachrichten ")
                variableValue(if (newState) "aktiviert" else "deaktiviert")
                success(".")
            }

        }
    }

    private val protectListItem = ItemType.DIRT.createItemStack().apply {
        displayName {
            protectColored("Meine Grundstücke".toSmallCaps(), TextDecoration.BOLD)
        }

        buildLore {
            emptyLine()
            line {
                spacer("Eine Liste mit allen deinen Grundstücken".toSmallCaps())
            }
            emptyLine()
        }
    }

    private val visualizeItem = ItemType.ENDER_EYE.createItemStack().apply {
        displayName {
            protectColored("Visualizer".toSmallCaps(), TextDecoration.BOLD)
        }

        buildLore {
            emptyLine()
            line {
                spacer("Aktiviert/Deaktiviert den Visualizer".toSmallCaps())
            }
            emptyLine()
        }
    }

    private val createItem = ItemType.GRASS_BLOCK.createItemStack().apply {
        displayName {
            protectColored("Grundstück erstellen".toSmallCaps(), TextDecoration.BOLD)
        }

        buildLore {
            emptyLine()
            line {
                spacer("Erstelle ein neues Grundstück".toSmallCaps())
            }
            emptyLine()
        }
    }

    private val plotMessagesItem = ItemType.LEATHER_BOOTS.createItemStack().apply {
        displayName {
            protectColored("Grundstück Nachrichten".toSmallCaps(), TextDecoration.BOLD)
        }

        buildLore {
            emptyLine()
            line {
                spacer("Aktiviert/Deaktiviert die Nachrichten".toSmallCaps())
            }
            line {
                spacer("beim Betreten/Verlassen eines Grundstücks".toSmallCaps())
            }
            emptyLine()
        }
    }
}