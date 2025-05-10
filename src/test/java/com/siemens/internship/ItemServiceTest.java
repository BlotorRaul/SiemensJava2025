package com.siemens.internship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ItemServiceTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    private Item testItem;

    @BeforeEach
    void setUp() {
        // Create a test item
        testItem = new Item();
        testItem.setName("Test Item");
        testItem.setDescription("Test Description");
        testItem.setStatus("PENDING");
        testItem.setEmail("test@example.com");
    }

    @Test
    void testSaveAndFindById() {
        // Save the item
        Item savedItem = itemService.save(testItem);
        assertNotNull(savedItem.getId());

        // Find the item
        Optional<Item> foundItem = itemService.findById(savedItem.getId());
        assertTrue(foundItem.isPresent());
        assertEquals(testItem.getName(), foundItem.get().getName());
        assertEquals(testItem.getEmail(), foundItem.get().getEmail());
    }

    @Test
    void testFindAll() {
        // Save multiple items
        Item item1 = new Item();
        item1.setName("Item 1");
        item1.setEmail("item1@example.com");
        item1.setStatus("PENDING");

        Item item2 = new Item();
        item2.setName("Item 2");
        item2.setEmail("item2@example.com");
        item2.setStatus("PENDING");

        itemService.save(item1);
        itemService.save(item2);

        // Find all items
        List<Item> items = itemService.findAll();
        assertTrue(items.size() >= 2);
        
        // Verify items are in the list
        assertTrue(items.stream().anyMatch(item -> item.getName().equals("Item 1")));
        assertTrue(items.stream().anyMatch(item -> item.getName().equals("Item 2")));
    }

    @Test
    void testDeleteById() {
        // Save an item
        Item savedItem = itemService.save(testItem);
        assertNotNull(savedItem.getId());

        // Delete the item
        itemService.deleteById(savedItem.getId());

        // Verify the item is deleted
        Optional<Item> deletedItem = itemService.findById(savedItem.getId());
        assertFalse(deletedItem.isPresent());
    }

    @Test
    void testProcessItemsAsync() throws InterruptedException {
        // Clear any existing items
        itemRepository.deleteAll();

        // Save multiple items
        Item item1 = new Item();
        item1.setName("Item 1");
        item1.setEmail("item1@example.com");
        item1.setStatus("PENDING");

        Item item2 = new Item();
        item2.setName("Item 2");
        item2.setEmail("item2@example.com");
        item2.setStatus("PENDING");

        Item savedItem1 = itemService.save(item1);
        Item savedItem2 = itemService.save(item2);

        // Process items
        List<Item> processedItems = itemService.processItemsAsync();

        // Verify processing
        assertNotNull(processedItems);
        assertEquals(2, processedItems.size(), "Expected 2 processed items, but got " + processedItems.size());
        
        // Get all items from database to verify their status
        List<Item> allItems = itemRepository.findAll();
        assertEquals(2, allItems.size(), "Expected 2 items in database");
        
        // Verify all items were processed
        allItems.forEach(item -> {
            assertEquals("PROCESSED", item.getStatus(), 
                "Item " + item.getId() + " should have status PROCESSED");
            assertTrue(item.getName().equals("Item 1") || item.getName().equals("Item 2"),
                "Item should be one of the test items");
        });
    }

    @Test
    void testProcessItemsAsyncWithNoItems() {
        // Clear all items
        itemRepository.deleteAll();
        
        // Process items
        List<Item> processedItems = itemService.processItemsAsync();
        
        // Verify no items were processed
        assertTrue(processedItems.isEmpty());
    }

    @Test
    void testInvalidEmail() {
        // Try to save an item with invalid email
        testItem.setEmail("invalid-email");
        
        // The save should fail with validation error
        assertThrows(jakarta.validation.ConstraintViolationException.class, () -> {
            itemService.save(testItem);
        });
    }

    @Test
    void testNullItem() {
        // Try to save a null item
        assertThrows(IllegalArgumentException.class, () -> {
            itemService.save(null);
        });
    }
} 