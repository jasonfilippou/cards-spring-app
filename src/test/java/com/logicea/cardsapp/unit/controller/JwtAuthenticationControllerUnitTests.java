package com.logicea.cardsapp.unit.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.logicea.cardsapp.controller.JwtAuthenticationController;
import com.logicea.cardsapp.model.jwt.JwtRequest;
import com.logicea.cardsapp.model.jwt.JwtResponse;
import com.logicea.cardsapp.model.user.UserDto;
import com.logicea.cardsapp.model.user.UserRole;
import com.logicea.cardsapp.service.jwtauthentication.JwtAuthenticationService;
import com.logicea.cardsapp.service.jwtauthentication.JwtUserDetailsService;
import com.logicea.cardsapp.util.JwtTokenUtil;
import com.logicea.cardsapp.util.TestUserDetailsImpl;
import java.util.Objects;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for {@link JwtAuthenticationController}. Make extensive use of Mockito and jUnit4 assertions.
 *
 * @author jason
 */
@RunWith(MockitoJUnitRunner.class)
public class JwtAuthenticationControllerUnitTests {

  private static final TestUserDetailsImpl TEST_USER_DETAILS =
      new TestUserDetailsImpl("username", "password");
  private static final JwtRequest TEST_JWT_REQUEST = new JwtRequest("username", "password");
  @InjectMocks private JwtAuthenticationController jwtAuthenticationController;
  @Mock private JwtTokenUtil jwtTokenUtil;
  @Mock private JwtUserDetailsService userDetailsService;
  @Mock private JwtAuthenticationService jwtAuthenticationService;

  @Test
  public void whenUserIsAuthenticatedInDB_thenReturnNewToken() throws Exception {
    doNothing().when(jwtAuthenticationService).authenticate(anyString(), anyString());
    when(userDetailsService.loadUserByUsername(anyString())).thenReturn(TEST_USER_DETAILS);
    when(jwtTokenUtil.generateToken(TEST_USER_DETAILS)).thenReturn("token");
    assertEquals(
        Objects.requireNonNull(
            jwtAuthenticationController.authenticate(TEST_JWT_REQUEST).getBody()),
        new JwtResponse("token"));
  }

  @Test
  public void whenUserRegistersWithAUsernameWithLeadingAndTrailingWhitespace_thenReturnedUserDetailsHasTheUsernameTrimmed(){
    UserDto userDto = new UserDto(" max    ", "maxpassword",  UserRole.ADMIN);
    UserDto expectedUserDto = new UserDto("max" , "maxpassword", UserRole.ADMIN); // The controller does not actually ever return the password, but that's fine for this unit test.
    when(userDetailsService.save(userDto)).thenAnswer(invocationOnMock -> { 
        UserDto providedUserDto = invocationOnMock.getArgument(0);
        return new UserDto(providedUserDto.getEmail().trim(), providedUserDto.getPassword(), providedUserDto.getRole());
      });
    assertEquals(new ResponseEntity<>(expectedUserDto, HttpStatus.CREATED), 
            jwtAuthenticationController.registerUser(userDto));
  }
}
