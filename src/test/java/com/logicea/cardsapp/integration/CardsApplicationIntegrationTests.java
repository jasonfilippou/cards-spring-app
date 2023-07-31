package com.logicea.cardsapp.integration;

import static com.logicea.cardsapp.util.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

import com.logicea.cardsapp.controller.CardController;
import com.logicea.cardsapp.controller.JwtAuthenticationController;
import com.logicea.cardsapp.model.card.CardDto;
import com.logicea.cardsapp.model.card.CardModelAssembler;
import com.logicea.cardsapp.model.card.CardStatus;
import com.logicea.cardsapp.model.jwt.JwtRequest;
import com.logicea.cardsapp.model.jwt.JwtResponse;
import com.logicea.cardsapp.model.user.UserDto;
import com.logicea.cardsapp.util.exceptions.CardNotFoundException;
import com.logicea.cardsapp.util.exceptions.EmailAlreadyInDatabaseException;
import com.logicea.cardsapp.util.exceptions.InsufficientPrivilegesException;
import io.micrometer.common.util.StringUtils;
import java.util.Objects;
import lombok.NoArgsConstructor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@NoArgsConstructor
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@SuppressWarnings("ConstantConditions")
public class CardsApplicationIntegrationTests {
   
    @Autowired
    private CardController cardController;
    @Autowired
    private JwtAuthenticationController authenticationController;
    @Autowired
    private CardModelAssembler assembler;
    
    /* ********************* */
    /* Authentication Tests */
    /* ********************* */


    @Test
    public void whenRegisteringAUserSuccessfully_thenTheUserIsReturned(){
        // Test registering an admin.
        ResponseEntity<UserDto> responseEntity = authenticationController.registerUser(ADMIN_DTO);
        registrationAssertions(responseEntity.getStatusCode(), ADMIN_DTO,
                Objects.requireNonNull(responseEntity.getBody()));
        
        // Test registering a member.
        responseEntity = authenticationController.registerUser(MEMBER_ONE_DTO);
        registrationAssertions(responseEntity.getStatusCode(), MEMBER_ONE_DTO,
                Objects.requireNonNull(responseEntity.getBody()));
    }
    
    private void registrationAssertions(HttpStatusCode statusCode, UserDto userOne, UserDto userTwo){
        assertEquals(HttpStatus.CREATED, statusCode);
        assertEquals(userOne.getEmail(), userTwo.getEmail());
        assertEquals(userOne.getRole(), userTwo.getRole());
    }
    
    @Test
    public void whenRegisteringAndThenAuthenticating_thenTokenIsReturned(){
        // Test authenticating an admin.
        registerThenAuthenticateThenMakeAssertions(ADMIN_DTO, ADMIN_EMAIL, ADMIN_PASSWORD);
        // Test authenticating a member.
        registerThenAuthenticateThenMakeAssertions(MEMBER_ONE_DTO, MEMBER_ONE_EMAIL, MEMBER_ONE_PASSWORD);
    }
    
    private void registerThenAuthenticateThenMakeAssertions(UserDto userDto, String email, String password){
        authenticationController.registerUser(userDto);
        ResponseEntity<JwtResponse> responseEntity = authenticationController.authenticate(new JwtRequest(email,
                password));
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(StringUtils.isNotBlank(Objects.requireNonNull(responseEntity.getBody()).getJwtToken()));
    }
    
    @Test(expected = EmailAlreadyInDatabaseException.class)
    public void whenRegisteringTheSameEmailTwice_thenEmailAlreadyInDatabaseExceptionIsThrown(){
        authenticationController.registerUser(ADMIN_DTO);
        authenticationController.registerUser(ADMIN_DTO);
    }
    
    @Test(expected = BadCredentialsException.class)
    public void whenAuthenticatingWithNonExistentUsername_thenBadCredentialsExceptionIsThrown(){
        authenticationController.registerUser(ADMIN_DTO);
        authenticationController.authenticate(new JwtRequest("memes@memecompany.com", ADMIN_PASSWORD));
    }

