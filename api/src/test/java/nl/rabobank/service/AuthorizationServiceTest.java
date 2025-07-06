package nl.rabobank.service;

import nl.rabobank.account.PaymentAccount;
import nl.rabobank.authorizations.Authorization;
import nl.rabobank.dto.AccessAccounts;
import nl.rabobank.dto.PoaRequest;
import nl.rabobank.dto.PoaResponse;
import nl.rabobank.exception.AccountAccessNotFoundException;
import nl.rabobank.exception.BadRequestException;
import nl.rabobank.exception.PoaExistException;
import nl.rabobank.mapper.PoaMapper;
import nl.rabobank.document.PoaDocument;
import nl.rabobank.repository.PoaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class AuthorizationServiceTest {

  @Mock
  private PoaRepository repository;

  @Mock
  private PoaMapper mapper;

  @InjectMocks
  private AuthorizationService service;

  private PoaRequest validRequest;
  private PoaDocument sampleDocument;
  private PoaResponse sampleResponse;

  @BeforeEach
  void setUp() {
    validRequest = PoaRequest.builder()
      .grantor("alice")
      .grantee("bob")
      .accountType("PAYMENT")
      .accountHolderName("alice")
      .accountNumber("ACC123")
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
  void grantAccess_sameGrantorAndGrantee_throwsBadRequest() {
    PoaRequest req = PoaRequest.builder()
      .grantor("alice")
      .grantee("alice")
      .accountNumber("ACC123")
      .authorizationType("READ")
      .build();

    assertThatThrownBy(() -> service.grantAccess(req))
      .isInstanceOf(BadRequestException.class)
      .hasMessage("Grantor user cannot be same as grantee user");

    verifyNoInteractions(repository, mapper);
  }

  @Test
  void grantAccess_alreadyExists_throwsExistException() {
    when(repository.existsByGranteeNameAndAuthorizationAndAccount_AccountNumber(
      eq("bob"), any(), eq("ACC123")))
      .thenReturn(true);

    assertThatThrownBy(() -> service.grantAccess(validRequest))
      .isInstanceOf(PoaExistException.class)
      .hasMessageContaining("is already existed");

    verify(repository).existsByGranteeNameAndAuthorizationAndAccount_AccountNumber(
      eq("bob"), any(), eq("ACC123"));
    verifyNoMoreInteractions(repository);
    verifyNoInteractions(mapper);
  }

  @Test
  void grantAccess_validRequest_returnsMappedResponse() {
    when(repository.existsByGranteeNameAndAuthorizationAndAccount_AccountNumber(
      eq("bob"), any(), eq("ACC123")))
      .thenReturn(false);
    when(mapper.mapToDocument(validRequest)).thenReturn(sampleDocument);
    when(repository.save(sampleDocument)).thenReturn(sampleDocument);
    when(mapper.mapToResponse(sampleDocument)).thenReturn(sampleResponse);

    PoaResponse resp = service.grantAccess(validRequest);

    assertThat(resp).isEqualTo(sampleResponse);

    InOrder inOrder = inOrder(repository, mapper);
    inOrder.verify(repository)
      .existsByGranteeNameAndAuthorizationAndAccount_AccountNumber(
        eq("bob"), any(), eq("ACC123"));
    inOrder.verify(mapper).mapToDocument(validRequest);
    inOrder.verify(repository).save(sampleDocument);
    inOrder.verify(mapper).mapToResponse(sampleDocument);
  }

  @Test
  void getAllAccessAccount_noRecords_throwsNotFound() {
    when(repository.findAllByGranteeNameIgnoreCase("ray"))
      .thenReturn(Collections.emptyList());

    assertThatThrownBy(() -> service.getAllAccessAccounts("ray"))
      .isInstanceOf(AccountAccessNotFoundException.class)
      .hasMessage("No access accounts found for user ray");

    verify(repository).findAllByGranteeNameIgnoreCase("ray");
    verifyNoInteractions(mapper);
  }

  @Test
  void getAllAccessAccount_withRecords_returnsMappedList() {
    PoaDocument doc1 = sampleDocument.toBuilder()
      .granteeName("ray")
      .build();

    PoaDocument doc2 = sampleDocument.toBuilder()
      .granteeName("ray")
      .authorization(Authorization.WRITE)
      .build();

    AccessAccounts acct1 = AccessAccounts.builder()
      .accountNumber("ACC123").authorizationType("READ").build();
    AccessAccounts acct2 = AccessAccounts.builder()
      .accountNumber("ACC123").authorizationType("WRITE").build();

    when(repository.findAllByGranteeNameIgnoreCase("ray"))
      .thenReturn(Arrays.asList(doc1, doc2));
    when(mapper.mapToAccessAccount(doc1)).thenReturn(acct1);
    when(mapper.mapToAccessAccount(doc2)).thenReturn(acct2);

    List<AccessAccounts> result = service.getAllAccessAccounts("ray");
    assertThat(result).containsExactly(acct1, acct2);

    verify(repository).findAllByGranteeNameIgnoreCase("ray");
    verify(mapper).mapToAccessAccount(doc1);
    verify(mapper).mapToAccessAccount(doc2);
  }
}
