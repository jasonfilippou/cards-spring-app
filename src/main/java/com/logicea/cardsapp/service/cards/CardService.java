package com.logicea.cardsapp.service.cards;


import static com.logicea.cardsapp.util.Constants.CREATING_USER_FILTER_STRING;

import com.logicea.cardsapp.model.card.CardDto;
import com.logicea.cardsapp.model.card.CardEntity;
import com.logicea.cardsapp.persistence.CardRepository;
import com.logicea.cardsapp.util.AggregateGetQueryParams;
import com.logicea.cardsapp.util.exceptions.CardNotFoundException;
import com.logicea.cardsapp.util.exceptions.InsufficientPrivilegesException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CardService {
    
    private final CardRepository cardRepository;
    private final AccessCheckService accessCheckService;

    public CardDto getCard(Long id) throws CardNotFoundException, InsufficientPrivilegesException {
        Optional<CardEntity> card = cardRepository.findById(id);
        // Did we even find a card with the designated id?
        if(card.isEmpty()){
            throw new CardNotFoundException(id);
        }
        // Do you have sufficient privileges for viewing the card?
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!accessCheckService.userHasAccessToCard(loggedInUser, card.get())){
            throw new InsufficientPrivilegesException(loggedInUser.getUsername());
        }
        // Ok, you're either an admin or a member with access to the card, so here's the card.
        return fromCardEntityToCardDto(card.get());
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

    public List<CardDto> getAllCardsByFilter(AggregateGetQueryParams params) throws InsufficientPrivilegesException {
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // Are you by any chance a member and you've requested another member's cards in your filters?
        if(accessCheckService.userIsMember(loggedInUser) && filterParamsIncludeOtherMemberCards(loggedInUser, params.getFilterParams())){
            throw new InsufficientPrivilegesException(loggedInUser.getUsername());
        }
        return cardRepository.findCardsByProvidedFilters(params, loggedInUser).stream()
                .map(this::fromCardEntityToCardDto)
                .collect(Collectors.toList());
    }
    
    public void deleteCard(Long id) throws CardNotFoundException, InsufficientPrivilegesException{
        Optional<CardEntity> card = cardRepository.findById(id);
        if(card.isEmpty()){
            throw new CardNotFoundException(id);
        }
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!accessCheckService.userHasAccessToCard(loggedInUser, card.get())){
            throw new InsufficientPrivilegesException(loggedInUser.getUsername());
        }
        cardRepository.deleteById(id);
    }

    private boolean filterParamsIncludeOtherMemberCards(User user, Map<String, String> filterParams){
        if(!filterParams.containsKey(CREATING_USER_FILTER_STRING)){
            return false;
        }
        return !filterParams.get(CREATING_USER_FILTER_STRING).equals(user.getUsername());
    }

    private CardDto fromCardEntityToCardDto(CardEntity cardEntity){
        return CardDto.builder()
                .id(cardEntity.getId())
                .description(cardEntity.getDescription())
                .name(cardEntity.getName())
                .color(cardEntity.getColor())
                .status(cardEntity.getStatus())
                .createdDateTime(cardEntity.getCreatedDateTime())
                .createdBy(cardEntity.getCreatedBy())
                .build();
    }
}
