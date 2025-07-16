package dev.slne.surf.protect.paper.message

import dev.slne.surf.protect.paper.config.config
import dev.slne.surf.protect.paper.region.info.RegionInfo
import dev.slne.surf.protect.paper.region.settings.ProtectionSettings
import dev.slne.surf.surfapi.core.api.messages.Colors
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.transaction.api.currency.Currency
import net.kyori.adventure.text.Component
import java.text.NumberFormat
import kotlin.math.roundToInt

object Messages {

    object Command {
        object PWho {
            const val NO_PLAYER_DEFINED_REGION = "Du stehst in keiner von einem Spieler gesicherten Region."

            fun renderInfo(info: RegionInfo) = buildText {
                val name = info.name.ifEmpty { "Unbenannt" }
                val id = info.region.id

                appendPrefix()
                info("Du befindest dich aktuell in der Region ")
                if (name != id) {
                    variableValue("$name ($id)")
                } else {
                    variableValue(id)
                }
                info(".")

                val owners = info.owners
                if (owners.isNotEmpty()) {
                    appendNewPrefixedLine {
                        variableKey("Eigentümer: ")
                        appendCollection(owners) {
                            Component.text(it.name ?: "#Unbekannt", Colors.VARIABLE_VALUE)
                        }
                    }
                }

                val members = info.members
                if (members.isNotEmpty()) {
                    appendNewPrefixedLine {
                        variableKey("Mitglieder: ")
                        appendCollection(members) {
                            Component.text(it.name ?: "#Unbekannt", Colors.VARIABLE_VALUE)
                        }
                    }
                }
            }
        }

        object MigrateFlags {
            val started = buildText {
                appendPrefix()
                info("Updating flags, please wait...")
            }

            val completed = buildText {
                appendPrefix()
                success("Flags have been successfully updated.")
            }
        }
    }

    object BorderCrossing {
        fun borderCrossing(
            regionName: String,
            entered: Boolean
        ) = buildText {
            appendPrefix()
            info("Du hast das Grundstück ")
            variableValue(regionName)
            appendSpace()
            if (entered) {
                info("betreten.")
            } else {
                info("verlassen.")
            }
        }
    }

    object Protecting {
        val overlappingRegions = buildText {
            appendPrefix()
            error("Die markierte Fläche kollidiert mit einem anderen Grundstück.")
        }

        val areaTooSmall = buildText {
            appendPrefix()
            error("Die markierte Fläche ist zu klein.")
        }

        val areaTooBig = buildText {
            appendPrefix()
            error("Die markierte Fläche ist zu groß.")
        }

        val tooExpensiveToBuy = buildText {
            appendPrefix()
            error("Du hast nicht genügend Geld um dieses Grundstück zu kaufen.")
        }

        val createdSuccessfully = buildText {
            appendPrefix()
            success("Dein Grundstück wurde erfolgreich erstellt.")
        }

        val noTpPointFound = buildText {
            appendPrefix()
            error("Es wurde kein Teleportationspunkt für dieses Grundstück gefunden.")
        }

        val alreadyProcessingTransaction = buildText {
            appendPrefix()
            error("Bitte gedulde dich einen Moment.")
        }

        fun moreMarkers(placedMarkers: Int) = buildText {
            val missingMarkers = config.markers.minAmount - placedMarkers

            appendPrefix()
            error("Du musst mindestens ")
            variableValue(missingMarkers)
            if (missingMarkers == 1) {
                error(" weiteren")
            } else {
                error(" weitere")
            }
            error(" Marker platzieren.")
        }

        fun offer(
            area: Long,
            effectiveCost: Double,
            currency: Currency,
            pricePerBlock: Double,
            distanceToSpawn: Double
        ) = buildText {
            val distanceToSpawn = (distanceToSpawn * 100).roundToInt() / 100.0
            val format = NumberFormat.getNumberInstance()

            appendPrefix()
            appendNewPrefixedLine {
                success("Das Grundstück steht zum Verkauf!")
            }
            appendNewPrefixedLine()
            appendNewPrefixedLine {
                variableKey("Fläche: ")
                variableValue(format.format(area))
                variableValue(" Blöcke")
            }
            appendNewPrefixedLine {
                variableKey("Preis pro Block: ")
                variableValue(format.format(pricePerBlock))
                appendSpace()
                append(currency.displayName.colorIfAbsent(Colors.VARIABLE_VALUE))
            }
            appendNewPrefixedLine {
                variableKey("Distanz zum Spawn: ")
                variableValue(format.format(distanceToSpawn))
                variableValue(" Blöcke")
            }
            appendNewPrefixedLine {
                variableKey("Gesamtkosten: ")
                variableValue(format.format(effectiveCost))
                appendSpace()
                append(currency.displayName.colorIfAbsent(Colors.VARIABLE_VALUE))
            }
            appendNewPrefixedLine()
            appendNewPrefixedLine {
                info("Wenn du das Grundstück kaufen möchtest,")
            }
            appendNewPrefixedLine {
                info("nutze den Bestätigungsknopf in deiner Hotbar.")
            }
        }
    }
}