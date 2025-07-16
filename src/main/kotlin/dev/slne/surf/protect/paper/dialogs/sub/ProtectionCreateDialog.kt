@file:Suppress("UnstableApiUsage")

package dev.slne.surf.protect.paper.dialogs.sub

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.protect.paper.config.config
import dev.slne.surf.protect.paper.dialogs.ProtectionMainDialog
import dev.slne.surf.protect.paper.plugin
import dev.slne.surf.protect.paper.region.ProtectionRegion
import dev.slne.surf.protect.paper.region.settings.ProtectionSettings
import dev.slne.surf.protect.paper.user.ProtectionUser
import dev.slne.surf.surfapi.bukkit.api.dialog.*
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.core.api.messages.Colors
import dev.slne.surf.surfapi.core.api.messages.adventure.appendNewline
import dev.slne.surf.surfapi.core.api.messages.adventure.clickCopiesToClipboard
import dev.slne.surf.surfapi.core.api.messages.adventure.clickOpensUrl
import io.papermc.paper.event.player.PlayerCustomClickEvent
import io.papermc.paper.registry.data.dialog.DialogBase
import net.kyori.adventure.text.Component
import org.bukkit.OfflinePlayer

object ProtectionCreateDialog {

    fun protectionCreateDialog(target: OfflinePlayer) = dialog {
        base {
            title { primary("Protections — Grundstück erstellen") }
            afterAction(DialogBase.DialogAfterAction.NONE)
            preventClosingWithEscape()
            body {
                plainMessage(400) {
                    primary("Willkommen im Protection System!")
                    appendNewline(2)
                    info("Sobald du den Protection-Mode aktivierst, erhältst du vorübergehend die Fähigkeit zu fliegen.")
                    appendSpace()
                    info("So kannst du dein Grundstück aus der Luft besser überblicken und bequem abstecken.")
                    appendSpace()
                    info("Setze bis zu ")
                    variableValue(config.markers.amountWritten)
                    info(" Markierungen, um die gewünschte Fläche zu definieren, ")
                    info("und bestätige deine Auswahl mit dem grünen Block.")
                    appendSpace()
                    info("Entscheidest du dich doch um, kannst du den Vorgang jederzeit ")
                    info("mit dem roten Block abbrechen und kehrst automatisch zu deinem Ausgangspunkt zurück.")
                }
                plainMessage(400) {
                    info("Weitere Informationen zum Protection-Mode und zu allen Funktionen ")
                    info("rund um Grundstücke findest du in unserer Dokumentation: ")
                    appendNewline()
                    appendNewline {
                        variableValue("https://server.castcrafter.de/plots-homepage.html")
                        clickOpensUrl("https://server.castcrafter.de/plots-homepage.html")
                    }
                }
            }
        }
        type {
            confirmation(createStartButton(), createBackButton(target))
        }
    }

    private fun createStartButton() = actionButton {
        label { text("Grundstück erstellen") }
        action {
            playerCallback { player ->
                plugin.launch(plugin.entityDispatcher(player)) {
                    val user = ProtectionUser.getProtectionUser(player)
                    val regionCreation = ProtectionRegion(user, player, player.inventory.contents)
                    val success =
                        user.startRegionCreation(regionCreation) { createErrorNoticeDialog(it) }
                    if (success) {
                        player.clearDialogs()
                    }
                }
            }
        }
    }

    private fun createErrorNoticeDialog(message: Component) = noticeDialog {
        base {
            title { error("Protections — Fehler") }
            body {
                plainMessage(400) {
                    append(message.colorIfAbsent(Colors.ERROR))
                }
            }
        }
    }


    private fun createBackButton(target: OfflinePlayer) = actionButton {
        label { text("Zurück") }
        action {
            playerCallback { it.showDialog(ProtectionMainDialog.mainDialog(it, target)) }
        }
    }
}