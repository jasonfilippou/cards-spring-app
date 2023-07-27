package com.logicea.cardsapp.service.jwtauthentication;

import static com.logicea.cardsapp.util.Constants.AUTH_HEADER_BEARER_PREFIX;

import com.logicea.cardsapp.config.JwtAuthenticationEntryPoint;
import com.logicea.cardsapp.util.JwtTokenUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * A {@link OncePerRequestFilter} that filters every incoming request to make sure that it is properly authenticated with an unexpired
 * JWT token.
 * @author jason 
 * @see JwtTokenUtil
 * @see JwtAuthenticationService
 * @see JwtAuthenticationEntryPoint
 */
@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {
  private final JwtUserDetailsService jwtUserDetailsService;
  private final JwtTokenUtil jwtTokenUtil;

  @Override
  protected void doFilterInternal(
          HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    // Get the token
    final String requestTokenHeader = request.getHeader("Authorization");
    String username = null;
    String jwtToken = null;
    if (requestTokenHeader != null && requestTokenHeader.startsWith(AUTH_HEADER_BEARER_PREFIX)) {
      jwtToken = requestTokenHeader.substring(AUTH_HEADER_BEARER_PREFIX.length());
      try {
        // Get the username from the token.
        username = jwtTokenUtil.getUsernameFromToken(jwtToken);
      } catch (IllegalArgumentException e) {
        logger.warn("Unable to get JWT Token");
      } catch (ExpiredJwtException e) {
        logger.warn("JWT Token has expired");
      }
    } else { // Token not found
      logger.warn("JWT Token does not begin with \"" + AUTH_HEADER_BEARER_PREFIX + "\" string");
    }
    // Validate the username and password
    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
      UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(username);
      // If token is valid, configure Spring Security to manually set authentication
      if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        usernamePasswordAuthenticationToken.setDetails(
            new WebAuthenticationDetailsSource().buildDetails(request));
        // After setting the Authentication in the context, we specify
        // that the current user is authenticated. So it passes the
        // Spring Security Configurations successfully.
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
      }
    }
    filterChain.doFilter(request, response);
  }
}
