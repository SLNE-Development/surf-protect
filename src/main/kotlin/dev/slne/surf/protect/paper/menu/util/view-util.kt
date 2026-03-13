package dev.slne.surf.protect.paper.menu.util

import dev.slne.surf.protect.paper.region.info.RegionInfo
import dev.slne.surf.protect.paper.util.formatString
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import dev.slne.surf.surfapi.bukkit.api.builder.buildLore
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.adventure.playSound
import dev.slne.surf.surfapi.core.api.messages.builder.SurfComponentBuilder
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.context.Context
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ItemType
import kotlin.math.roundToInt

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
fun createRegionItem(protection: RegionInfo) = ItemType.DIRT.createItemStack().apply {
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
            variableValue(protection.owners.joinToString(", ") { it.displayName })
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
            variableValue("${protection.volume} Blöcke".toSmallCaps())
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
            white("Bezahlter Preis: ".toSmallCaps())
            variableValue("${protection.price.roundToInt()}CC".toSmallCaps())
        }
        line {
            appendBlob()
            appendSpace()
            white("Verkaufspreis: ".toSmallCaps())
            variableValue("${protection.retailPrice.roundToInt()}CC".toSmallCaps())
        }
        emptyLine()
        line {
            spacer("Klicke für mehr Informationen".toSmallCaps())
        }
    }
}

fun SurfComponentBuilder.appendBlob() = darkSpacer("▪")

fun SurfComponentBuilder.protectColored(text: Any, vararg decoration: TextDecoration) =
    coloredComponent(text.toString(), TextColor.color(224, 89, 11), *decoration)