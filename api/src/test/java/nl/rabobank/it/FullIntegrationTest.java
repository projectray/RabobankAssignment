package nl.rabobank.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.rabobank.account.PaymentAccount;
import nl.rabobank.authorizations.Authorization;
import nl.rabobank.document.PoaDocument;
import nl.rabobank.document.UserDocument;
import nl.rabobank.dto.LoginRequest;
import nl.rabobank.dto.PoaRequest;
import nl.rabobank.repository.PoaRepository;
import nl.rabobank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FullIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PoaRepository poaRepository;

  @Autowired
  private ObjectMapper objectMapper;

  @BeforeEach
  void setupTestData() {
    userRepository.deleteAll();
    poaRepository.deleteAll();

    userRepository.save(UserDocument.builder()
      .username("testuser")
      .password(new BCryptPasswordEncoder().encode("secret"))
      .roles(List.of("ROLE_GRANTOR"))
      .build());

    poaRepository.save(PoaDocument.builder()
      .grantorName("grantor123")
      .granteeName("grantee456")
      .account(PaymentAccount.builder()
        .accountNumber("NL12RABO0123456789")
        .accountHolderName("grantor123")
        .balance(5.0)
        .build())
      .authorization(Authorization.READ)
      .build());
  }

  @Test
  void testLoginAndGrantorAccess() throws Exception {
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setUsername("testuser");
    loginRequest.setPassword("secret");

    String loginResp = mockMvc.perform(post("/auth/login")
        .contentType("application/json")
        .content(objectMapper.writeValueAsString(loginRequest)))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();

    String token = objectMapper.readTree(loginResp).get("token").asText();

    PoaRequest poaRequest = new PoaRequest();
    poaRequest.setGrantor("grantor123");
    poaRequest.setGrantee("grantee456");
    poaRequest.setAccountNumber("NL12RABO0123456789");
    poaRequest.setAccountHolderName("grantor123");
    poaRequest.setAccountType("PAYMENT");
    poaRequest.setInitialBalance(5.0);
    poaRequest.setAuthorizationType("READ");

    mockMvc.perform(post("/api/poa")
        .header("Authorization", "Bearer " + token)
        .contentType("application/json")
        .content(objectMapper.writeValueAsString(poaRequest)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.authorization").value("READ"))
      .andExpect(jsonPath("$.grantorName").value("grantor123"))
      .andExpect(jsonPath("$.granteeName").value("grantee456"));
  }
}
