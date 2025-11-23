package com.example.tpb.service;

import com.example.tpb.model.GiftCard;
import com.example.tpb.persistence.entities.GiftCardEntity;
import com.example.tpb.persistence.repositories.GiftCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GiftCardService {
    @Autowired private GiftCardRepository repository;

    @Transactional(readOnly = true)
    public GiftCard findById(String id) {
        GiftCardEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(GiftCard.InvalidCard));
        return toModel(entity);
    }

    @Transactional
    public GiftCard save(GiftCard card) {
        GiftCardEntity entity = repository.findById(card.id())
                .orElse(new GiftCardEntity(card.id(), card.balance()));
        updateEntity(entity, card);
        repository.save(entity);
        return card;
    }

    @Transactional(readOnly = true)
    public long count() {
        return repository.count();
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    private GiftCard toModel(GiftCardEntity entity) {
        if (entity.getOwner() != null && !entity.getOwner().isEmpty()) {
            return new GiftCard(
                    entity.getId(),
                    entity.getBalance(),
                    entity.getOwner(),
                    entity.getCharges()
            );
        } else {
            return new GiftCard(entity.getId(), entity.getBalance());
        }
    }

    private void updateEntity(GiftCardEntity entity, GiftCard card) {
        entity.setBalance(card.balance());
        entity.setOwner(card.owner());
        entity.setCharges(new java.util.ArrayList<>(card.charges())); // Defensive copy
    }
}