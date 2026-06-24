package com.ssafy.ssafy_slap.auth.controller;

import com.ssafy.ssafy_slap.auth.dto.AuthResponse;
import com.ssafy.ssafy_slap.auth.dto.AuthUserResponse;
import com.ssafy.ssafy_slap.auth.service.AuthService;
import com.ssafy.ssafy_slap.auth.service.AuthSession;
import com.ssafy.ssafy_slap.auth.service.AuthSessionService;
import com.ssafy.ssafy_slap.auth.service.IssuedRefreshToken;
import com.ssafy.ssafy_slap.auth.service.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    @Test
    void loginReturnsAccessTokenAndSetsRefreshTokenOnlyInHttpOnlyCookie() throws Exception {
        AuthService authService = mock(AuthService.class);
        AuthSession session = session("access-token", "refresh-token");
        when(authService.login(org.mockito.ArgumentMatchers.any(com.ssafy.ssafy_slap.auth.dto.LoginRequest.class)))
                .thenReturn(session);
        MockMvc mvc = mvc(authService, mock(AuthSessionService.class), mock(RefreshTokenService.class));

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"password\":\"password123!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("slap_refresh_token=refresh-token"),
                        org.hamcrest.Matchers.containsString("HttpOnly"),
                        org.hamcrest.Matchers.containsString("Secure"),
                        org.hamcrest.Matchers.containsString("SameSite=None")
                )));
    }

    @Test
    void refreshRotatesCookieAndReturnsNewAccessToken() throws Exception {
        AuthSessionService sessionService = mock(AuthSessionService.class);
        when(sessionService.refresh("old-refresh")).thenReturn(session("new-access", "new-refresh"));
        MockMvc mvc = mvc(mock(AuthService.class), sessionService, mock(RefreshTokenService.class));

        mvc.perform(post("/api/auth/refresh")
                        .header("X-Refresh-Request", "true")
                        .cookie(new jakarta.servlet.http.Cookie(RefreshTokenCookie.NAME, "old-refresh")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"))
                .andExpect(header().string("Set-Cookie",
                        org.hamcrest.Matchers.containsString("slap_refresh_token=new-refresh")));
    }

    @Test
    void refreshRejectsRequestsWithoutCsrfGuardHeader() throws Exception {
        MockMvc mvc = mvc(mock(AuthService.class), mock(AuthSessionService.class), mock(RefreshTokenService.class));

        mvc.perform(post("/api/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie(RefreshTokenCookie.NAME, "old-refresh")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void logoutRevokesRefreshTokenAndExpiresCookie() throws Exception {
        RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
        MockMvc mvc = mvc(mock(AuthService.class), mock(AuthSessionService.class), refreshTokenService);

        mvc.perform(post("/api/auth/logout")
                        .header("X-Refresh-Request", "true")
                        .cookie(new jakarta.servlet.http.Cookie(RefreshTokenCookie.NAME, "refresh-token")))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=0")));

        verify(refreshTokenService).revoke("refresh-token");
    }

    private MockMvc mvc(
            AuthService authService,
            AuthSessionService authSessionService,
            RefreshTokenService refreshTokenService
    ) {
        return MockMvcBuilders.standaloneSetup(new AuthController(
                authService,
                authSessionService,
                refreshTokenService,
                new RefreshTokenCookie(true, "None")
        )).build();
    }

    private AuthSession session(String accessToken, String refreshToken) {
        return new AuthSession(
                new AuthResponse(
                        "Bearer",
                        accessToken,
                        new AuthUserResponse(7L, "test@example.com", "tester", "USER", false)
                ),
                new IssuedRefreshToken(refreshToken, Instant.now().plusSeconds(3600))
        );
    }
}
