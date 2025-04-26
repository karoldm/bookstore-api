package com.karoldm.bookstore.services;

import com.karoldm.bookstore.dto.requests.UpdateStoreDTO;
import com.karoldm.bookstore.dto.responses.ResponseStoreDTO;
import com.karoldm.bookstore.entities.Store;
import com.karoldm.bookstore.exceptions.StoreNotFoundException;
import com.karoldm.bookstore.repositories.StoreRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class StoreService {
    private StoreRepository storeRepository;

    public ResponseStoreDTO getStore(UUID id) {
        Optional<Store> optionalStore = storeRepository.findById(id);

        if (optionalStore.isEmpty()) {
            throw new StoreNotFoundException(id);
        }

        Store store = optionalStore.get();

        return ResponseStoreDTO.builder()
                .id(id)
                .banner(store.getBanner())
                .name(store.getName())
                .slogan(store.getSlogan())
                .build();
    }

    @Transactional
    public ResponseStoreDTO updateStore(UUID id, UpdateStoreDTO updateStoreDTO) {
        Optional<Store> optionalStore = storeRepository.findById(id);

        if (optionalStore.isEmpty()) {
            throw new StoreNotFoundException(id);
        }

        Store store = optionalStore.get();

        store.setName(updateStoreDTO.getName());
        store.setBanner(updateStoreDTO.getBanner());
        store.setSlogan(updateStoreDTO.getSlogan());

        storeRepository.save(store);

        return ResponseStoreDTO.builder()
                .id(store.getId())
                .name(store.getName())
                .slogan(store.getSlogan())
                .banner(store.getBanner())
                .build();
    }
}
