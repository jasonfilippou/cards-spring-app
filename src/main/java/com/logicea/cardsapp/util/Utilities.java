package com.logicea.cardsapp.util;


import com.logicea.cardsapp.model.card.CardDto;
import com.logicea.cardsapp.model.card.CardEntity;

public final class Utilities {
    
    private Utilities(){}

    // The following could probably be optimized with MapStruct or
    // BeanUtils/PropertyUtils.copyProperties().
    public static CardDto fromCardEntityToCardDto(CardEntity cardEntity) {
        return CardDto.builder()
                .id(cardEntity.getId())
                .description(cardEntity.getDescription())
                .name(cardEntity.getName())
                .color(cardEntity.getColor())
                .status(cardEntity.getStatus())
                .createdDateTime(cardEntity.getCreatedDateTime())
                .createdBy(cardEntity.getCreatedBy())
                .lastModifiedBy(cardEntity.getLastModifiedBy())
                .lastModifiedDateTime(cardEntity.getLastModifiedDateTime())
                .build();
    }
}
