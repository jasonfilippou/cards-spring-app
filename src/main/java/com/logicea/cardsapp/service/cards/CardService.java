package com.logicea.cardsapp.service.cards;

import static com.logicea.cardsapp.util.Constants.CREATING_USER_FILTER_STRING;
import static com.logicea.cardsapp.util.Utilities.fromCardEntityToCardDto;

import com.logicea.cardsapp.controller.CardController;
import com.logicea.cardsapp.model.card.CardDto;
import com.logicea.cardsapp.model.card.CardEntity;
import com.logicea.cardsapp.model.card.CardStatus;
import com.logicea.cardsapp.persistence.CardRepository;
import com.logicea.cardsapp.util.AggregateGetQueryParams;
import com.logicea.cardsapp.util.Utilities;
import com.logicea.cardsapp.util.exceptions.CardNotFoundException;
import com.logicea.cardsapp.util.exceptions.InsufficientPrivilegesException;
import com.logicea.cardsapp.util.logger.Logged;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A service that provides the core of the API functionality. Calls methods of {@link CardRepository} to persist
 * {@link CardEntity} instances. Returns {@link CardDto} instances to {@link CardController} so that the user can
 * receive appropriate payloads. Uses {@link AccessCheckService} and {@link PatchMapper} for internal work.
 * 
 * @see CardController
 * @see CardRepository
 * 
 * @author jason 
 */
@Service
@RequiredArgsConstructor
@Logged
public class CardService {

  private final CardRepository cardRepository;
  private final AccessCheckService accessCheckService;
  private final PatchMapper patchMapper;

