package dev.slne.surf.protect.paper.menu.view

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.sksamuel.aedile.core.expireAfterWrite
import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.dsl.onItemClick
import dev.slne.surf.api.paper.inventory.framework.dsl.openForPlayer
import dev.slne.surf.api.paper.inventory.framework.dsl.slot
import dev.slne.surf.api.paper.inventory.framework.open
import dev.slne.surf.api.paper.inventory.framework.view.container.dsl.blockRow
import dev.slne.surf.api.paper.inventory.framework.view.containerDefaults
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconColor
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconType
import dev.slne.surf.api.paper.inventory.framework.view.icon.viewIcon
import dev.slne.surf.api.paper.inventory.framework.view.onFirstRender
import dev.slne.surf.api.paper.inventory.framework.view.settings
import dev.slne.surf.api.paper.inventory.framework.view.surfView
import dev.slne.surf.protect.paper.menu.util.playGeneralClickSound
import dev.slne.surf.protect.paper.menu.util.protectColored
import dev.slne.surf.protect.paper.menu.view.list.protectionListView
import dev.slne.surf.protect.paper.plugin
import dev.slne.surf.protect.paper.region.ProtectionRegion
import dev.slne.surf.protect.paper.region.visual.visualizer.ProtectionVisualizerManager
import dev.slne.surf.protect.paper.user.protectionUser
import dev.slne.surf.protect.paper.util.hasBorderCrossingMessagesEnabled
import dev.slne.surf.protect.paper.util.setBorderCrossingMessagesEnabled
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.inventory.ItemType
import java.util.*
import kotlin.time.Duration.Companion.seconds

private val visualizerCooldown = Caffeine.newBuilder()
    .expireAfterWrite(1.seconds)
    .build<UUID, Unit>()

val protectionMainView = surfView("Grundstuecke") {
    settings {
        rows(3)
        navigateBackOnOutsideClick(false)
    }
    containerDefaults {
        blockRow(1)
        blockRow(2, exemptColumns = intArrayOf(2, 3, 5, 6))
        blockRow(3)
    }
    onFirstRender {
        slot(2, 3) {
            withItem(protectListItem)
            onItemClick {
                playGeneralClickSound()
                openForPlayer(protectionListView)
            }
        }
        slot(2, 4) {
            withItem(createItem)
            onItemClick {
                closeForPlayer()

                plugin.launch(plugin.entityDispatcher(player)) {
                    val user = player.protectionUser()
                    val regionCreation = ProtectionRegion(user, player, player.inventory.contents)
                    val success = user.startRegionCreation(regionCreation)

                    if (success) {
                        player.sendText {
                            appendSuccessPrefix()
                            success("Du befindest dich nun im Protection-Mode.")
                        }
                    } else {
                        player.sendText {
                            appendErrorPrefix()
                            error("Es ist ein Fehler aufgetreten. Versuche es später erneut.")
                        }
                    }
                }
            }
        }
        slot(2, 6) {
            withItem(visualizeItem)
            onItemClick {
                playGeneralClickSound()

                val onCooldown = visualizerCooldown.asMap().putIfAbsent(player.uniqueId, Unit) != null
                if (onCooldown) {
                    player.sendText {
                        appendErrorPrefix()
                        error("Bitte warte einen Augenblick.")
                    }
                    return@onItemClick
                }

                val isVisualizing = ProtectionVisualizerManager.switchVisualizing(player)
                player.sendText {
                    appendSuccessPrefix()
                    if (isVisualizing) {
                        success("Du hast den Visualizer aktiviert.")

                    } else {
                        success("Du hast den Visualizer deaktiviert.")
                    }
                }

                closeForPlayer()
            }
        }
        slot(2, 7) {
            withItem(plotMessagesItem)
            onItemClick {
                playGeneralClickSound()

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
    }
}

private val protectListItem = buildItem(ItemType.DIRT) {
    displayName {
        protectColored("Meine Grundstücke".toSmallCaps(), TextDecoration.BOLD)
    }

    buildLore {
        emptyLine()
        line {
            spacer("Eine Liste mit all deinen Grundstücken.".toSmallCaps())
        }
        emptyLine()
    }
}

private val visualizeItem = buildItem(ItemType.ENDER_EYE) {
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

private val createItem = viewIcon(ViewIconType.PLUS, ViewIconColor.GREEN) {
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

private val plotMessagesItem = buildItem(ItemType.LEATHER_BOOTS) {
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