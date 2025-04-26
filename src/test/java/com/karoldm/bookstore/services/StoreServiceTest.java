package com.karoldm.bookstore.services;

import com.karoldm.bookstore.dto.requests.UpdateStoreDTO;
import com.karoldm.bookstore.dto.responses.ResponseStoreDTO;
import com.karoldm.bookstore.entities.Store;
import com.karoldm.bookstore.exceptions.StoreNotFoundException;
import com.karoldm.bookstore.repositories.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private StoreService storeService;

    private UpdateStoreDTO updateStoreDTO;
    private Store store;

    @BeforeEach
    void setup() {
        updateStoreDTO = UpdateStoreDTO.builder()
                .name("store updated")
                .slogan("slogan updated")
                .banner("base64image")
                .build();

        store = Store.builder()
                .books(Set.of())
                .employees(Set.of())
                .name("bookstore")
                .slogan("The best tech books")
                .banner(null)
                .id(UUID.randomUUID())
                .build();
    }

    @Nested
    class GetStoreTests {
        @Test
        void shouldThrowNotFoundWhenStoreDoesNotExist() {
            UUID id = UUID.randomUUID();

            when(storeRepository.findById(id)).thenReturn(Optional.empty());

            Exception ex = assertThrows(StoreNotFoundException.class, () -> {
                storeService.getStore(id);
            });

            assertEquals("Loja com id " + id + " não encontrada.", ex.getMessage());
            verify(storeRepository, times(1)).findById(id);
        }

        @Test
        void shouldReturnStore() {
            when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));

            ResponseStoreDTO responseStoreDTO = storeService.getStore(store.getId());

            assertEquals(store.getId(), responseStoreDTO.getId());
            assertEquals(store.getName(), responseStoreDTO.getName());
            assertEquals(store.getSlogan(), responseStoreDTO.getSlogan());
            assertEquals(store.getBanner(), responseStoreDTO.getBanner());
            verify(storeRepository, times(1)).findById(store.getId());
        }
    }

    @Nested
    class UpdateStoreTests {

        @Test
        void shouldThrowNotFoundWhenStoreDoesNotExist() {
            UUID id = UUID.randomUUID();

            when(storeRepository.findById(id)).thenReturn(Optional.empty());

            Exception ex = assertThrows(StoreNotFoundException.class, () -> {
                storeService.updateStore(id, updateStoreDTO);
            });

            assertEquals("Loja com id " + id + " não encontrada.", ex.getMessage());
            verify(storeRepository, times(1)).findById(id);
        }

        @Test
        void shouldUpdateStore() {
            when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));

            ResponseStoreDTO responseStoreDTO = storeService.updateStore(store.getId(), updateStoreDTO);

            assertEquals(store.getId(), responseStoreDTO.getId());
            assertEquals(store.getName(), responseStoreDTO.getName());
            assertEquals(store.getSlogan(), responseStoreDTO.getSlogan());
            assertEquals(store.getBanner(), responseStoreDTO.getBanner());
            verify(storeRepository, times(1)).findById(store.getId());
            verify(storeRepository, times(1)).save(store);
        }
    }
}
