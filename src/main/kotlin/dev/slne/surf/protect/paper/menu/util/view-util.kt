package dev.slne.surf.protect.paper.menu.util

import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.core.messages.builder.SurfComponentBuilder
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.protect.paper.region.info.RegionInfo
import dev.slne.surf.protect.paper.util.blockFormat
import dev.slne.surf.protect.paper.util.castCoinFormat
import dev.slne.surf.protect.paper.util.formatString
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.context.Context
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ItemType

val View.outlineItem: ItemStack
    get() = buildItem(Material.GRAY_STAINED_GLASS_PANE) {
        displayName {
            spacer("")
        }
    }

fun Context.playGeneralClickSound() {
    player.playSound(true) {
        type(Sound.UI_BUTTON_CLICK)
    }
}

fun Context.playNewPageSound() {
    player.playSound(true) {
        type(Sound.ENTITY_CHICKEN_EGG)
    }
}

fun Context.playNoSound() {
    player.playSound(true) {
        type(Sound.ENTITY_VILLAGER_NO)
    }
}

fun Context.playYesSound() {
    player.playSound(true) {
        type(Sound.ENTITY_VILLAGER_YES)
    }
}

val previousItem = MenuHeads.ARROW_LEFT.clone().apply {
    displayName {
        protectColored("Vorherige Seite".toSmallCaps(), TextDecoration.BOLD)
    }
    buildLore {
        line {
            spacer("Eine Seite zurück")
        }
    }
}

val nextItem = MenuHeads.ARROW_RIGHT.clone().apply {
    displayName {
        protectColored("Nächste Seite".toSmallCaps(), TextDecoration.BOLD)
    }
    buildLore {
        line {
            spacer("Eine Seite weiter".toSmallCaps())
        }
    }
}

val backItem = MenuHeads.CROSS.apply {
    displayName {
        protectColored("Zurück".toSmallCaps(), TextDecoration.BOLD)
    }

    buildLore {
        line {
            spacer("Zum vorherigen Menü zurückkehren".toSmallCaps())
        }
    }
}

val closeItem = MenuHeads.CROSS.apply {
    displayName {
        protectColored("Schließen".toSmallCaps(), TextDecoration.BOLD)
    }

    buildLore {
        line {
            spacer("Das Menü schließen".toSmallCaps())
        }
    }
}

@Suppress("UnstableApiUsage")
fun createRegionItem(protection: RegionInfo, showMoreInfo: Boolean = true) =
    ItemType.DIRT.createItemStack().apply {
        displayName {
            variableValue(protection.name)
        }

        buildLore {
            emptyLine()
            line {
                protectColored("Grundstücksinformation".toSmallCaps(), TextDecoration.BOLD)
            }
            line {
                appendBlob()
                appendSpace()
                white("Besitzer: ".toSmallCaps())
                variableValue(protection.owners.joinToString(", ") { it.name })
            }
            line {
                appendBlob()
                appendSpace()
                white("Id: ".toSmallCaps())
                variableValue(protection.region.id.toSmallCaps())
            }
            line {
                appendBlob()
                appendSpace()
                white("Fläche: ".toSmallCaps())
                variableValue("${blockFormat.format(protection.volume)} Blöcke".toSmallCaps())
            }

            line {
                appendBlob()
                appendSpace()
                white("Mittelpunkt: ".toSmallCaps())
                variableValue(protection.centerLocation.formatString())
            }
            line {
                appendBlob()
                appendSpace()
                white("Mitglieder: ".toSmallCaps())
                variableValue(protection.members.size)
            }
            line {
                appendBlob()
                appendSpace()
                white("Grundstückswert: ".toSmallCaps())
                variableValue(castCoinFormat.format(protection.price))
            }
            line {
                appendBlob()
                appendSpace()
                white("Verkaufspreis: ".toSmallCaps())
                variableValue(castCoinFormat.format(protection.retailPrice))
            }

            if (showMoreInfo) {
                emptyLine()
                line {
                    spacer("Klicke für mehr Informationen".toSmallCaps())
                }
            }
        }
    }

fun SurfComponentBuilder.appendBlob() = darkSpacer("▪")

fun SurfComponentBuilder.protectColored(text: Any, vararg decoration: TextDecoration) =
    coloredComponent(text.toString(), TextColor.color(224, 89, 11), *decoration)