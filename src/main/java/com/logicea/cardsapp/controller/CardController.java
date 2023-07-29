package com.logicea.cardsapp.controller;

import static com.logicea.cardsapp.util.Constants.*;

import com.logicea.cardsapp.model.card.CardDto;
import com.logicea.cardsapp.model.card.CardModelAssembler;
import com.logicea.cardsapp.service.cards.CardService;
import com.logicea.cardsapp.util.AggregateGetQueryParams;
import com.logicea.cardsapp.util.SortOrder;
import com.logicea.cardsapp.util.exceptions.InvalidSortByFieldException;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for querying the API for {@link CardDto} instances. Supports operations for POST, GET
 * (id and aggreggate), PUT, PATCH and DELETE.
 *
 * @author jason
 * @see CardDto
 */
@RestController
@RequestMapping("/cardsapi")
@CrossOrigin
@RequiredArgsConstructor
@Tag(name = "2. Cards API")
@Validated
public class CardController {

  private final CardService cardService;

  private final CardModelAssembler assembler;

  @PostMapping("/card")
  public ResponseEntity<EntityModel<CardDto>> postCard(@RequestBody @Valid CardDto cardDto) {
    return new ResponseEntity<>(
        assembler.toModel(cardService.storeCard(cardDto)), HttpStatus.CREATED);
  }

  @GetMapping("/card/{id}")
  public ResponseEntity<EntityModel<CardDto>> getCard(@PathVariable Long id) {
    return ResponseEntity.ok(assembler.toModel(cardService.getCard(id)));
  }

  @GetMapping("/card")
  public ResponseEntity<CollectionModel<EntityModel<CardDto>>> aggregateGetCards(
      @RequestParam Map<String, String> filterParams,
      @RequestParam(name = "page", defaultValue = DEFAULT_PAGE_IDX) @Min(0) Integer page,
      @RequestParam(name = "items_in_page", defaultValue = DEFAULT_PAGE_SIZE) @Min(1)
          Integer pageSize,
      @RequestParam(name = "sort_by_field", defaultValue = DEFAULT_SORT_BY_FIELD) @NonNull @NotBlank
          String sortByField,
      @RequestParam(name = "sort_order", defaultValue = DEFAULT_SORT_ORDER) @NonNull
          SortOrder sortOrder) {
    List<String> cardFieldNames =
        Arrays.stream(CardDto.class.getDeclaredFields()).map(Field::getName).toList();
    if (!cardFieldNames.contains(sortByField)) {
      throw new InvalidSortByFieldException(sortByField, cardFieldNames);
    }
    return ResponseEntity.ok(
        assembler.toCollectionModel(
            cardService.getAllCardsByFilter(
                AggregateGetQueryParams.builder()
                    .filterParams(filterParams)
                    .page(page)
                    .pageSize(pageSize)
                    .sortByField(sortByField)
                    .sortOrder(sortOrder)
                    .build())));
  }

  @PutMapping("/card/{id}")
  public ResponseEntity<EntityModel<CardDto>> putCard(
      @PathVariable Long id, @RequestBody @Valid CardDto cardDto) {
    return ResponseEntity.ok(assembler.toModel(cardService.replaceCard(id, cardDto)));
  }

  @DeleteMapping("/card/{id}")
  public ResponseEntity<?> deleteCard(@PathVariable Long id) {
    cardService.deleteCard(id);
    return ResponseEntity.noContent().build();
  }
}
