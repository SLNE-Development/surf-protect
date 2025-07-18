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

/**
 * The Messages object serves as a container for various predefined message and text utilities.
 * It provides categorized sub-objects to handle specific message contexts, such as commands,
 * border crossing notifications, and region protection feedback. Each sub-object and its members
 * focus on delivering localized, structured, and dynamically generated feedback.
 */
object Messages {

    /**
     * Represents a collection of commands for interfacing with and managing regions or performing administrative actions.
     * The commands are grouped into subcommands, each handling a distinct functionality.
     */
    object Command {
        /**
         * The PWho object provides functionality for displaying information about a player's
         * current location in relation to defined protected regions. This includes rendering
         * detailed information about a region, such as its name, owners, members, and other metadata.
         */
        object PWho {
            /**
             * Message string indicating that the player is currently not standing in any protected region
             * defined by other players.
             *
             * This message is typically used when a command or action requires a player to be located in a
             * defined protected region, but there are no such regions at the player's current location.
             */
            const val NO_PLAYER_DEFINED_REGION = "Du stehst in keiner von einem Spieler gesicherten Region."

            /**
             * Renders detailed information about a given region and its associated metadata.
             *
             * This method formats information such as the region's name, ID, owners, and members,
             * appending the details to a text representation.
             *
             * @param info An instance of [RegionInfo] containing the region's metadata and associated attributes
             * such as owners, members, and the protection flag info.
             */
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

        /**
         * MigrateFlags is a utility object that provides predefined messages
         * related to the migration process of region flags. These messages
         * are primarily used to inform and guide users during the migration
         * process.
         */
        object MigrateFlags {
            /**
             * Represents a message indicating the initiation of the flag migration process.
             * Built dynamically to include context-specific information.
             *
             * This message is displayed to notify users that the flag update process has started
             * and guides them to wait for its completion.
             */
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

    /**
     * The `Protecting` object serves as a collection of predefined messages and utility
     * methods used during*/
    object Protecting {
        /**
         * Represents a predefined error message indicating that the marked area overlaps
         * with another region or property during protection region creation or modification.
         */
        val overlappingRegions = buildText {
            appendPrefix()
            error("Die markierte Fläche kollidiert mit einem anderen Grundstück.")
        }

        /**
         * Represents a localized error message indicating that the marked area is too small.
         *
         * This property contains a prebuilt text message intended to be displayed to*/
        val areaTooSmall = buildText {
            appendPrefix()
            error("Die markierte Fläche ist zu klein.")
        }

        /**
         * This constant represents a predefined error message that is displayed when the area
         * defined for protection exceeds the allowable size as per configuration constraints.
         *
         * It is used to*/
        val areaTooBig = buildText {
            appendPrefix()
            error("Die markierte Fläche ist zu groß.")
        }

        /**
         * Represents a localized message indicating that the player does not have sufficient funds
         * to purchase the property or region they are attempting to acquire.
         *
         * The message is*/
        val tooExpensiveToBuy = buildText {
            appendPrefix()
            error("Du hast nicht genügend Geld um dieses Grundstück zu kaufen.")
        }

        /**
         * A text message indicating that a property has been successfully created.
         * The message includes a localized success notification.
         */
        val createdSuccessfully = buildText {
            appendPrefix()
            success("Dein Grundstück wurde erfolgreich erstellt.")
        }

        /**
         * Represents a predefined message indicating that no teleportation point was found
         * for the associated protection region.
         *
         * This variable is used when a teleportation location (e.g., `Flags.TELE_LOC`) cannot
         * be determined or is missing in the provided temporary region. It provides feedback
         * to the protectionUser regarding the absence of a teleportation point during
         * region setup or finalization processes.
         */
        val noTpPointFound = buildText {
            appendPrefix()
            error("Es wurde kein Teleportationspunkt für dieses Grundstück gefunden.")
        }

        /**
         * A predefined message displayed to indicate that another transaction is already being processed.
         *
         * This value is dynamically constructed to include an appropriate prefix and an error message
         * that asks the user to wait for a moment. It is designed to provide immediate feedback when
         * a concurrent transaction processing attempt occurs.
         *
         * Typically used in contexts where transactions are being managed in an asynchronous or synchronous
         * manner, ensuring a clear indication to prevent overlapping or redundant operations.
         */
        val alreadyProcessingTransaction = buildText {
            appendPrefix()
            error("Bitte gedulde dich einen Moment.")
        }

        /**
         * Generates a message indicating how many more markers need to be placed to meet the minimum required amount.
         *
         * @param placedMarkers The number of markers that have already been placed.
         */
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

        /**
         * Builds a message displaying details about a property region offered for sale,
         * including area, pricing information, currency, and distance from spawn.
         *
         * @param area the size of the property in square blocks
         * @param effectiveCost the total calculated cost of the property
         * @param currency the currency in which the property is priced
         * @param pricePerBlock the price per individual block of the property
         * @param distanceToSpawn the distance of the property from the spawn location
         */
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
                variableValue(" Blöcke²")
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