package com.siemens.internship;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    private Item testItem;

    @BeforeEach
    void setUp() {
        // Clear the database before each test
        itemRepository.deleteAll();

        // Create a test item
        testItem = new Item();
        testItem.setName("Test Item");
        testItem.setDescription("Test Description");
        testItem.setStatus("PENDING");
        testItem.setEmail("test@example.com");
    }

    @Test
    void testCreateItem() throws Exception {
        mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testItem)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(testItem.getName()))
                .andExpect(jsonPath("$.email").value(testItem.getEmail()));
    }

    @Test
    void testCreateItemWithInvalidEmail() throws Exception {
        testItem.setEmail("invalid-email");
        mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testItem)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateItemWithMissingRequiredFields() throws Exception {
        testItem.setName(null);
        testItem.setEmail(null);
        testItem.setStatus(null);
        
        mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testItem)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAllItems() throws Exception {
        // Create an item first
        Item savedItem = itemService.save(testItem);

        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(savedItem.getName()))
                .andExpect(jsonPath("$[0].email").value(savedItem.getEmail()));
    }

    @Test
    void testGetItemById() throws Exception {
        // Create an item first
        Item savedItem = itemService.save(testItem);

        mockMvc.perform(get("/api/items/" + savedItem.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(savedItem.getName()))
                .andExpect(jsonPath("$.email").value(savedItem.getEmail()));
    }

    @Test
    void testGetItemByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/items/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateItem() throws Exception {
        // Create an item first
        Item savedItem = itemService.save(testItem);
        savedItem.setName("Updated Name");

        mockMvc.perform(put("/api/items/" + savedItem.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(savedItem)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void testUpdateItemWithInvalidEmail() throws Exception {
        // Create an item first
        Item savedItem = itemService.save(testItem);
        savedItem.setEmail("invalid-email");

        mockMvc.perform(put("/api/items/" + savedItem.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(savedItem)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateItemNotFound() throws Exception {
        mockMvc.perform(put("/api/items/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testItem)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteItem() throws Exception {
        // Create an item first
        Item savedItem = itemService.save(testItem);

        mockMvc.perform(delete("/api/items/" + savedItem.getId()))
                .andExpect(status().isNoContent());

        // Verify item is deleted
        mockMvc.perform(get("/api/items/" + savedItem.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testProcessItems() throws Exception {
        // Create multiple items
        Item item1 = new Item();
        item1.setName("Item 1");
        item1.setEmail("item1@example.com");
        item1.setStatus("PENDING");
        itemService.save(item1);

        Item item2 = new Item();
        item2.setName("Item 2");
        item2.setEmail("item2@example.com");
        item2.setStatus("PENDING");
        itemService.save(item2);

        MvcResult result = mockMvc.perform(get("/api/items/process"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertTrue(content.contains("PROCESSED"));
    }
} 