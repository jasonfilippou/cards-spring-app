package com.logicea.cardsapp.service.cards;

import com.logicea.cardsapp.model.card.CardDto;
import com.logicea.cardsapp.model.card.CardEntity;
import com.logicea.cardsapp.persistence.CardRepository;
import com.logicea.cardsapp.util.AggregateGetQueryParams;
import com.logicea.cardsapp.util.exceptions.CardNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardService {
    
    private final CardRepository cardRepository;
    
    public CardDto getCard(Long id){
        return cardRepository
            .findById(id)
            .map(this::fromCardEntityToCardDto)
            .orElseThrow(() -> new CardNotFoundException(id));
    }

    public CardDto storeCard(CardDto cardDto){
        CardEntity storedCard = cardRepository.save(
                CardEntity.builder()
                        .name(cardDto.getName())
                        .color(cardDto.getColor())
                        .description(cardDto.getDescription())
                        .build());
        return fromCardEntityToCardDto(storedCard);

    }

    public List<CardDto> getAllCardsByFilter(AggregateGetQueryParams params){
        return cardRepository.findCardsByProvidedFilters(params).stream()
                .map(this::fromCardEntityToCardDto)
                .collect(Collectors.toList());
    }

    private CardDto fromCardEntityToCardDto(CardEntity cardEntity){
        return CardDto.builder()
                .id(cardEntity.getId())
                .description(cardEntity.getDescription())
                .name(cardEntity.getDescription())
                .color(cardEntity.getColor())
                .status(cardEntity.getStatus())
                .createdDate(cardEntity.getCreatedDate())
                .createdBy(cardEntity.getCreatedBy())
                .build();
    }
}
