package nl.rabobank.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.rabobank.RaboAssignmentApplication;
import nl.rabobank.document.UserDocument;
import nl.rabobank.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private BCryptPasswordEncoder passwordEncoder;

  @MockBean
  private UserRepository userRepository;

  static class LoginRequest {
    public String username;
    public String password;
    public LoginRequest(String u, String p) { username = u; password = p; }
  }

  @Test
  void loginSuccess_returnsTokenAndUsername() throws Exception {
    String rawPwd = "secret";
    UserDocument doc = new UserDocument();
    doc.setUsername("john");
    doc.setPassword(passwordEncoder.encode(rawPwd));
    doc.setRoles(List.of("GRANTOR"));

    Mockito.when(userRepository.findByUsername("john"))
      .thenReturn(Optional.of(doc));

    mockMvc.perform(post("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(new LoginRequest("john", rawPwd))))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.token").isString());
  }

  @Test
  void loginUnknownUser_returns401() throws Exception {
    Mockito.when(userRepository.findByUsername("nosuch"))
      .thenReturn(Optional.empty());

    mockMvc.perform(post("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(new LoginRequest("nosuch", "whatever"))))
      .andExpect(status().isUnauthorized())
      .andExpect(jsonPath("$.error").value("Bad credentials"));
  }

  @Test
  void loginWrongPassword_returns401() throws Exception {
    String raw = "rightpwd";
    UserDocument doc = new UserDocument();
    doc.setUsername("alice");
    doc.setPassword(passwordEncoder.encode(raw));
    doc.setRoles(List.of("GRANTOR"));

    Mockito.when(userRepository.findByUsername("alice"))
      .thenReturn(Optional.of(doc));

    mockMvc.perform(post("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(new LoginRequest("alice", "wrong"))))
      .andExpect(status().isUnauthorized())
      .andExpect(jsonPath("$.error").value("Bad credentials"));
  }

  @Test
  void loginMissingFields_returns401() throws Exception {
    String json = "{\"username\":\"bob\"}";
    mockMvc.perform(post("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
      .andExpect(status().isUnauthorized())
      .andExpect(jsonPath("$.message").exists());
  }
}
