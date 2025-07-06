package nl.rabobank.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.rabobank.RaboAssignmentApplication;
import nl.rabobank.account.PaymentAccount;
import nl.rabobank.authorizations.Authorization;
import nl.rabobank.document.PoaDocument;
import nl.rabobank.dto.PoaRequest;
import nl.rabobank.dto.PoaResponse;
import nl.rabobank.mapper.PoaMapper;
import nl.rabobank.repository.PoaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@EnableAutoConfiguration(exclude = {
  MongoAutoConfiguration.class,
  EmbeddedMongoAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
class PoaIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private PoaRepository repository;

  @MockBean
  private PoaMapper mapper;

  private PoaRequest validRequest;
  private PoaDocument sampleDocument;
  private PoaResponse sampleResponse;

  @BeforeEach
  void setUp() {
    validRequest = PoaRequest.builder()
      .grantor("alice")
      .grantee("bob")
      .accountNumber("ACC123")
      .accountHolderName("alice")
      .accountType("PAYMENT")
      .initialBalance(5.0)
      .authorizationType("READ")
      .build();

    sampleDocument = PoaDocument.builder()
      .grantorName("alice")
      .granteeName("bob")
      .account(PaymentAccount.builder()
        .accountHolderName("alice")
        .accountNumber("ACC123")
        .balance(5.0)
        .build())
      .authorization(Authorization.READ)
      .build();

    sampleResponse = PoaResponse.builder()
      .grantor("alice")
      .grantee("bob")
      .accountNumber("ACC123")
      .authorizationType("READ")
      .build();
  }

  @Test
  void grantAccess_success() throws Exception {
    Mockito.when(repository.existsByGranteeNameAndAuthorizationAndAccount_AccountNumber(
        eq("bob"), any(), eq("ACC123")))
      .thenReturn(false);
    Mockito.when(mapper.mapToDocument(validRequest))
      .thenReturn(sampleDocument);
    Mockito.when(repository.save(sampleDocument))
      .thenReturn(sampleDocument);
    Mockito.when(mapper.mapToResponse(sampleDocument))
      .thenReturn(sampleResponse);

    mockMvc.perform(post("/api/poa")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(validRequest)))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.grantee").value("bob"))
      .andExpect(jsonPath("$.authorizationType").value("READ"));

    InOrder o = Mockito.inOrder(repository, mapper);
    o.verify(repository).existsByGranteeNameAndAuthorizationAndAccount_AccountNumber(
      eq("bob"), any(), eq("ACC123"));
    o.verify(mapper).mapToDocument(validRequest);
    o.verify(repository).save(sampleDocument);
    o.verify(mapper).mapToResponse(sampleDocument);
  }

  @Test
  void grantAccess_badRequest_whenSameGrantorAndGrantee() throws Exception {
    PoaRequest req = validRequest.toBuilder()
      .grantee("alice").build();

    mockMvc.perform(post("/api/poa")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(req)))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.message")
        .value("Grantor user cannot be same as grantee user"));
  }

  @Test
  void grantAccess_conflict_whenAlreadyExists() throws Exception {
    Mockito.when(repository.existsByGranteeNameAndAuthorizationAndAccount_AccountNumber(
        eq("bob"), any(), eq("ACC123")))
      .thenReturn(true);

    mockMvc.perform(post("/api/poa")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(validRequest)))
      .andExpect(status().isConflict())
      .andExpect(jsonPath("$.message")
        .value("Authorization request for the grantee user bob is already existed"));
  }

  @Test
  void grantAccess_validationError_onMissingField() throws Exception {
    String json = "{\"grantor\":\"alice\",\"grantee\":\"bob\",\"authorizationType\":\"READ\"}";

    mockMvc.perform(post("/api/poa")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.message").exists());
  }

}
