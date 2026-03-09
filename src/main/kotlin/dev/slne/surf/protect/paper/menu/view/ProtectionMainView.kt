package dev.slne.surf.protect.paper.menu.view

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.protect.paper.menu.util.closeItem
import dev.slne.surf.protect.paper.menu.util.outlineItem
import dev.slne.surf.protect.paper.menu.util.playGeneralClickSound
import dev.slne.surf.protect.paper.menu.util.protectColored
import dev.slne.surf.protect.paper.menu.view.list.ProtectionListView
import dev.slne.surf.protect.paper.plugin
import dev.slne.surf.protect.paper.region.ProtectionRegion
import dev.slne.surf.protect.paper.user.ProtectionUser
import dev.slne.surf.protect.paper.util.hasBorderCrossingMessagesEnabled
import dev.slne.surf.protect.paper.util.setBorderCrossingMessagesEnabled
import dev.slne.surf.surfapi.bukkit.api.builder.buildLore
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.titleBuilder
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.inventory.ItemType

@Suppress("UnstableApiUsage")
object ProtectionMainView : View() {
    override fun onInit(config: ViewConfigBuilder) {
        config
            .titleBuilder {
                protectColored("Grundstücke".toSmallCaps(), TextDecoration.BOLD)
            }
            .size(5)
            .layout(
                "OOOO?OOOO",
                "O       O",
                "O LV CP O",
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
        render.layoutSlot('C', createItem).onClick { click ->
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

                click.openForPlayer(ProtectionMainView::class.java)
            }
        }
        render.layoutSlot('P', plotMessagesItem)
    }

    private val protectListItem = ItemType.DIRT.createItemStack().apply {
        displayName {
            primary("Meine Grundstücke")
        }

        buildLore {
            emptyLine()
            line {
                spacer("Eine Liste mit allen deinen Grundstücken")
            }
            emptyLine()
        }
    }

    private val visualizeItem = ItemType.ENDER_EYE.createItemStack().apply {
        displayName {
            primary("Visualizer")
        }

        buildLore {
            emptyLine()
            line {
                spacer("Aktiviert/Deaktiviert den Visualizer")
            }
            emptyLine()
        }
    }

    private val createItem = ItemType.GRASS_BLOCK.createItemStack().apply {
        displayName {
            primary("Grundstück erstellen")
        }

        buildLore {
            emptyLine()
            line {
                spacer("Erstelle ein neues Grundstück")
            }
            emptyLine()
        }
    }

    private val plotMessagesItem = ItemType.LEATHER_BOOTS.createItemStack().apply {
        displayName {
            primary("Grundstück Nachrichten")
        }

        buildLore {
            emptyLine()
            line {
                spacer("Aktiviert/Deaktiviert die Nachrichten")
            }
            line {
                spacer("beim Betreten/Verlassen eines Grundstücks")
            }
            emptyLine()
        }
    }
}