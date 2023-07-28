package com.logicea.cardsapp.model.card;

import static com.logicea.cardsapp.util.Constants.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.logicea.cardsapp.controller.CardController;
import com.logicea.cardsapp.util.SortOrder;
import java.util.Collections;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class CardModelAssembler implements RepresentationModelAssembler<CardDto, EntityModel<CardDto>> {
    @Override
    public @NonNull EntityModel<CardDto> toModel(@NonNull CardDto cardDto) {

        return EntityModel.of(
                cardDto,
                linkTo(methodOn(CardController.class).getCard(cardDto.getId())).withSelfRel(),
                linkTo(methodOn(CardController.class).aggregateGetCards(Collections.emptyMap(), Integer.parseInt(DEFAULT_PAGE_IDX),
                        Integer.parseInt(DEFAULT_PAGE_SIZE), DEFAULT_SORT_BY_FIELD, SortOrder.ASC)).withRel(ALL_CARDS));
    }

    @Override
    public @NonNull CollectionModel<EntityModel<CardDto>> toCollectionModel(
            @NonNull Iterable<? extends CardDto> entities) {
        return CollectionModel.of(
                IterableUtils.toList(entities).stream().map(this::toModel).collect(Collectors.toList()),
                linkTo(methodOn(CardController.class).aggregateGetCards(Collections.emptyMap(), Integer.parseInt(DEFAULT_PAGE_IDX),
                        Integer.parseInt(DEFAULT_PAGE_SIZE), DEFAULT_SORT_BY_FIELD, SortOrder.ASC)).withSelfRel());
    }
}
