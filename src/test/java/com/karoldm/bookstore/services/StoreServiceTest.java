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
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private StoreService storeService;

    private UpdateStoreDTO updateStoreDTO;
    private Store store;

    @BeforeEach
    void setup() {
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                "image/jpeg",
                new byte[0]
        );
        updateStoreDTO = UpdateStoreDTO.builder()
                .name("store updated")
                .slogan("slogan updated")
                .banner(mockMultipartFile)
                .build();

        store = Store.builder()
                .name("bookstore")
                .slogan("The best tech books")
                .banner("image-url")
                .id(1L)
                .build();
    }

    @Nested
    class GetStoreTests {
        @Test
        void mustThrowNotFoundWhenStoreDoesNotExist() {
            Long id = 1L;

            when(storeRepository.findById(id)).thenReturn(Optional.empty());

            Exception ex = assertThrows(StoreNotFoundException.class, () -> {
                storeService.getStore(id);
            });

            assertEquals("Loja com id " + id + " não encontrada.", ex.getMessage());
            verify(storeRepository, times(1)).findById(id);
        }

        @Test
        void mustReturnStore() {
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
        void mustThrowNotFoundWhenStoreDoesNotExist() {
            Long id = 1L;

            when(storeRepository.findById(id)).thenReturn(Optional.empty());

            Exception ex = assertThrows(StoreNotFoundException.class, () -> {
                storeService.updateStore(id, updateStoreDTO);
            });

            assertEquals("Loja com id " + id + " não encontrada.", ex.getMessage());
            verify(storeRepository, times(1)).findById(id);
        }

        @Test
        void mustUpdateStore() {
            when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));
            when(fileStorageService.uploadFile(any())).thenReturn("image-url");
            ResponseStoreDTO responseStoreDTO = storeService.updateStore(store.getId(), updateStoreDTO);

            assertEquals(store.getId(), responseStoreDTO.getId());
            assertEquals(store.getName(), responseStoreDTO.getName());
            assertEquals(store.getSlogan(), responseStoreDTO.getSlogan());
            assertEquals(store.getBanner(), responseStoreDTO.getBanner());
            verify(storeRepository, times(1)).findById(store.getId());
            verify(storeRepository, times(1)).save(store);
            verify(fileStorageService, times(1)).uploadFile(any());
            verify(fileStorageService, times(1)).removeFileByUrl(any());
        }
    }
}
