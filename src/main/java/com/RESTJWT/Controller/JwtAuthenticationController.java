package com.RESTJWT.Controller;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

import com.RESTJWT.service.CordaRPCConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.RESTJWT.service.JwtUserDetailsService;
import com.RESTJWT.config.JwtTokenUtil;
import com.RESTJWT.model.JwtRequest;
import com.RESTJWT.model.JwtResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin
public class JwtAuthenticationController {
    @Autowired
    CordaRPCConnectionService cordaRPCConnectionService;

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private JwtUserDetailsService userDetailsService;
    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {
        authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(authenticationRequest.getUsername());
        final String token = jwtTokenUtil.generateToken(userDetails);
        Date expirationDateFromToken = jwtTokenUtil.getExpirationDateFromToken(token);
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(expirationDateFromToken.toInstant(), ZoneId.of("Asia/Tokyo"));
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        String format = dateTimeFormatter.format(zonedDateTime);
        cordaRPCConnectionService.createConnection();
        return ResponseEntity.ok(new JwtResponse(token, format));
    }
    private void authenticate(String username, String password) throws Exception {
        try {
            Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            SecurityContextHolder.getContext().setAuthentication(authenticate);
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }

    @PostMapping(value = "/disconnect")
    public ResponseEntity<?> disconnect(HttpServletRequest request) throws ServletException {
        cordaRPCConnectionService.closeConnection();
        request.logout();
        return ResponseEntity.ok("Logout Successful!" +cordaRPCConnectionService.currentUserLIst());
    }
}