  /**
   * Retrieve the card uniquely identified by the provided id.
   * @param id The ID of the card to retrieve.
   * @return A {@link CardDto} describing the unique card to return.
   * @throws CardNotFoundException If the card cannot be found in the repository.
   * @throws InsufficientPrivilegesException If the logged-in user does not have sufficient privileges for viewing the card.
   */
  @Transactional(readOnly = true)
  public CardDto getCard(Long id) throws CardNotFoundException, InsufficientPrivilegesException {
    Optional<CardEntity> card = cardRepository.findById(id);
    // Did we even find a card with the designated id?
    if (card.isEmpty()) {
      throw new CardNotFoundException(id);
    }
    // Do you have sufficient privileges for viewing the card?
    User loggedInUser =
        (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (!accessCheckService.userHasAccessToCard(loggedInUser, card.get())) {
      throw new InsufficientPrivilegesException(loggedInUser.getUsername());
    }
    // Ok, you're either an admin or a member with access to the card, so here's the card.
    return fromCardEntityToCardDto(card.get());
  }

  /**
   * Fully replace the card uniquely identified by the provided id with the information of the provided {@link CardDto}.
   * @param id The unique identifier of the card to replace.
   * @param cardDto The new card to replace the old card with.
   * @return The now replaced card.
   * @throws CardNotFoundException If the card to replace cannot be found in the repository.
   * @throws InsufficientPrivilegesException If the logged-in user does not have sufficient privileges for replacing the card.
   */
  @Transactional
  public CardDto replaceCard(Long id, CardDto cardDto)
      throws CardNotFoundException, InsufficientPrivilegesException {
    Optional<CardEntity> cardOptional = cardRepository.findById(id);
    if (cardOptional.isEmpty()) {
      throw new CardNotFoundException(id);
    }
    User loggedInUser =
        (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (!accessCheckService.userHasAccessToCard(loggedInUser, cardOptional.get())) {
      throw new InsufficientPrivilegesException(loggedInUser.getUsername());
    }
    CardEntity oldCard = cardOptional.get();
    // Need to save the creation information before we save() again.
    LocalDateTime createdDateTime = oldCard.getCreatedDateTime();
    String createdBy = oldCard.getCreatedBy();
    CardEntity newCard =
        cardRepository.save(
            CardEntity.builder()
                .id(oldCard.getId())
                .name(cardDto.getName())
                .color(cardDto.getColor())
                .description(cardDto.getDescription())
                .status(Optional.ofNullable(cardDto.getStatus()).orElse(CardStatus.TODO))
                .build());
    // Need to set those parameters explicitly because of the way save() works.
    newCard.setCreatedDateTime(createdDateTime);
    newCard.setCreatedBy(createdBy);
    newCard.setLastModifiedDateTime(LocalDateTime.now());
    newCard.setLastModifiedBy(loggedInUser.getUsername());
    return fromCardEntityToCardDto(newCard);
  }

  /**
   * Partially or fully update the card uniquely identified by the provided id with the information of the provided {@link CardDto}.
   * @param id The unique identifier of the card to update.
   * @param cardDto The new card information to update the old card with.
   * @return The now partially or fully updated card.
   * @throws CardNotFoundException If the card to update cannot be found in the repository.
   * @throws InsufficientPrivilegesException If the logged-in user does not have sufficient privileges for updating the card.
   */
  @Transactional
  public CardDto updateCard(Long id, CardDto cardDto){
    Optional<CardEntity> cardOptional = cardRepository.findById(id);
    if(cardOptional.isEmpty()){
      throw new CardNotFoundException(id);
    }
    User loggedInUser =
            (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (!accessCheckService.userHasAccessToCard(loggedInUser, cardOptional.get())) {
      throw new InsufficientPrivilegesException(loggedInUser.getUsername());
    }
    CardEntity cardEntity = cardOptional.get();
    LocalDateTime createdDateTime = cardEntity.getCreatedDateTime();
    String createdBy = cardEntity.getCreatedBy();
    patchMapper.updateEntityFromDto(cardDto, cardEntity);
    CardEntity patchedCard = cardRepository.save(CardEntity.builder()
            .id(cardEntity.getId())
            .name(cardEntity.getName())
            .description(cardEntity.getDescription())
            .color(cardEntity.getColor())
            .status(cardEntity.getStatus())
            .build());
    // Maintain proper audit fields
    patchedCard.setCreatedDateTime(createdDateTime);
    patchedCard.setCreatedBy(createdBy);
    patchedCard.setLastModifiedDateTime(LocalDateTime.now());
    patchedCard.setLastModifiedBy(loggedInUser.getUsername());
    return fromCardEntityToCardDto(patchedCard);
  }

  /**
   * Store the provided card in our repository
   * @param cardDto The card to store in our repository.
   * @return The card that was just stored in the repository.
   */
  @Transactional
  public CardDto storeCard(CardDto cardDto) {
    CardEntity storedCard =
        cardRepository.save(
            CardEntity.builder()
                .name(cardDto.getName())
                .color(cardDto.getColor())
                .description(cardDto.getDescription())
                .build());
    return fromCardEntityToCardDto(storedCard);
  }

  /**
   * Get all cards that satisfy the criteria provided.
   * @param params An instance of {@link AggregateGetQueryParams} that specifies pagination, sorting and filtering 
   *               criteria to apply to the cards that will be returned by the repository.
   * @return A {@link List} of cards that satisfy the provided criteria.
   * @throws InsufficientPrivilegesException If a member user attempts to filter the cards by the username of a different member
   * user or an admin.
   */

  @Transactional(readOnly = true)
  public List<CardDto> getAllCardsByFilter(AggregateGetQueryParams params)
      throws InsufficientPrivilegesException {
    User loggedInUser =
        (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    // Are you by any chance a member and you've requested another member's cards in your filters?
    if (accessCheckService.userIsMember(loggedInUser)
        && filterParamsIncludeOtherMemberCards(loggedInUser, params.getFilterParams())) {
      throw new InsufficientPrivilegesException(loggedInUser.getUsername());
    }
    return cardRepository.findCardsByProvidedFilters(params, loggedInUser).stream()
        .map(Utilities::fromCardEntityToCardDto)
        .collect(Collectors.toList());
  }

  private boolean filterParamsIncludeOtherMemberCards(User user, Map<String, String> filterParams) {
    if (!filterParams.containsKey(CREATING_USER_FILTER_STRING)) {
      return false;
    }
    return !filterParams.get(CREATING_USER_FILTER_STRING).equals(user.getUsername());
  }


  /**
   * Hard-delete the provided card from the repository.
   * @param id The unique ID of the card to delete.
   * @throws CardNotFoundException if we cannot find the card in the repository to delete (design choice).
   * @throws InsufficientPrivilegesException if a member user attempts to delete a card created by either a different
   * member user or an admin.
   */
  @Transactional
  public void deleteCard(Long id) throws CardNotFoundException, InsufficientPrivilegesException {
    Optional<CardEntity> card = cardRepository.findById(id);
    if (card.isEmpty()) {
      throw new CardNotFoundException(id);
    }
    User loggedInUser =
        (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (!accessCheckService.userHasAccessToCard(loggedInUser, card.get())) {
      throw new InsufficientPrivilegesException(loggedInUser.getUsername());
    }
    cardRepository.deleteById(id);
  }


}
