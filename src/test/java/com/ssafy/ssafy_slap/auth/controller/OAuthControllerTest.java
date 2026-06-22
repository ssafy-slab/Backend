package com.ssafy.ssafy_slap.auth.controller;

import com.ssafy.ssafy_slap.auth.dto.AuthResponse;
import com.ssafy.ssafy_slap.auth.dto.AuthUserResponse;
import com.ssafy.ssafy_slap.auth.oauth.OAuthProvider;
import com.ssafy.ssafy_slap.auth.service.OAuthLoginTicketStore;
import com.ssafy.ssafy_slap.auth.service.OAuthService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OAuthControllerTest {

    @Test
    void startsOAuthAuthorizationOnlyWithPost() throws Exception {
        OAuthService oauthService = mock(OAuthService.class);
        OAuthLoginTicketStore ticketStore = mock(OAuthLoginTicketStore.class);
        when(oauthService.createAuthorizationUrl(eq(OAuthProvider.KAKAO), anyString()))
                .thenReturn("https://kauth.kakao.com/oauth/authorize?state=generated");
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new OAuthController(oauthService, ticketStore)).build();

        mockMvc.perform(post("/api/oauth/kakao/authorize"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://kauth.kakao.com/oauth/authorize?state=generated"))
                .andExpect(header().exists("Set-Cookie"));

        mockMvc.perform(get("/api/oauth/kakao/authorize"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void redirectsOAuthCallbackWithTicketOnly() throws Exception {
        OAuthService oauthService = mock(OAuthService.class);
        OAuthLoginTicketStore ticketStore = mock(OAuthLoginTicketStore.class);
        AuthResponse response = new AuthResponse(
                "Bearer",
                "jwt-token",
                new AuthUserResponse(8L, "kakao@example.com", "tester", "USER", false)
        );
        when(oauthService.login(OAuthProvider.KAKAO, "code-123", "state-123", "state-123"))
                .thenReturn(response);
        when(oauthService.frontendRedirectUri()).thenReturn("http://localhost:5173/oauth/callback");
        when(ticketStore.create(response)).thenReturn("ticket-123");
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new OAuthController(oauthService, ticketStore)).build();

        mockMvc.perform(get("/api/oauth/kakao/callback")
                        .param("code", "code-123")
                        .param("state", "state-123")
                        .cookie(new jakarta.servlet.http.Cookie("slap_oauth_state", "state-123")))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "http://localhost:5173/oauth/callback?ticket=ticket-123"));
    }

    @Test
    void exchangesOAuthTicketForAuthResponse() throws Exception {
        OAuthService oauthService = mock(OAuthService.class);
        OAuthLoginTicketStore ticketStore = mock(OAuthLoginTicketStore.class);
        AuthResponse response = new AuthResponse(
                "Bearer",
                "jwt-token",
                new AuthUserResponse(8L, "kakao@example.com", "tester", "USER", false)
        );
        when(ticketStore.consume("ticket-123")).thenReturn(response);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new OAuthController(oauthService, ticketStore)).build();

        mockMvc.perform(post("/api/oauth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ticket\":\"ticket-123\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.user.userId").value(8));
    }
}
