package com.isedol_clip_backend.web.controller;

import com.isedol_clip_backend.auth.JwtTokenProvider;
import com.isedol_clip_backend.exception.NoExistedDataException;
import com.isedol_clip_backend.util.MakeResp;
import com.isedol_clip_backend.web.entity.AccountEntity;
import com.isedol_clip_backend.web.model.response.CommonResponse;
import com.isedol_clip_backend.web.service.AccountService;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AccountService accountService;

    @GetMapping("/verify")
    public ResponseEntity<CommonResponse> verify(HttpServletRequest request) {
//        String jwt = (String) request.getAttribute("jwt");
//        log.info("jwt: {}", jwt);
//        try {
//            JwtTokenProvider.getTokenClaims(jwt);
//        } catch (ExpiredJwtException e) {
//            log.info("bad request");
//            return MakeResp.make(HttpStatus.BAD_REQUEST, "Need refresh access token");
//        } catch(Exception e) {
//            log.info("unauthorized");
//            return MakeResp.make(HttpStatus.UNAUTHORIZED, "Need Login");
//        }
//        ResponseEntity<CommonResponse> errorResponse = getErrorResponse(jwt);
//        if(errorResponse != null) {
//            return errorResponse;
//        }

        return MakeResp.make(HttpStatus.OK, "USER");

    }

    @GetMapping("/refresh")
    public ResponseEntity<CommonResponse> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
//        String jwt = (String) request.getAttribute("jwt");
//
//        ResponseEntity<CommonResponse> errorResponse = getErrorResponse(jwt);
//        if(errorResponse != null) {
//            return errorResponse;
//        }

        long id = getId();

        AccountEntity entity;
        try {
            entity = accountService.getById(id);
        } catch (NoExistedDataException e) {
            return MakeResp.make(HttpStatus.INTERNAL_SERVER_ERROR, "Not Existed Account Id");
        }

        String refreshToken = entity.getRefreshToken();

        try {
            JwtTokenProvider.getTokenClaims(refreshToken);
        } catch (ExpiredJwtException e) {
            return MakeResp.make(HttpStatus.BAD_REQUEST, "Expired refresh token. Need Login");
        } catch (Exception e) {
            return MakeResp.make(HttpStatus.UNAUTHORIZED, "Need Login");
        }

        String accessToken = JwtTokenProvider.generateUserToken(id);
        response.setHeader("Authorization", "Bearer " + accessToken);

        return MakeResp.make(HttpStatus.OK, "Success");
    }

    @GetMapping("/logout")
    public ResponseEntity<CommonResponse> logout(HttpServletRequest request, HttpServletResponse response) {
//        String jwt = (String) request.getAttribute("jwt");
//        try {
//            JwtTokenProvider.getTokenClaims(jwt);
//        } catch (Exception e) {
//            return MakeResp.make(HttpStatus.OK, "Success");
//        }

        long id = getId();

        try {
            AccountEntity entity = accountService.getById(id);
            entity.setRefreshToken(null);
            entity.setTwitchAccessToken(null);
            entity.setTwitchRefreshToken(null);

            accountService.save(entity);
        } catch (NoExistedDataException e) {
            return MakeResp.make(HttpStatus.INTERNAL_SERVER_ERROR, "Not Existed Account Id");
        }

        return MakeResp.make(HttpStatus.OK, "Success");
    }

//    private boolean isValidToken(String token) {
//        try {
//            JwtTokenProvider.getTokenClaims(token);
//        } catch (Exception e) {
//            return false;
//        }
//
//        return true;
//    }

//    private ResponseEntity<CommonResponse> getErrorResponse(String token) {
//        try {
//            JwtTokenProvider.getTokenClaims(token);
//        } catch (ExpiredJwtException e) {
//            return MakeResp.make(HttpStatus.UNAUTHORIZED, "Need refresh access token");
//        } catch (Exception e) {
//            return MakeResp.make(HttpStatus.BAD_REQUEST, "Need Login");
//        }
//
//        return null;
//    }

    private long getId() {
        String id = SecurityContextHolder.getContext().getAuthentication().getName();
        return Long.parseLong(id);
    }
}
