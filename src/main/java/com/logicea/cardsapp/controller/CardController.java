package com.logicea.cardsapp.controller;

import static com.logicea.cardsapp.util.Constants.*;

import com.logicea.cardsapp.model.card.CardDto;
import com.logicea.cardsapp.model.card.CardModelAssembler;
import com.logicea.cardsapp.service.cards.CardService;
import com.logicea.cardsapp.util.AggregateGetQueryParams;
import com.logicea.cardsapp.util.Constants;
import com.logicea.cardsapp.util.SortOrder;
import com.logicea.cardsapp.util.exceptions.CardNameCannotBeBlankException;
import com.logicea.cardsapp.util.exceptions.CardNameNotProvidedException;
import com.logicea.cardsapp.util.exceptions.InvalidSortByFieldException;
import com.logicea.cardsapp.util.logger.Logged;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for querying the API for {@link CardDto} instances. Supports operations for POST, GET
 * (id and aggreggate), PUT, PATCH and DELETE. <a href =
 * "https://hibernate.org/validator/#:~:text=Hibernate%20Validator%20allows%20to%20express,server%20and%20client%20application%20programming.">
 * Hibernate validators</a> are used to validate the request bodies, where applicable.
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
@Logged
public class CardController {

  private final CardService cardService;

  private final CardModelAssembler assembler;

