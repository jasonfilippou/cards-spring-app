package com.logicea.cardsapp.service.cards;

import static com.logicea.cardsapp.util.Constants.CREATING_USER_FILTER_STRING;

import com.logicea.cardsapp.model.card.CardDto;
import com.logicea.cardsapp.model.card.CardEntity;
import com.logicea.cardsapp.model.card.CardStatus;
import com.logicea.cardsapp.persistence.CardRepository;
import com.logicea.cardsapp.util.AggregateGetQueryParams;
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

@Service
@RequiredArgsConstructor
@Logged
public class CardService {

  private final CardRepository cardRepository;
  private final AccessCheckService accessCheckService;
  private final PatchMapper patchMapper;
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

  @Transactional
  public CardDto replaceCard(Long id, CardDto cardDto) {
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
        .map(this::fromCardEntityToCardDto)
        .collect(Collectors.toList());
  }

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

  private boolean filterParamsIncludeOtherMemberCards(User user, Map<String, String> filterParams) {
    if (!filterParams.containsKey(CREATING_USER_FILTER_STRING)) {
      return false;
    }
    return !filterParams.get(CREATING_USER_FILTER_STRING).equals(user.getUsername());
  }

  // The following could probably be optimized with MapStruct or
  // BeanUtils/PropertyUtils.copyProperties().
  private CardDto fromCardEntityToCardDto(CardEntity cardEntity) {
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
