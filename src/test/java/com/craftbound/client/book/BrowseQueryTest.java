package com.craftbound.client.book;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class BrowseQueryTest
{
    record Item(String name, BrowseTab tab, String group, boolean unlocked)
    {
        Item(String name, BrowseTab tab)
        {
            this(name, tab, null, true);
        }
    }

    private static final Item LOG = new Item("Oak Log", BrowseTab.BUILDING);
    private static final Item PLANKS = new Item("Oak Planks", BrowseTab.BUILDING);
    private static final Item BOAT = new Item("Oak Boat", BrowseTab.MISC, "boat", true);
    private static final Item CHEST_BOAT = new Item("Oak Chest Boat", BrowseTab.MISC, "boat", true);
    private static final Item STICK = new Item("Stick", BrowseTab.MISC);
    private static final Item AXE = new Item("Iron Axe", BrowseTab.EQUIPMENT);
    private static final Item SECRET = new Item("Secret", BrowseTab.MISC, null, false);

    private record Fake(List<Item> producible, List<Item> bookmarked, Set<String> jeiHits) implements BrowseQuery.Catalog<Item>
    {
        private static final Map<String, Set<String>> MADE_FROM = Map.of(
                "Oak Log", Set.of("Oak Planks"),
                "Oak Planks", Set.of("Stick", "Oak Boat"));

        @Override
        public List<Item> searchable()
        {
            return List.of(LOG, PLANKS, BOAT, CHEST_BOAT, STICK, AXE, SECRET);
        }

        @Override
        public boolean unlocked(Item item)
        {
            return item.unlocked();
        }

        @Override
        public BrowseTab tabOf(Item item)
        {
            return item.tab();
        }

        @Override
        public String unlockKey(Item item)
        {
            return item.name();
        }

        @Override
        public String variantGroup(Item item)
        {
            return item.group();
        }

        @Override
        public boolean named(Item item, String lowerCaseText)
        {
            return item.name().toLowerCase().contains(lowerCaseText);
        }

        @Override
        public Predicate<Item> jeiMatches(String text)
        {
            return item -> jeiHits.contains(item.name());
        }

        @Override
        public Set<String> outputsUsing(Set<String> unlockKeys)
        {
            return unlockKeys.stream()
                    .flatMap(key -> MADE_FROM.getOrDefault(key, Set.of()).stream())
                    .collect(Collectors.toSet());
        }
    }

    private static final Fake CATALOG = new Fake(List.of(LOG, PLANKS, BOAT, CHEST_BOAT, STICK, AXE, SECRET),
            List.of(CHEST_BOAT, BOAT), Set.of("Iron Axe"));

    private static List<List<Item>> entries(BrowseTab tab, String text)
    {
        return new BrowseQuery(tab, text, false).entries(CATALOG, item -> true);
    }

    @Test
    void lockedItemsNeverShow()
    {
        assertEquals(List.of(List.of(LOG), List.of(PLANKS), List.of(BOAT, CHEST_BOAT), List.of(STICK), List.of(AXE)),
                entries(BrowseTab.ALL, ""));
    }

    @Test
    void anItemRibbonShowsOnlyItsOwnItems()
    {
        assertEquals(List.of(List.of(BOAT, CHEST_BOAT), List.of(STICK)), entries(BrowseTab.MISC, ""));
    }

    @Test
    void bookmarksShowOneByOneInTheirOwnOrder()
    {
        assertEquals(List.of(List.of(CHEST_BOAT), List.of(BOAT)), entries(BrowseTab.BOOKMARKS, ""));
    }

    @Test
    void aNameSearchListsMatchesBeforeWhatTheyMake()
    {
        assertEquals(List.of(List.of(LOG), List.of(PLANKS), List.of(BOAT, CHEST_BOAT), List.of(STICK)),
                entries(BrowseTab.ALL, "Oak"));
        assertEquals(List.of(List.of(PLANKS), List.of(STICK), List.of(BOAT)),
                new BrowseQuery(BrowseTab.ALL, "planks", false).entries(
                        new Fake(List.of(STICK, BOAT, PLANKS), List.of(), Set.of()), item -> true));
    }

    @Test
    void usesComeFromEveryIngredientNotOnlyTheListedOnes()
    {
        Fake withoutLogs = new Fake(List.of(PLANKS, STICK), List.of(), Set.of());
        assertEquals(List.of(List.of(PLANKS)),
                new BrowseQuery(BrowseTab.ALL, "log", false).entries(withoutLogs, item -> true));
    }

    @Test
    void searchSyntaxGoesToJeiAndAddsNoUses()
    {
        assertEquals(List.of(List.of(AXE)), entries(BrowseTab.ALL, "@minecraft"));
    }

    @Test
    void theCraftableFilterAppliesLast()
    {
        assertEquals(List.of(List.of(PLANKS), List.of(STICK)),
                new BrowseQuery(BrowseTab.ALL, "oak", true).entries(CATALOG, item -> item == PLANKS || item == STICK));
        assertEquals(entries(BrowseTab.ALL, "oak"),
                new BrowseQuery(BrowseTab.ALL, "oak", false).entries(CATALOG, item -> false));
    }

    @Test
    void equalQueriesAreEqual()
    {
        assertEquals(new BrowseQuery(BrowseTab.ALL, "oak", true), new BrowseQuery(BrowseTab.ALL, "oak", true));
    }
}
