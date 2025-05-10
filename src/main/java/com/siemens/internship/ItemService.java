package com.siemens.internship;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Service class responsible for managing Item operations in the application.
 * Provides CRUD operations and asynchronous item processing capabilities.
 * 
 * This service handles:
 * - Basic CRUD operations for Item entities
 * - Asynchronous processing of items with parallel execution
 * - Transaction management for database operations
 * - Thread pool management for concurrent processing
 */
@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;

    /**
     * Thread pool for parallel item processing.
     * Size is set to match the number of available processors for optimal performance.
     */
    private static final ExecutorService executor =
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    /**
     * Retrieves all items from the database.
     * 
     * @return List of all items in the system
     */
    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    /**
     * Retrieves a specific item by its unique identifier.
     * 
     * @param id The unique identifier of the item to retrieve
     * @return Optional containing the item if found, empty otherwise
     */
    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    /**
     * Saves or updates an item in the database.
     * Performs validation before saving and manages the transaction.
     * 
     * @param item The item to save or update
     * @return The saved item with updated fields
     * @throws IllegalArgumentException if the item is null
     * @throws jakarta.validation.ConstraintViolationException if validation fails
     */
    @Transactional
    public Item save(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }
        return itemRepository.save(item);
    }

    /**
     * Deletes an item from the database by its unique identifier.
     * 
     * @param id The unique identifier of the item to delete
     */
    @Transactional
    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }

    /**
     * Processes all items asynchronously in parallel.
     * 
     * Implementation details:
     * 1. Loads all items synchronously in the current transaction context
     * 2. Creates parallel processing tasks for each item
     * 3. Each task:
     *    - Simulates processing time
     *    - Updates item status to "PROCESSED"
     *    - Saves the item in a new transaction
     * 4. Waits for all tasks to complete
     * 5. Returns the list of successfully processed items
     * 
     * @return List of items that were successfully processed
     */
    public List<Item> processItemsAsync() {
        // Load all items synchronously in the current transaction context
        List<Item> toProcess = itemRepository.findAll();

        // Create parallel processing tasks
        List<CompletableFuture<Item>> futures = toProcess.stream()
            .map(item -> CompletableFuture.supplyAsync(() -> {
                try {
                    // Simulate processing time
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                // Update and save item in a new transaction
                item.setStatus("PROCESSED");
                return itemRepository.save(item);
            }, executor))
            .collect(Collectors.toList());

        // Wait for all tasks to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // Collect and return results
        return futures.stream()
                      .map(CompletableFuture::join)
                      .collect(Collectors.toList());
    }

    /**
     * Gracefully shuts down the executor service.
     * Should be called during application shutdown to ensure proper cleanup.
     * 
     * Implementation:
     * 1. Initiates shutdown
     * 2. Waits up to 60 seconds for tasks to complete
     * 3. Forces shutdown if tasks don't complete in time
     */
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

