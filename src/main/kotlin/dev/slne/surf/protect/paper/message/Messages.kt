package dev.slne.surf.protect.paper.message

import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.Colors
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.builder.SurfComponentBuilder
import dev.slne.surf.api.paper.inventory.framework.view.util.shift
import dev.slne.surf.protect.paper.config
import dev.slne.surf.protect.paper.menu.util.appendBlob
import dev.slne.surf.protect.paper.menu.util.protectColored
import dev.slne.surf.protect.paper.region.info.RegionInfo
import dev.slne.surf.protect.paper.region.structure.StructureSurcharge
import dev.slne.surf.protect.paper.util.blockFormat
import dev.slne.surf.protect.paper.util.castCoinFormat
import dev.slne.surf.transaction.api.currency.Currency
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
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

                appendInfoPrefix()
                info("Du befindest dich aktuell in der Region ")
                if (name != id) {
                    variableValue("$name ($id)")
                } else {
                    variableValue(id)
                }
                info(".")

                val owners = info.owners
                if (owners.isNotEmpty()) {
                    appendNewInfoPrefixedLine()
                    variableKey("Eigentümer: ")
                    appendCollection(owners) {
                        Component.text(it.name ?: "#Unbekannt", Colors.VARIABLE_VALUE)
                    }
                }

                val members = info.members
                if (members.isNotEmpty()) {
                    appendNewInfoPrefixedLine()
                    variableKey("Mitglieder: ")
                    appendCollection(members) {
                        Component.text(it.name ?: "#Unbekannt", Colors.VARIABLE_VALUE)
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
                appendInfoPrefix()
                info("Updating flags, please wait...")
            }

            val completed = buildText {
                appendSuccessPrefix()
                success("Flags have been successfully updated.")
            }
        }
    }

    object BorderCrossing {
        fun borderCrossing(
            regionName: String,
            entered: Boolean
        ) = buildText {
            appendInfoPrefix()
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
            appendErrorPrefix()
            error("Die markierte Fläche kollidiert mit einem anderen Grundstück.")
        }

        /**
         * Represents a localized error message indicating that the marked area is too small.
         *
         * This property contains a prebuilt text message intended to be displayed to*/
        val areaTooSmall = buildText {
            appendErrorPrefix()
            error("Die markierte Fläche ist zu klein.")
        }

        /**
         * This constant represents a predefined error message that is displayed when the area
         * defined for protection exceeds the allowable size as per configuration constraints.
         *
         * It is used to*/
        val areaTooBig = buildText {
            appendErrorPrefix()
            error("Die markierte Fläche ist zu groß.")
        }

        /**
         * Represents a localized message indicating that the player does not have sufficient funds
         * to purchase the property or region they are attempting to acquire.
         *
         * The message is*/
        val tooExpensiveToBuy = buildText {
            appendErrorPrefix()
            error("Du hast nicht genügend Geld um dieses Grundstück zu kaufen.")
        }

        /**
         * A text message indicating that a property has been successfully created.
         * The message includes a localized success notification.
         */
        val createdSuccessfully = buildText {
            appendSuccessPrefix()
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
            appendErrorPrefix()
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
            appendErrorPrefix()
            error("Bitte gedulde dich einen Moment.")
        }

        /**
         * Sent when the price was recalculated before buying and differs from the last offer.
         */
        val offerChanged = buildText {
            appendInfoPrefix()
            info("Der Preis hat sich geändert. Bitte bestätige den Kauf erneut.")
        }

        /**
         * Sent when the offer could not be calculated, e.g. because a chunk could not be loaded.
         */
        val offerCalculationFailed = buildText {
            appendErrorPrefix()
            error("Der Preis konnte nicht berechnet werden. Bitte versuche es erneut.")
        }

        val canOnlyProtectAboveNetherRoofInNether = buildText {
            appendErrorPrefix()
            error("Du kannst nur auf der Netherdecke ein Grundstück erstellen.")
        }

        /**
         * Generates a message indicating how many more markers need to be placed to meet the minimum required amount.
         *
         * @param placedMarkers The number of markers that have already been placed.
         */
        fun moreMarkers(placedMarkers: Int) = buildText {
            val missingMarkers = config.markers.minAmount - placedMarkers

            appendErrorPrefix()
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
         * @param structureSurcharge the surcharge for structures inside the property
         * @param chargedArea the area that has to be paid for, which is smaller than [area] when expanding
         */
        fun offer(
            area: Long,
            effectiveCost: Double,
            currency: Currency,
            pricePerBlock: Double,
            distanceToSpawn: Double,
            discountFactor: Double = 1.0,
            structureSurcharge: StructureSurcharge = StructureSurcharge.NONE,
            chargedArea: Long = area
        ) = buildText {
            val isExpanding = chargedArea != area
            val areaCost = chargedArea * pricePerBlock

            appendInfoPrefix()
            appendNewInfoPrefixedLine()
            success("Das Grundstück steht zum Verkauf!")

            appendNewInfoPrefixedLine()
            appendNewInfoPrefixedLine()
            variableKey("Fläche: ")
            variableValue("${blockFormat.format(area)} Blöcke²")
            if (isExpanding) {
                spacer(" (+${blockFormat.format(chargedArea)} Blöcke²)")
            }

            val hasDiscount = discountFactor < 1.0
            val showPremiumHint = !hasDiscount && config.pricing.discountPercent > 0

            appendNewInfoPrefixedLine()
            appendNewInfoPrefixedLine()
            variableKey("Gesamtkosten: ")
            variableValue(castCoinFormat.format(effectiveCost))

            appendNewInfoPrefixedLine()
            append(buildText {
                appendTreeBranch(isLast = false)
                variableKey("Grundpreis: ")
                variableValue(castCoinFormat.format(areaCost))
                appendHoverHint()

                hoverEvent(buildText {
                    appendHoverTitle("Grundpreis")
                    appendHoverRow("Entfernung zum Spawn", "${blockFormat.format(distanceToSpawn)} Blöcke")
                    appendHoverRow("Preis pro Block", castCoinFormat.format(pricePerBlock))
                    appendHoverRow(
                        if (isExpanding) "Neue Fläche" else "Fläche",
                        "${blockFormat.format(chargedArea)} Blöcke²"
                    )
                    appendHoverRow("Rechnung", calculation(chargedArea, pricePerBlock, areaCost))

                    if (isExpanding) {
                        appendNewline()
                        appendNewline()
                        spacer("Bei einer Erweiterung zahlst du nur die neue Fläche.".toSmallCaps())
                    }
                })
            })

            appendNewInfoPrefixedLine()
            append(buildText {
                appendTreeBranch(isLast = !hasDiscount && !showPremiumHint)
                variableKey("Strukturzuschlag: ")
                variableValue(castCoinFormat.format(structureSurcharge.totalCost))
                appendHoverHint()

                hoverEvent(buildText {
                    appendHoverTitle("Strukturzuschlag")

                    if (structureSurcharge.parts.isEmpty()) {
                        appendNewline()
                        spacer("Im Grundstück liegt keine Struktur mit Zuschlag.".toSmallCaps())
                        return@buildText
                    }

                    for (part in structureSurcharge.parts) {
                        appendHoverRow(part.displayName, calculation(part.blocks, part.pricePerBlock, part.cost))
                    }
                    appendNewline()
                    appendNewline()
                    warning("Den Strukturzuschlag erhältst du beim Verkauf nicht zurück.".toSmallCaps())
                })
            })

            if (hasDiscount) {
                val originalCost = areaCost + structureSurcharge.totalCost
                val discountPercent = ((1.0 - discountFactor) * 100).roundToInt()

                appendNewInfoPrefixedLine()
                appendTreeBranch(isLast = true)
                variableKey("Premium-Rabatt (${discountPercent}%): ")
                success("-${castCoinFormat.format(originalCost - effectiveCost)}")
            }

            if (showPremiumHint) {
                val originalCost = areaCost + structureSurcharge.totalCost
                val premiumCost = (originalCost * config.pricing.discountModifier).roundToInt()

                appendNewInfoPrefixedLine()
                append(buildText {
                    appendTreeBranch(isLast = true)
                    spacer("Mit ")
                    append(config.pricing.premiumPrefixComponent)
                    spacer(
                        "${shift(HALF_SPACE_WIDTH)}sparst du ${castCoinFormat.format(effectiveCost - premiumCost)} " +
                                "(${config.pricing.discountPercent}%)"
                    )
                    appendHoverHint()
                    clickOpensUrl(config.pricing.premiumShopUrl)

                    hoverEvent(buildText {
                        appendHoverTitle("Premium")
                        appendNewline()
                        spacer("Klicke, um zum Shop zu gelangen.".toSmallCaps())
                    })
                })
            }

            appendNewInfoPrefixedLine()
            appendNewInfoPrefixedLine()
            info("Bestätige den Kauf mit dem Knopf in deiner Hotbar.")

        }
    }

    /**
     * Appends a small icon to show players that the text has a hover.
     */
    private fun SurfComponentBuilder.appendHoverHint() = info(" ℹ")

    /** Half the width of a space in pixels, used after the premium prefix which already ends with some spacing. */
    private const val HALF_SPACE_WIDTH = 2

    /**
     * Appends a tree branch to show that the line is part of the line above.
     */
    private fun SurfComponentBuilder.appendTreeBranch(isLast: Boolean) =
        spacer(if (isLast) "└ " else "├ ")

    /**
     * Appends the title of a hover, styled like the region information lore.
     */
    private fun SurfComponentBuilder.appendHoverTitle(title: String) =
        protectColored(title.toSmallCaps(), TextDecoration.BOLD)

    /**
     * Appends a `▪ Key: Value` row to a hover, styled like the region information lore.
     */
    private fun SurfComponentBuilder.appendHoverRow(key: String, value: String) {
        appendNewline()
        appendBlob()
        appendSpace()
        white("$key: ".toSmallCaps())
        variableValue(value)
    }

    /**
     * Formats a price calculation like `441 Blöcke² × 20 CC = 8.820 CC`.
     */
    private fun calculation(blocks: Long, pricePerBlock: Double, cost: Double) =
        "${blockFormat.format(blocks)} Blöcke² × ${castCoinFormat.format(pricePerBlock)} = ${castCoinFormat.format(cost)}"
}