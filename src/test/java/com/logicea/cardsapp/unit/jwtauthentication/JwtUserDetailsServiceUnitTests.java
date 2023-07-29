package com.logicea.cardsapp.unit.jwtauthentication;

import static com.logicea.cardsapp.util.Constants.ADMIN_AUTHORITY;
import static com.logicea.cardsapp.util.Constants.MEMBER_AUTHORITY;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.logicea.cardsapp.model.user.UserDto;
import com.logicea.cardsapp.model.user.UserEntity;
import com.logicea.cardsapp.model.user.UserRole;
import com.logicea.cardsapp.persistence.UserRepository;
import com.logicea.cardsapp.service.jwtauthentication.JwtUserDetailsService;
import java.util.Optional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

@RunWith(MockitoJUnitRunner.class)
public class JwtUserDetailsServiceUnitTests {

  public static final String ADMIN_EMAIL = "admin@company.com";
  public static final String ADMIN_PASSWORD = "adminpass";

  public static final String MEMBER_EMAIL = "plainoljoe@company.com";
  public static final String MEMBER_PASSWORD = "plainoljoepass";
  private static final UserEntity TEST_ADMIN_USER_ENTITY = new UserEntity(ADMIN_EMAIL, ADMIN_PASSWORD, UserRole.ADMIN);
  private static final UserDto TEST_ADMIN_USER_DTO = new UserDto(ADMIN_EMAIL, ADMIN_PASSWORD, UserRole.ADMIN);
  private static final UserEntity TEST_MEMBER_USER_ENTITY = new UserEntity(MEMBER_EMAIL, MEMBER_PASSWORD, UserRole.MEMBER);
  private static final UserDto TEST_MEMBER_USER_DTO = new UserDto(MEMBER_EMAIL, MEMBER_PASSWORD, UserRole.MEMBER);
  @InjectMocks private JwtUserDetailsService jwtUserDetailsService;
  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;

  @Test
  public void whenAdminUserIsInDB_thenAdminUserDetailsReturned() {
    when(userRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(TEST_ADMIN_USER_ENTITY));
    UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(ADMIN_EMAIL);
    assertEquals(userDetails.getUsername(), ADMIN_EMAIL);
    assertEquals(userDetails.getPassword(), ADMIN_PASSWORD);
    assertEquals(CollectionUtils.extractSingleton(userDetails.getAuthorities()), ADMIN_AUTHORITY);
  }

  @Test
  public void whenMemberUserIsInDB_thenMemberUserDetailsReturned() {
    when(userRepository.findByEmail(MEMBER_EMAIL)).thenReturn(Optional.of(TEST_MEMBER_USER_ENTITY));
    UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(MEMBER_EMAIL);
    assertEquals(userDetails.getUsername(), MEMBER_EMAIL);
    assertEquals(userDetails.getPassword(), MEMBER_PASSWORD);
    assertEquals(CollectionUtils.extractSingleton(userDetails.getAuthorities()), MEMBER_AUTHORITY);
  }

  @Test(expected = UsernameNotFoundException.class)
  public void whenUserIsNotInDB_thenUsernameNotFoundExceptionIsThrown() {
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    jwtUserDetailsService.loadUserByUsername(RandomStringUtils.randomAlphanumeric(10));
  }

  @Test
  public void whenSavingNewAdminUser_thenTheirInformationIsReturned() {
    when(passwordEncoder.encode(any(CharSequence.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0)); // Encoder basically does nothing.
    when(userRepository.save(any())).thenReturn(TEST_ADMIN_USER_ENTITY);
    assertEquals(TEST_ADMIN_USER_DTO, jwtUserDetailsService.save(TEST_ADMIN_USER_DTO));
  }

  @Test
  public void whenSavingNewMemberUser_thenTheirInformationIsReturned() {
    when(passwordEncoder.encode(any(CharSequence.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0)); // Encoder basically does nothing.
    when(userRepository.save(any())).thenReturn(TEST_MEMBER_USER_ENTITY);
    assertEquals(TEST_MEMBER_USER_DTO, jwtUserDetailsService.save(TEST_MEMBER_USER_DTO));
  }
  
  @Test
  public void whenSavingNewUserWithTrailingAndLeadingWhitespaceInUsername_thenThatUsernameIsTrimmed(){
    UserDto userDto = new UserDto(" max    ", "maxpassword", UserRole.MEMBER);
    UserDto expectedUserDto = new UserDto("max" , "maxpassword", UserRole.MEMBER);
    when(passwordEncoder.encode(any(CharSequence.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
    when(userRepository.save(any(UserEntity.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
    Assertions.assertEquals(expectedUserDto, jwtUserDetailsService.save(userDto));
  }
}
