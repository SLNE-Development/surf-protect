package dev.slne.protect.bukkit.book;

import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.region.settings.ProtectionSettings;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.List;

public class ProtectionBook {

    private final Book book;

    /**
     * Creates a new protection book
     */
    public ProtectionBook() {
        List<Component> pages = new ArrayList<>();

        TextComponent.Builder pageOne = Component.text();
        pageOne.append(Component.text("Wenn du den ProtectionMode betrittst, erhältst du vorübergehend Fly um dein " +
                "Grundstück besser definieren zu können.", NamedTextColor.GRAY));
        pageOne.append(Component.newline());
        pageOne.append(Component.newline());
        pageOne.append(Component.text("Du definierst dein Grundstück indem du bis zu ", NamedTextColor.GRAY));
        pageOne.append(Component.text(ProtectionSettings.MARKERS, MessageManager.VARIABLE_VALUE));
        pageOne.append(Component.text(" Marker platzierst und anschließend mit dem grünen Block bestätigst.",
                NamedTextColor.GRAY));
        pageOne.append(Component.text("Mit dem roten Block kannst du die Protection jederzeit abbrechen und zu deinem" +
                " Ausgangspunkt zurückkehren."));
        pages.add(pageOne.build());

        this.book =
                Book.builder().pages(pages).author(Component.text("SLNE Dev Team", MessageManager.PRIMARY))
                        .title(Component.text("Protection System", MessageManager.PRIMARY)).build();
    }

    /**
     * Gets the book
     *
     * @return the book
     */
    public Book getBook() {
        return book;
    }
}
