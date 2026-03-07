package dev.slne.surf.protect.paper.menu.view

import dev.slne.surf.protect.paper.dialogs.sub.ProtectionCreateDialog
import dev.slne.surf.protect.paper.menu.util.closeItem
import dev.slne.surf.protect.paper.menu.util.outlineItem
import dev.slne.surf.protect.paper.menu.util.playGeneralClickSound
import dev.slne.surf.protect.paper.menu.util.protectColored
import dev.slne.surf.protect.paper.permission.ProtectPermissionRegistry
import dev.slne.surf.protect.paper.region.visual.visualizer.ProtectionVisualizerManager
import dev.slne.surf.protect.paper.settings.ProtectionUserSettings
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import dev.slne.surf.surfapi.bukkit.api.builder.buildLore
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.titleBuilder
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.inventory.ItemType

@Suppress("UnstableApiUsage")
object ProtectionMainView : View() {
    override fun onInit(config: ViewConfigBuilder) {
        config
            .titleBuilder {
                protectColored("Protection".toSmallCaps(), TextDecoration.BOLD)
            }
            .size(5)
            .layout(
                "OOOOOOOOO",
                "O       O",
                "O LV NP O",
                "O       O",
                "OOOOCOOOO"
            )
            .cancelInteractions()
    }

    override fun onFirstRender(render: RenderContext) {
        render.layoutSlot('O', outlineItem)

        render.layoutSlot('C', closeItem).onClick { click ->
            click.playGeneralClickSound()
            click.closeForPlayer()
        }

        if (render.player.hasPermission(ProtectPermissionRegistry.PROTECTION_LIST_PERMISSION)) {
            render.layoutSlot('L', protectListItem).onClick { click ->
                click.playGeneralClickSound()
                click.openForPlayer(ProtectionListView::class.java)
            }
        } else {
            render.layoutSlot('L', outlineItem)
        }

        if (render.player.hasPermission(ProtectPermissionRegistry.PROTECTION_VISUALIZE_PERMISSION)) {
            render.layoutSlot('V', visualizeItem).onClick { click ->
                click.playGeneralClickSound()
                val state = ProtectionVisualizerManager.switchVisualizing(click.player)
                click.player.sendText {
                    appendInfoPrefix()
                    info("Du hast die Visualisierung der Grundstücke ")
                    if (state) success("aktiviert") else error("deaktiviert")
                    info(". Bitte warte einen kleinen Moment, bis die Änderungen wirksam werden.")
                }
            }
        } else {
            render.layoutSlot('V', outlineItem)
        }

        if (render.player.hasPermission(ProtectPermissionRegistry.PROTECTION_CREATE_PERMISSION)) {
            render.layoutSlot('N', createItem).onClick { click ->
                click.playGeneralClickSound()
                click.closeForPlayer()
                click.player.showDialog(ProtectionCreateDialog.protectionCreateDialog(click.player))
            }
        } else {
            render.layoutSlot('N', outlineItem)
        }

        render.layoutSlot('P').updateOnClick().renderWith {
            plotMessagesItem(ProtectionUserSettings.PLOT_MESSAGES.getValue(render.player))
        }.onClick { click ->
            click.playGeneralClickSound()
            ProtectionUserSettings.PLOT_MESSAGES.toggle(click.player)
        }
    }

    private val protectListItem = buildItem(Material.GRASS_BLOCK) {
        displayName {
            primary("Meine Grundstücke".toSmallCaps(), TextDecoration.BOLD)
        }

        buildLore {
            emptyLine()
            line {
                spacer("Eine Liste mit allen deinen Grundstücken")
            }
            emptyLine()
        }
    }

    private val visualizeItem = buildItem(Material.ENDER_EYE) {
        displayName {
            primary("Visualizer".toSmallCaps(), TextDecoration.BOLD)
        }

        buildLore {
            emptyLine()
            line {
                spacer("Aktiviert/Deaktiviert den Visualizer")
            }
            emptyLine()
        }
    }

    private val createItem = buildItem(Material.DIRT) {
        displayName {
            primary("Grundstück erstellen".toSmallCaps(), TextDecoration.BOLD)
        }

        buildLore {
            emptyLine()
            line {
                spacer("Erstelle ein neues Grundstück")
            }
            emptyLine()
        }
    }

    private fun plotMessagesItem(enabled: Boolean) = ItemType.LEATHER_BOOTS.createItemStack().apply {
        displayName {
            primary("Grundstück Nachrichten".toSmallCaps(), TextDecoration.BOLD)
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
            line {
                spacer("Status: ")
                if (enabled) {
                    success("Aktiviert")
                } else {
                    error("Deaktiviert")
                }
            }
            emptyLine()
        }
    }
}