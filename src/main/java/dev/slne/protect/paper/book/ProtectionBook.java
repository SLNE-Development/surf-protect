package dev.slne.protect.paper.book;

import dev.slne.protect.paper.message.MessageManager;
import dev.slne.protect.paper.region.settings.ProtectionSettings;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import java.util.ArrayList;
import java.util.List;

public class ProtectionBook {

    private final Book book;

    /**
     * Creates a new protection book
     */
    public ProtectionBook() {
        final List<Component> pages = new ArrayList<>();
        final TextComponent.Builder pageOne = Component.text();
        final TextComponent.Builder pageTwo = Component.text();

        final Component titel = Component.text("Protection System", MessageManager.PRIMARY);

        pageOne.append(Component.text("Willkommen im Protection System!", MessageManager.PRIMARY));
        pageOne.append(Component.newline());
        pageOne.append(Component.newline());
        pageOne.append(Component.text("Wenn du den ProtectionMode betrittst, erhältst du vorübergehend Fly um dein " +
                "Grundstück besser definieren zu können.", MessageManager.BLACK));

        pageTwo.append(Component.text("Du definierst dein Grundstück indem du bis zu ", MessageManager.BLACK));
        pageTwo.append(Component.text(ProtectionSettings.MARKERS, MessageManager.VARIABLE_VALUE));
        pageTwo.append(Component.text(" Marker platzierst und anschließend mit dem grünen Block bestätigst.",
                MessageManager.BLACK));
        pageTwo.append(Component.newline());
        pageTwo.append(Component.text("Mit dem roten Block kannst du die Protection jederzeit abbrechen und zu deinem" +
                " Ausgangspunkt zurückkehren."));

        pages.add(pageOne.build());
        pages.add(pageTwo.build());

        final Component author = Component.text("SLNE Dev Team", MessageManager.PRIMARY);

       this.book = Book.book(titel, author, pages);
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