    @Test(expected = BadCredentialsException.class)
    public void whenAuthenticatingWithAWrongPassword_thenBadCredentialsExceptionIsThrown(){
        authenticationController.registerUser(ADMIN_DTO);
        authenticationController.authenticate(new JwtRequest(ADMIN_EMAIL, ADMIN_PASSWORD + "memes"));
    }

    /* *********** */
    /* Card Tests */
    /* ********** */

    private void adminLogin(){
        SecurityContextHolder.getContext().setAuthentication(ADMIN_UPAT);
    }
    
    private void memberOneLogin(){
        SecurityContextHolder.getContext().setAuthentication(MEMBER_ONE_UPAT);
    }
    
    private void memberTwoLogin(){
        SecurityContextHolder.getContext().setAuthentication(MEMBER_TWO_UPAT);
    }
    
    /* POST-ing and GET-ing */

    @Test
    public void whenPostingACard_thenTheCardIsReturned(){
        // Test for admins.
        adminLogin();
        CardDto cardDto = CARD_DTOS.get(0);
        ResponseEntity<EntityModel<CardDto>> responseEntity = cardController.postCard(cardDto);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertTrue(cardDtosEqual(cardDto, Objects.requireNonNull(responseEntity.getBody().getContent()))) ;
        
        // Test for members.
        memberOneLogin();
        // We can post the same card no problem, no unique constaints on cards.
        responseEntity = cardController.postCard(cardDto);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertTrue(cardDtosEqual(cardDto,responseEntity.getBody().getContent()));
    }

    @Test
    public void whenPostingACard_thenGettingItByItsGeneratedIdRetrievesTheCard(){
        adminLogin();
        CardDto cardDto = CARD_DTOS.get(0);
        ResponseEntity<EntityModel<CardDto>> postResponse = cardController.postCard(cardDto);
        final Long generatedIdOne = Objects.requireNonNull(postResponse.getBody().getContent()).getId();
        ResponseEntity<EntityModel<CardDto>> getOneResponse = cardController.getCard(generatedIdOne);
        assertEquals(HttpStatus.OK, getOneResponse.getStatusCode());
        CardDto retrievedCard = getOneResponse.getBody().getContent();
        assertTrue(cardDtosEqual(cardDto, retrievedCard));
        memberOneLogin();
        postResponse = cardController.postCard(cardDto);
        final Long generatedIdTwo = Objects.requireNonNull(postResponse.getBody().getContent()).getId();
        ResponseEntity<EntityModel<CardDto>> getTwoResponse = cardController.getCard(generatedIdTwo);
        assertEquals(HttpStatus.OK, getTwoResponse.getStatusCode());
        assertTrue(cardDtosEqual(cardDto, Objects.requireNonNull(getTwoResponse.getBody().getContent())));
    }

    @Test
    public void whenPostingACardWithAStatusNotTODO_thenThatStatusIsIgnored(){
        adminLogin();
        CardDto cardDto = CardDto.builder()
                .name("Test Card")
                .status(CardStatus.IN_PROGRESS)
                .build();
        ResponseEntity<EntityModel<CardDto>> postResponse = cardController.postCard(cardDto);
        
    }

    @Test(expected = InsufficientPrivilegesException.class)
    public void whenPostingACardAsAdmin_andThenLoggingInAMember_thenAccessToTheCardIsForbidden(){

    }

    @Test(expected = InsufficientPrivilegesException.class)
    public void whenPostingACardAsMember_andThenLoggingInAsDifferentMember_thenAccessToTheCardIsForbidden(){

    }

    @Test
    public void whenPostingACardAsMember_andThenLoggingInAsAdmin_thenAccessToTheCardIsAllowed(){

    }

    @Test(expected = CardNotFoundException.class)
    public void whenTryingToGetACardThatHasNotBeenPosted_thenResourceCannotBeFound(){

    }

    /* POST-ing and PUT-ing */