  /**
   * POST mapping endpoint for cards.
   *
   * @param cardDto The deserialized JSON payload describing the card's parameters.
   * @return A {@link ResponseEntity} with appropriate status:
   *     <ul>
   *       <li>{@link HttpStatus#BAD_REQUEST} and an error message in the case of bad requests.
   *       <li>{@link HttpStatus#OK} and an updated payload with the original card data enhanced
   *           with audit data in the case of successful requests.
   *     </ul>
   *
   * @throws CardNameNotProvidedException if the user does not supply a card name. We require that
   *     new card instances in the API have a card name.
   */
  @Operation(summary = "Store a new card")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Card successfully created",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema =  @Schema(
                          type = "object",
                          additionalProperties = Schema.AdditionalPropertiesValue.TRUE,
                          ref = "#/components/schemas/FullCardDto"))
            }),
        @ApiResponse(
            responseCode = "400",
            description = "Card Name not provided",
            content = @Content),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthenticated user",
            content = @Content),
      })
  @PostMapping("/card")
  public ResponseEntity<EntityModel<CardDto>> postCard(@RequestBody @Valid CardDto cardDto)
      throws CardNameNotProvidedException {
    if (StringUtils.isBlank(cardDto.getName())) {
      throw new CardNameNotProvidedException();
    }
    return new ResponseEntity<>(
        assembler.toModel(cardService.storeCard(cardDto)), HttpStatus.CREATED);
  }

  /**
   * Get-by-ID endpoint. Member users that attempt to access cards of other member users or admins
   * cannot do so, and are informed via the return of status code {@link HttpStatus#FORBIDDEN}.
   *
   * @param id The unique ID of the card to look up.
   * @return A {@link ResponseEntity} with either an error status code and an error message or
   *     {@link HttpStatus#OK} and the requested card's payload.
   */
  @Operation(summary = "Get card by ID")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Card successfully retrieved",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema =  @Schema(
                          type = "object",
                          additionalProperties = Schema.AdditionalPropertiesValue.TRUE,
                          ref = "#/components/schemas/FullCardDto"))
            }),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthenticated user",
            content = @Content),
        @ApiResponse(
            responseCode = "403",
            description = "Attempted to get card that we don't have access to.",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Card not found", content = @Content)
      })
  @GetMapping("/card/{id}")
  public ResponseEntity<EntityModel<CardDto>> getCard(@PathVariable Long id) {
    return ResponseEntity.ok(assembler.toModel(cardService.getCard(id)));
  }

  /**
   * Aggregate GET endpoint for filtered, paginated and sorted queries for multiple cards.
   *
   * @param filterParams A {@link Map} describing the possible filters to filter returned cards by.
   *     For example, {@link Constants#NAME_FILTER_STRING}, {@link Constants#COLOR_FILTER_STRING}.
   * @param page The zero-based index of the page that we want to return, default 0.
   * @param pageSize The number of records per page to return, default 5.
   * @param sortByField The field to sort the responses by, default &quot;id&quot;
   * @param sortOrder The sort order of the results, default {@link SortOrder#ASC}.
   * @return A {@link ResponseEntity} with {@link HttpStatus#OK} and the cards that specify the
   *     required criteria, or a combination of appropriate Http error code and error text. Members
   *     that attempt to filter by a different member's or an admin's username are returned an error
   *     {@link HttpStatus#FORBIDDEN}.
   * @throws InvalidSortByFieldException if the user provides an invalid field to sort by. Note that
   *     fields need to be provided in camelCase format.
   */
  @Operation(summary = "Get all cards that satisfy some criteria")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cards successfully retrieved",
            content = {
              @Content(
                  mediaType = "application/json",
                  array = @ArraySchema(schema = @Schema(
                          type = "object",
                          additionalProperties = Schema.AdditionalPropertiesValue.TRUE,
                          ref = "#/components/schemas/FullCardDto")))
            }),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid sorting field specified",
            content = @Content),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthenticated user",
            content = @Content),
        @ApiResponse(
            responseCode = "403",
            description = "Tried to filter cards by different user's name",
            content = @Content)
      })
  @GetMapping("/card")
  public ResponseEntity<CollectionModel<EntityModel<CardDto>>> aggregateGetCards(
          @Parameter(
                  name = "filterParams",
                  in = ParameterIn.QUERY,
                  schema =
                  @Schema(
                          type = "object",
                          additionalProperties = Schema.AdditionalPropertiesValue.TRUE,
                          ref = "#/components/schemas/FilterMap"),
                  style = ParameterStyle.FORM,
                  explode = Explode.TRUE)
      @RequestParam Map<String, String> filterParams,
      @RequestParam(name = "page", defaultValue = DEFAULT_PAGE_IDX) @Min(0) Integer page,
      @RequestParam(name = "items_in_page", defaultValue = DEFAULT_PAGE_SIZE) @Min(1)
          Integer pageSize,
      @RequestParam(name = "sort_by_field", defaultValue = DEFAULT_SORT_BY_FIELD) @NonNull @NotBlank
          String sortByField,
      @RequestParam(name = "sort_order", defaultValue = DEFAULT_SORT_ORDER) @NonNull
          SortOrder sortOrder)
      throws InvalidSortByFieldException {
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

  /**
   * PUT endpoint for Cards. We follow the traditional semantics of PUT, for full replacement, but
   * for auditing purposes we do not update the creation timestamp. Members that attempt to PUT on
   * an ID that they have not POST-ed receive a {@link HttpStatus#FORBIDDEN} error code and an
   * appropriate message.
   *
   * @param id THe unique ID of the card to replace.
   * @param cardDto The data of the new card to replace. It should provide a name.
   * @return The updated card and {@link HttpStatus#OK}, or a combination of Http error code and
   *     error text.
   * @throws CardNameNotProvidedException if the user has not provided a name for the new card.
   */
  @Operation(summary = "Replace a Card")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Card successfully replaced",
            content =
                @Content(
                    mediaType = "application/json",
                    schema =  @Schema(
                            type = "object",
                            additionalProperties = Schema.AdditionalPropertiesValue.TRUE,
                            ref = "#/components/schemas/FullCardDto"))),
        @ApiResponse(
            responseCode = "400",
            description = "Card Name not provided",
            content = @Content),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthenticated user",
            content = @Content),
        @ApiResponse(
            responseCode = "403",
            description = "Access to card forbidden",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Card not found", content = @Content)
      })
  @PutMapping("/card/{id}")
  public ResponseEntity<EntityModel<CardDto>> putCard(
      @PathVariable Long id, @RequestBody @Valid CardDto cardDto)
      throws CardNameNotProvidedException {
    if (StringUtils.isBlank(cardDto.getName())) {
      throw new CardNameNotProvidedException();
    }
    return ResponseEntity.ok(assembler.toModel(cardService.replaceCard(id, cardDto)));
  }

  /**
   * PATCH endpoint for cards. {@literal null} or missing entries are ignored. We do not allow the
   * user to clear the name of the card, though. Members that attempt to PATCH on an ID that they
   * have not POST-ed receive a {@link HttpStatus#FORBIDDEN} error code and an appropriate message.
   *
   * @param id THe unique ID of the card to update.
   * @param cardDto The data of the new card to update.
   * @return The updated card and {@link HttpStatus#OK}, or a combination of Http error code and
   *     error text.
   * @throws CardNameCannotBeBlankException if the user tries to clear the name of the card by
   *     providing a whitespace-only non-{@literal null} string.
   */
  @Operation(summary = "Update a Card")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Card successfully updated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema =  @Schema(
                            type = "object",
                            additionalProperties = Schema.AdditionalPropertiesValue.TRUE,
                            ref = "#/components/schemas/FullCardDto"))),
        @ApiResponse(
            responseCode = "400",
            description = "Attempted to clear card name.",
            content = @Content),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthenticated user",
            content = @Content),
        @ApiResponse(
            responseCode = "403",
            description = "Access to card forbidden",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Card not found", content = @Content)
      })
  @PatchMapping("/card/{id}")
  public ResponseEntity<EntityModel<CardDto>> patchCard(
      @PathVariable Long id, @RequestBody @Valid CardDto cardDto)
      throws CardNameCannotBeBlankException {
    // We don't allow clearing the name of a card.
    if (StringUtils.isWhitespace(
        cardDto.getName())) { // StringUtils.isWhitespace(null) returns false, which is good.
      throw new CardNameCannotBeBlankException();
    }
    return ResponseEntity.ok(assembler.toModel(cardService.updateCard(id, cardDto)));
  }

  /**
   * DELETE endpoint for cards. We hard-delete cards from the database, for simplicity. Members that
   * attempt to DELETE an ID that they have not POST-ed receive a {@link HttpStatus#FORBIDDEN} error
   * code and an appropriate message.
   *
   * @param id The unique ID of the card to delete. If we cannot find the ID, we choose to send a
   *     {@link HttpStatus#NOT_FOUND} error code to the user.
   * @return A no-content {@link ResponseEntity} if the deletion worked well, otherwise a
   *     combination of Http status code and error.
   */
  @Operation(summary = "Delete card by ID")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "204",
            description = "Card successfully deleted",
            content = @Content),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthenticated user",
            content = @Content),
        @ApiResponse(
            responseCode = "403",
            description = "Access to card forbidden",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Card not found", content = @Content)
      })
  @DeleteMapping("/card/{id}")
  public ResponseEntity<?> deleteCard(@PathVariable Long id) {
    cardService.deleteCard(id);
    return ResponseEntity.noContent().build();
  }
}
