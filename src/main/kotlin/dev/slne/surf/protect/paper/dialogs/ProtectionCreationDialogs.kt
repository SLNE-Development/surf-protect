package dev.slne.surf.protect.paper.dialogs

import dev.slne.surf.surfapi.bukkit.api.dialog.noticeDialogWithBuilder
import dev.slne.surf.surfapi.core.api.messages.Colors
import dev.slne.surf.surfapi.core.api.messages.adventure.text

object ProtectionCreationDialogs {

    fun protectionCreatedNotice() =
        noticeDialogWithBuilder(text("Protections — Grundstück erstellt", Colors.PRIMARY)) {
            success("Dein Grundstück wurde erfolgreich erstellt.")
        }

    fun protectionCancelledNotice() = noticeDialogWithBuilder(text("Protections — Erstellung abgebrochen", Colors.PRIMARY)) {
        error("Du hast die Erstellung deines Grundstücks abgebrochen.")
    }
}