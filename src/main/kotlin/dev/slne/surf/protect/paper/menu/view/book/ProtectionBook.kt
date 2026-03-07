package dev.slne.surf.protect.paper.menu.view.book

import dev.slne.surf.protect.paper.config.config
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import net.kyori.adventure.inventory.Book

object ProtectionBook {
    fun create() = Book.book(
        buildText {
            primary("Protection System")
        },
        buildText {
            primary("SLNE DEVELOPMENT TEAM")
        },
        buildText {
            listOf(
                buildText {
                    primary("Willkommen im Protection System!")
                    appendNewline()
                    appendNewline()
                    darkSpacer("Wenn du den ProtectionMode betrittst, erhältst du vorübergehend Fly um dein Grundstück besser definieren zu können.")
                },
                buildText {
                    darkSpacer("Du definierst dein Grundstück indem du bis zu ")
                    variableValue(config.markers.amount)
                    darkSpacer(" Marker platzierst und anschließend mit dem grünen Block bestätigst.")
                    appendNewline()
                    darkSpacer("Mit dem roten Block kannst du die Protection jederzeit abbrechen und zu deinem Ausgangspunkt zurückkehren.")
                }
            )
        }
    )
}