package com.logicea.cardsapp.unit.service.cards;

import static com.logicea.cardsapp.util.Constants.ADMIN_AUTHORITY;
import static com.logicea.cardsapp.util.Constants.MEMBER_AUTHORITY;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.logicea.cardsapp.model.card.CardDto;
import com.logicea.cardsapp.model.card.CardEntity;
import com.logicea.cardsapp.model.card.CardStatus;
import com.logicea.cardsapp.persistence.CardRepository;
import com.logicea.cardsapp.service.cards.AccessCheckService;
import com.logicea.cardsapp.service.cards.CardService;
import com.logicea.cardsapp.util.exceptions.CardNotFoundException;
import com.logicea.cardsapp.util.exceptions.InsufficientPrivilegesException;
import java.util.Collections;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

@RunWith(MockitoJUnitRunner.class)
public class CardServiceUnitTests {
    
    @Mock
    private CardRepository cardRepository;
    
    @Mock
    private AccessCheckService accessCheckService;

    @InjectMocks
    private CardService cardService;

    public static final String ADMIN_EMAIL = "admin@company.com";
    public static final String ADMIN_PASSWORD = "adminpassword";
    public static final String MEMBER_EMAIL = "member@company.com";
    public static final String MEMBER_PASSWORD = "memberpassword";
    private static final User ADMIN_USER = new User(ADMIN_EMAIL, ADMIN_PASSWORD, Collections.singletonList(ADMIN_AUTHORITY));
    private static final User MEMBER_USER = new User(MEMBER_EMAIL, MEMBER_PASSWORD, Collections.singletonList(MEMBER_AUTHORITY));

    private static final UsernamePasswordAuthenticationToken ADMIN_UPAT = new UsernamePasswordAuthenticationToken(ADMIN_USER,
            null, ADMIN_USER.getAuthorities());
    private static final UsernamePasswordAuthenticationToken MEMBER_UPAT = new UsernamePasswordAuthenticationToken(MEMBER_USER,
            null, MEMBER_USER.getAuthorities());
    private static final CardDto CARD_DTO = CardDto.builder()
            .id(1L)
            .name("CARD-1")
            .description("A test card with ID 1")
            .status(CardStatus.TODO)
            .color("#G78JK0")
            .build();

    private static final CardEntity CARD_ENTITY = CardEntity.builder()
            .id(CARD_DTO.getId())
            .name(CARD_DTO.getName())
            .description(CARD_DTO.getDescription())
            .status(CARD_DTO.getStatus())
            .color(CARD_DTO.getColor())
            .build();

    // We will consider a CardDto to be "equal" to a CardEntity if the non-audit fields match.
    private static boolean cardsEqual(CardDto cardDto, CardEntity cardEntity){
        return cardDto.getId().equals(cardEntity.getId())
                && cardDto.getStatus().equals(cardEntity.getStatus())
                && cardDto.getDescription().equals(cardEntity.getDescription())
                && cardDto.getColor().equals(cardEntity.getColor())
                && cardDto.getName().equals(cardEntity.getName());
    }
    
    /* GET by ID tests */

    @Test
    public void whenRepoGetsAnEntityByIdSuccessfully_thenRelevantDtoIsReturned(){
        when(cardRepository.findById(CARD_DTO.getId())).thenReturn(Optional.of(CARD_ENTITY));
        SecurityContextHolder.getContext().setAuthentication(ADMIN_UPAT);
        when(accessCheckService.userHasAccessToCard(ADMIN_USER, CARD_ENTITY)).thenReturn(true);
        assertTrue(cardsEqual(cardService.getCard(CARD_DTO.getId()), CARD_ENTITY));
    }
    
    @Test(expected = CardNotFoundException.class)
    public void whenRepoCannotFindTheCard_thenCardNotFoundExceptionIsThrown(){
        when(cardRepository.findById(CARD_DTO.getId())).thenReturn(Optional.empty());
        SecurityContextHolder.getContext().setAuthentication(ADMIN_UPAT);
        cardService.getCard(CARD_DTO.getId());
    }
    
    @Test(expected = InsufficientPrivilegesException.class)
    public void whenUserHasInsufficientPrivilegesToGetCard_thenInsufficientPrivilegesExceptionIsThrown(){
        when(cardRepository.findById(CARD_DTO.getId())).thenReturn(Optional.of(CARD_ENTITY));
        SecurityContextHolder.getContext().setAuthentication(MEMBER_UPAT); 
        when(accessCheckService.userHasAccessToCard(MEMBER_USER, CARD_ENTITY)).thenReturn(false);
        cardService.getCard(CARD_DTO.getId());
    }
    
    /* Aggregate GET tests */
    
    

    /* POST tests */

    /* DELETE tests */

    /* PUT tests */

    /* PATCH tests */

    // TODO
}
