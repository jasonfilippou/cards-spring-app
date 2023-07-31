package com.logicea.cardsapp.unit.controller;

import static com.logicea.cardsapp.util.TestUtils.CARD_DTOS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.logicea.cardsapp.controller.CardController;
import com.logicea.cardsapp.model.card.CardDto;
import com.logicea.cardsapp.model.card.CardModelAssembler;
import com.logicea.cardsapp.service.cards.CardService;
import com.logicea.cardsapp.util.AggregateGetQueryParams;
import com.logicea.cardsapp.util.SortOrder;
import com.logicea.cardsapp.util.exceptions.CardNameCannotBeBlankException;
import com.logicea.cardsapp.util.exceptions.CardNameNotProvidedException;
import com.logicea.cardsapp.util.exceptions.InvalidSortByFieldException;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RunWith(MockitoJUnitRunner.class)
public class CardControllerUnitTests {

    @Mock
    private CardService cardService;

    @Mock
    private CardModelAssembler cardModelAssembler;

    @InjectMocks
    private CardController cardController;

    private static final CardDto STANDARD_DTO = CARD_DTOS.get(0);
    
    @Before
    public void setupAssembler(){
        // Pseudo-mocking the assembler.
        lenient().when(cardModelAssembler.toModel(any(CardDto.class))).thenCallRealMethod();
        lenient().when(cardModelAssembler.toCollectionModel(any())).thenCallRealMethod();
    }

    /* Get by ID tests */

    @Test
    public void whenServiceSuccessfullyReturnsDto_thenResponseEntityContainsFormattedDto(){
        when(cardService.getCard(STANDARD_DTO.getId())).thenReturn(STANDARD_DTO);
        assertEquals(ResponseEntity.ok(cardModelAssembler.toModel(STANDARD_DTO)), 
                cardController.getCard(STANDARD_DTO.getId()));
    }

    /* POST tests */
    
    @Test
    public void whenServiceSuccessfullyStoresAndReturnsDto_thenResponseEntityContainsFormattedDto(){
        when(cardService.storeCard(STANDARD_DTO)).thenReturn(STANDARD_DTO);
        assertEquals(new ResponseEntity<>(cardModelAssembler.toModel(STANDARD_DTO), HttpStatus.CREATED),
                cardController.postCard(STANDARD_DTO));
    }
    
    @Test(expected = CardNameNotProvidedException.class)
    public void whenUserDoesNotProvideANameForCard_thenCardNameNotProvidedExceptionisThrown(){
        cardController.postCard(CardDto.builder().color("#9kL042").build());
    }

    /* GET ALL tests */

    @Test
    public void whenServiceReturnsACollectionOfDtos_thenResponseEntityContainsFormattedCollection(){
        when(cardService.getAllCardsByFilter(any(AggregateGetQueryParams.class))).thenReturn(CARD_DTOS);
        assertEquals(ResponseEntity.ok(cardModelAssembler.toCollectionModel(CARD_DTOS)), 
                cardController.aggregateGetCards(Collections.emptyMap(), 0, 1, "id", SortOrder.ASC));
    }
    @Test(expected = InvalidSortByFieldException.class)
    public void whenAnInvalidSortByFieldIsUsed_thenSInvalidSortByFieldExceptionIsThrown(){
        cardController.aggregateGetCards(Collections.emptyMap(), 0, 1, 
                "MEME_FIELD", SortOrder.ASC);
    }
    
    /* PUT tests */

    @Test
    public void whenServiceSuccessfullyReplacesCardWithDto_thenResponseEntityContainsFormattedDto(){
        when(cardService.replaceCard(STANDARD_DTO.getId(), STANDARD_DTO)).thenReturn(STANDARD_DTO);
        assertEquals(ResponseEntity.ok(cardModelAssembler.toModel(STANDARD_DTO)),
                cardController.putCard(STANDARD_DTO.getId(), STANDARD_DTO));
    }
    
    @Test(expected = CardNameNotProvidedException.class)
    public void whenAttemptingToReplaceACardWithANamelessCard_thenCardNameNotProvidedExceptionIsThrown(){
        cardController.putCard(1L, CardDto.builder().description("desc").color("#45HJLP").build());
    }
    
    /* PATCH tests */
    @Test
    public void whenServiceSuccessfullyUpdatesCardWithDto_thenResponseEntityContainsFormattedDto(){
        when(cardService.updateCard(STANDARD_DTO.getId(), STANDARD_DTO)).thenReturn(STANDARD_DTO);
        assertEquals(ResponseEntity.ok(cardModelAssembler.toModel(STANDARD_DTO)),
                cardController.patchCard(STANDARD_DTO.getId(), STANDARD_DTO));
    }
    
    @Test(expected = CardNameCannotBeBlankException.class)
    public void whenAttemptingToClearTheNameOfACard_thenCardNameCannotBeBlankExceptionIsThrown(){
        cardController.patchCard(1L, CardDto.builder().name("     ").description("desc").build());
    }
    
    /* DELETE tests */
    
    @Test
    public void whenServiceSuccessfullyDeletesACard_thenOk(){
        doNothing().when(cardService).deleteCard(STANDARD_DTO.getId());
        assertEquals(ResponseEntity.noContent().build(), cardController.deleteCard(STANDARD_DTO.getId()));
    }

}
