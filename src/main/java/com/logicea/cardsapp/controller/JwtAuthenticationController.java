package com.logicea.cardsapp.controller;

import com.logicea.cardsapp.model.jwt.JwtRequest;
import com.logicea.cardsapp.model.jwt.JwtResponse;
import com.logicea.cardsapp.model.user.UserDto;
import com.logicea.cardsapp.service.jwtauthentication.JwtAuthenticationService;
import com.logicea.cardsapp.service.jwtauthentication.JwtUserDetailsService;
import com.logicea.cardsapp.util.JwtTokenUtil;
import com.logicea.cardsapp.util.logger.Logged;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for user authentication. Provides endpoints for authentication and login.
 *
 * @author jason
 */
@RestController
@RequestMapping("/cardsapi")
@CrossOrigin
@RequiredArgsConstructor
@Tag(name = "1. Authentication API")
@Validated
@Logged
public class JwtAuthenticationController {

  private final JwtTokenUtil jwtTokenUtil;
  private final JwtUserDetailsService userDetailsService;
  private final JwtAuthenticationService jwtAuthenticationService;

  /**
   * POST endpoint for JWT user authentication.
   *
   * @param authenticationRequest An instance of {@link JwtRequest} containing the user's username
   *     and password. The password is stored in the database in encrypted format.
   * @return A {@link ResponseEntity} over {@link JwtResponse} instances.
   */
  @Operation(summary = "Authenticate with your username and password")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Authentication successful, JWT returned.",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = JwtResponse.class))
            }),
        @ApiResponse(responseCode = "401", description = "Bad password.", content = @Content),
        @ApiResponse(responseCode = "404", description = "Username not found.", content = @Content)
      })
  @PostMapping(value = "/authenticate")
  public ResponseEntity<JwtResponse> authenticate(
      @RequestBody @Valid JwtRequest authenticationRequest) {

    jwtAuthenticationService.authenticate(
        authenticationRequest.getEmail(), authenticationRequest.getPassword());
    final UserDetails userDetails =
        userDetailsService.loadUserByUsername(authenticationRequest.getEmail());
    final String token = jwtTokenUtil.generateToken(userDetails);
    return ResponseEntity.ok(new JwtResponse(token));
  }

  /**
   * A POST endpoint for registering users.
   *
   * @param user An instance of {@link UserDto} containing a username and password for the user. The
   *     password will be stored in encrypted format in the database.
   * @return An instance of {@link ResponseEntity} over a {@link UserDto} instance.
   */
  @Operation(summary = "Register with your username and password")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Registration successful.",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = UserDto.class))
            }),
        @ApiResponse(
            responseCode = "400",
            description =
                "Invalid password length provided; passwords should be from 8 to 30 characters.",
            content = @Content),
        @ApiResponse(
            responseCode = "409",
            description = "Username already taken.",
            content = @Content)
      })
  @PostMapping(value = "/register")
  public ResponseEntity<UserDto> registerUser(@RequestBody @Valid UserDto user) {
    return new ResponseEntity<>(userDetailsService.save(user), HttpStatus.CREATED);
  }
}
