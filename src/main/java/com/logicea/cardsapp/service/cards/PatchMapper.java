package com.logicea.cardsapp.service.cards;

import com.logicea.cardsapp.model.card.CardDto;
import com.logicea.cardsapp.model.card.CardEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.stereotype.Component;

/**
 * A {@link Mapper} used by PATCH logic to map the fields of a {@link CardDto} onto a {@link CardEntity} just pulled
 * from the database.
 *
 * @author jason
 */
@Mapper(componentModel = "spring")
@Component
public interface PatchMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(CardDto cardDto, @MappingTarget CardEntity cardEntity);
}