    @Test
    public void whenPostingACard_andThenPuttingADifferentCardOnTheId_thenNewCardIsReturned(){

    }

    @Test
    public void whenPostingACard_andThenPuttingADifferentCardOnTheId_thenGettingTheIdReturnsTheNewCard(){

    }

    @Test(expected = InsufficientPrivilegesException.class)
    public void whenPostingACardAsAdmin_andThenLoggingInAsMember_thenPuttingANewCardOnTheIdIsNotAllowed(){

    }

    @Test(expected = InsufficientPrivilegesException.class)
    public void whenPostingACardAsMember_andThenLoggingInAsDifferentMember_thenPuttingANewCardOnTheIdIsNotAllowed(){

    }

    @Test
    public void whenPostingACardAsMember_andThenLoggingInAsAdmin_thenPuttingANewCardOnTheIdIsAllowed(){

    }

    @Test(expected = CardNotFoundException.class)
    public void whenPuttingACardOnANonExistentId_thenResourceCannotBeFound(){

    }

    /* POST-ing and PATCH-ing */

    @Test
    public void whenPostingACard_andThenApplyingAPatchOnTheId_thenPatchedCardIsReturned(){

    }

    @Test
    public void whenPostingACard_andThenApplyingAPatchOnTheId_thenGettingTheIdReturnsThePatchedCard(){

    }

    @Test(expected = InsufficientPrivilegesException.class)
    public void whenPostingACardAsAdmin_andThenLoggingInAsMember_thenApplyingAPatchOnTheIdIsNotAllowed(){

    }

    @Test(expected = InsufficientPrivilegesException.class)
    public void whenPostingACardAsMember_andThenLoggingInAsDifferentMember_thenApplyingAPatchOnTheIdIsNotAllowed(){

    }

    @Test
    public void whenPostingACardAsMember_andThenLoggingInAsAdmin_thenApplyingAPatchOnTheIdIsAllowed(){

    }

    @Test(expected = CardNotFoundException.class)
    public void whenApplyingAPatchOnANonExistentId_thenResourceCannotBeFound(){

    }

    /* POST-ing and DELETE-ing */

    @Test
    public void whenPostingACard_andThenDeletingIt_thenNoContentResponseIsReturned(){

    }

    @Test(expected = InsufficientPrivilegesException.class)
    public void whenPostingACardAsAdmin_andThenLoggingInAsMember_thenDeletingTheCardIsForbidden(){

    }

    @Test(expected = InsufficientPrivilegesException.class)
    public void whenPostingACardAsMember_andThenLoggingInAsDifferentMember_thenDeletingTheCardIsForbidden(){

    }

    @Test
    public void whenPostingACardAsMember_andThenLoggingInAsAdmin_thenDeletingTheCardIsAllowed(){

    }

    @Test(expected = CardNotFoundException.class)
    public void whenDeletingANonExistentCard_thenResourceCannotBeFound(){

    }

    /* Aggregate GET with pagination / sorting */
    
    private void postAllCards(){
        CARD_DTOS.forEach(cardController::postCard);
    }

    @Test
    public void whenRequestingTheEntireDataset_thenWeGetTheEntireDataset(){
        
    }

    @Test
    public void whenWeSortByAField_andRequestASpecificPage_thenWeGetThatPage(){
        
    }
    
    @Test
    public void whenWeSortByAField_andFilterByAnAttribute_andRequestASpecificPage_thenWeGetThatPage(){
        
    }
    
    @Test
    public void whenWeSortByAField_andFilterByTwoAttributes_andRequestASpecificPage_thenWeGetThatPage(){
        
    }
    @Test
    public void whenNoCardsHaveBeenPosted_thenWeGetAnEmptyList(){
        
    }
    
    @Test(expected = InsufficientPrivilegesException.class)
    public void whenAMemberFiltersByTheNameOfAnotherMember_thenInsufficientPrivilegesExceptionIsThrown(){
        
    }
    
}
