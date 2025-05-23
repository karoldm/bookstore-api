package com.karoldm.bookstore.mocks;

import com.karoldm.bookstore.entities.Book;
import com.karoldm.bookstore.entities.Store;

import java.time.LocalDate;
import java.util.List;

abstract public class BooksMock {
    public static Store store = Store.builder()
            .banner(null)
            .slogan("The best fantasy library")
            .name("Fantasy and more")
            .id(1L)
            .build();

    public static List<Book> books = List.of(
            Book.builder().createdAt(LocalDate.of(2025, 4, 17)).title("Good Omens").summary("A comedy about the apocalypse.").id(1L).author("Neil Gaiman and Terry Pratchett").cover(null).available(true).rating(5).releasedAt(LocalDate.of(1990, 5, 10)).store(store).build(),
            Book.builder().createdAt(LocalDate.of(2025, 4, 5)).title("The Name of the Wind").summary("Story of Kvothe's adventures.").id(2L).author("Patrick Rothfuss").cover(null).available(true).rating(5).releasedAt(LocalDate.of(2007, 3, 27)).store(store).build(),
            Book.builder().createdAt(LocalDate.of(2025, 4, 14)).title("The Dark Tower: The Gunslinger").summary("First book of The Dark Tower series.").id(3L).author("Stephen King").cover(null).available(true).rating(4).releasedAt(LocalDate.of(1982, 6, 10)).store(store).build(),
            Book.builder().createdAt(LocalDate.of(2025, 4, 20)).title("The Blade Itself").summary("A gritty fantasy novel.").id(4L).author("Joe Abercrombie").cover(null).available(true).rating(5).releasedAt(LocalDate.of(2006, 5, 4)).store(store).build(),
            Book.builder().createdAt(LocalDate.of(2025, 4, 8)).title("The Way of Kings").summary("Epic fantasy by Brandon Sanderson.").id(5L).author("Brandon Sanderson").cover(null).available(false).rating(5).releasedAt(LocalDate.of(2010, 8, 31)).store(store).build(),
            Book.builder().createdAt(LocalDate.of(2025, 4, 13)).title("The Sword of Shannara").summary("Fantasy novel inspired by Tolkien.").id(6L).author("Terry Brooks").cover(null).available(true).rating(3).releasedAt(LocalDate.of(1977, 3, 10)).store(store).build(),
            Book.builder().createdAt(LocalDate.of(2025, 4, 7)).title("Eragon").summary("A farm boy finds a dragon egg.").id(7L).author("Christopher Paolini").cover(null).available(true).rating(4).releasedAt(LocalDate.of(2002, 6, 25)).store(store).build(),
            Book.builder().createdAt(LocalDate.of(2025, 4, 12)).title("Dune").summary("Sci-fi novel about politics and power.").id(8L).author("Frank Herbert").cover(null).available(true).rating(5).releasedAt(LocalDate.of(1965, 8, 1)).store(store).build(),
            Book.builder().createdAt(LocalDate.of(2025, 4, 10)).title("Percy Jackson: The Lightning Thief").summary("A boy discovers he is a demigod.").id(9L).author("Rick Riordan").cover(null).available(true).rating(4).releasedAt(LocalDate.of(2005, 6, 28)).store(store).build(),
            Book.builder().createdAt(LocalDate.of(2025, 4, 16)).title("His Dark Materials: The Golden Compass").summary("A young girl's adventure.").id(10L).author("Philip Pullman").cover(null).available(true).rating(4).releasedAt(LocalDate.of(1995, 7, 15)).store(store).build(),
            Book.builder().createdAt(LocalDate.of(2025, 4, 19)).title("The Eye of the World").summary("First book of The Wheel of Time.").id(11L).author("Robert Jordan").cover(null).available(true).rating(4).releasedAt(LocalDate.of(1990, 1, 15)).store(store).build(),
            Book.builder().createdAt(LocalDate.of(2025, 4, 6)).title("Mistborn: The Final Empire").summary("Fantasy novel about a world of ash and mist.").id(12L).author("Brandon Sanderson").cover(null).available(true).rating(5).releasedAt(LocalDate.of(2006, 7, 17)).store(store).build(),
            Book.builder().createdAt(LocalDate.of(2025, 4, 3)).title("Harry Potter and the Sorcerer's Stone").summary("A novel about a young wizard.").id(13L).author("J.K. Rowling").cover(null).available(true).rating(5).releasedAt(LocalDate.of(1997, 6, 26)).store(store).build(),
            Book.builder().createdAt(LocalDate.of(2025, 4, 9)).title("The Last Wish").summary("Stories of Geralt the Witcher.").id(14L).author("Andrzej Sapkowski").cover(null).available(true).rating(5).releasedAt(LocalDate.of(1993, 6, 10)).store(store).build(),
            Book.builder().createdAt(LocalDate.of(2025, 4, 1)).title("The Chronicles of Narnia").summary("The Chronicles of Narnia is a fantasy series produced by Walden Media and distributed by Disney and Fox.").id(15L).author("C. S. Lewis").cover(null).available(true).rating(5).releasedAt(LocalDate.of(1949, 3, 31)).store(store).build(),
            Book.builder().createdAt(LocalDate.of(2025, 4, 15)).title("American Gods").summary("A novel about gods living among us.").id(16L).author("Neil Gaiman").cover(null).available(true).rating(5).releasedAt(LocalDate.of(2001, 6, 19)).store(store).build(),
            Book.builder().createdAt(LocalDate.of(2025, 4, 2)).title("The Hobbit").summary("A fantasy novel by J.R.R. Tolkien.").id(17L).author("J.R.R. Tolkien").cover(null).available(true).rating(5).releasedAt(LocalDate.of(1937, 9, 21)).store(store).build(),
            Book.builder().createdAt(LocalDate.of(2025, 4, 18)).title("The Black Prism").summary("Epic fantasy by Brent Weeks.").id(18L).author("Brent Weeks").cover(null).available(false).rating(4).releasedAt(LocalDate.of(2010, 8, 25)).store(store).build(),
            Book.builder().createdAt(LocalDate.of(2025, 4, 11)).title("The Silmarillion").summary("The mythopoeic stories by Tolkien.").id(19L).author("J.R.R. Tolkien").cover(null).available(false).rating(4).releasedAt(LocalDate.of(1977, 9, 15)).store(store).build(),
            Book.builder().createdAt(LocalDate.of(2025, 4, 4)).title("Game of Thrones").summary("A fantasy novel by George R.R. Martin.").id(20L).author("George R.R. Martin").cover(null).available(false).rating(4).releasedAt(LocalDate.of(1996, 8, 6)).store(store).build(),
            Book.builder().createdAt(LocalDate.of(2025, 4, 21)).title("An Ember in the Ashes").summary("A fantasy novel inspired by Ancient Rome.").id(21L).author("Sabaa Tahir").cover(null).available(true).rating(4).releasedAt(LocalDate.of(2015, 4, 28)).store(store).build()
    );
}