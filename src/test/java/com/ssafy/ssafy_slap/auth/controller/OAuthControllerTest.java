package com.ssafy.ssafy_slap.auth.controller;

import com.ssafy.ssafy_slap.auth.dto.AuthResponse;
import com.ssafy.ssafy_slap.auth.dto.AuthUserResponse;
import com.ssafy.ssafy_slap.auth.oauth.OAuthProvider;
import com.ssafy.ssafy_slap.auth.service.OAuthLoginTicketStore;
import com.ssafy.ssafy_slap.auth.service.OAuthService;
import com.ssafy.ssafy_slap.auth.service.AuthSession;
import com.ssafy.ssafy_slap.auth.service.IssuedRefreshToken;
import com.ssafy.ssafy_slap.global.config.AppFrontendProperties;
import jakarta.servlet.http.Cookie;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
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

    private final AppFrontendProperties frontendProperties = new AppFrontendProperties(
            "http://localhost:5173,http://127.0.0.1:5173,https://ssafyslap.vercel.app",
            "https://ssafyslap.vercel.app"
    );

    @Test
    void startsOAuthAuthorizationOnlyWithPost() throws Exception {
        OAuthService oauthService = mock(OAuthService.class);
        OAuthLoginTicketStore ticketStore = mock(OAuthLoginTicketStore.class);
        when(oauthService.createAuthorizationUrl(eq(OAuthProvider.KAKAO), anyString()))
                .thenReturn("https://kauth.kakao.com/oauth/authorize?state=generated");
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(controller(oauthService, ticketStore))
                .build();

        MvcResult result = mockMvc.perform(post("/api/oauth/kakao/authorize")
                        .header("Origin", "http://localhost:5173"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://kauth.kakao.com/oauth/authorize?state=generated"))
                .andExpect(header().exists("Set-Cookie"))
                .andReturn();
        assertThat(result.getResponse().getHeaders("Set-Cookie"))
                .anySatisfy(cookie -> assertThat(cookie)
                        .contains("slap_oauth_redirect_origin=http://localhost:5173"));

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
        AuthSession session = session(response);
        when(oauthService.login(OAuthProvider.KAKAO, "code-123", "state-123", "state-123"))
                .thenReturn(session);
        when(ticketStore.create(session)).thenReturn("ticket-123");
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(controller(oauthService, ticketStore))
                .build();

        mockMvc.perform(get("/api/oauth/kakao/callback")
                        .param("code", "code-123")
                        .param("state", "state-123")
                        .cookie(new Cookie("slap_oauth_state", "state-123"))
                        .cookie(new Cookie("slap_oauth_redirect_origin", "http://localhost:5173")))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "http://localhost:5173/oauth/callback?ticket=ticket-123"));
    }

    @Test
    void redirectsOAuthCallbackToDeployedFrontendWhenLoginStartedThere() throws Exception {
        OAuthService oauthService = mock(OAuthService.class);
        OAuthLoginTicketStore ticketStore = mock(OAuthLoginTicketStore.class);
        AuthResponse response = new AuthResponse(
                "Bearer",
                "jwt-token",
                new AuthUserResponse(8L, "kakao@example.com", "tester", "USER", false)
        );
        AuthSession session = session(response);
        when(oauthService.login(OAuthProvider.KAKAO, "code-123", "state-123", "state-123"))
                .thenReturn(session);
        when(ticketStore.create(session)).thenReturn("ticket-123");
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(controller(oauthService, ticketStore))
                .build();

        mockMvc.perform(get("/api/oauth/kakao/callback")
                        .param("code", "code-123")
                        .param("state", "state-123")
                        .cookie(new Cookie("slap_oauth_state", "state-123"))
                        .cookie(new Cookie("slap_oauth_redirect_origin", "https://ssafyslap.vercel.app")))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://ssafyslap.vercel.app/oauth/callback?ticket=ticket-123"));
    }

    @Test
    void redirectsOAuthFailureWithoutUrlFragment() throws Exception {
        OAuthService oauthService = mock(OAuthService.class);
        OAuthLoginTicketStore ticketStore = mock(OAuthLoginTicketStore.class);
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(controller(oauthService, ticketStore))
                .build();

        mockMvc.perform(get("/api/oauth/kakao/callback")
                        .param("error", "access_denied")
                        .cookie(new Cookie("slap_oauth_redirect_origin", "http://localhost:5173")))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "http://localhost:5173/oauth/callback?error=oauth_failed"));
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
        when(ticketStore.consume("ticket-123")).thenReturn(session(response));
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(controller(oauthService, ticketStore))
                .build();

        mockMvc.perform(post("/api/oauth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ticket\":\"ticket-123\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.user.userId").value(8))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("HttpOnly")));
    }

    private OAuthController controller(OAuthService oauthService, OAuthLoginTicketStore ticketStore) {
        return new OAuthController(
                oauthService,
                ticketStore,
                frontendProperties,
                new RefreshTokenCookie(true, "None")
        );
    }

    private AuthSession session(AuthResponse response) {
        return new AuthSession(
                response,
                new IssuedRefreshToken("refresh-token", Instant.now().plusSeconds(3600))
        );
    }
}
