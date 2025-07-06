package nl.rabobank.mapper;


import nl.rabobank.account.Account;
import nl.rabobank.account.PaymentAccount;
import nl.rabobank.account.SavingsAccount;
import nl.rabobank.authorizations.Authorization;
import nl.rabobank.document.PoaDocument;
import nl.rabobank.dto.AccessAccounts;
import nl.rabobank.dto.PoaRequest;
import nl.rabobank.dto.PoaResponse;
import org.springframework.stereotype.Component;

@Component
public class PoaMapper {
  public PoaResponse mapToResponse(PoaDocument pofDocument) {
    return PoaResponse.builder()
      .id(pofDocument.getId())
      .grantor(pofDocument.getGrantorName())
      .grantee(pofDocument.getGranteeName())
      .accountNumber(pofDocument.getAccount().getAccountNumber())
      .accountType(mapAccountType(pofDocument.getAccount()))
      .authorizationType(pofDocument.getAuthorization().toString())
      .build();
  }

  public PoaDocument mapToDocument(PoaRequest poaRequest) {

    Account account;
    if (poaRequest.getAccountType().equalsIgnoreCase("PAYMENT")) {
      account = PaymentAccount.builder()
        .accountNumber(poaRequest.getAccountNumber())
        .accountHolderName(poaRequest.getAccountHolderName())
        .balance(poaRequest.getInitialBalance())
        .build();
    } else {
      account = SavingsAccount.builder()
        .accountNumber(poaRequest.getAccountNumber())
        .accountHolderName(poaRequest.getAccountHolderName())
        .balance(poaRequest.getInitialBalance())
        .build();
    }
    return PoaDocument.builder()
      .grantorName(poaRequest.getGrantor())
      .granteeName(poaRequest.getGrantee())
      .account(account)
      .authorization(Authorization.valueOf(poaRequest.getAuthorizationType().toUpperCase()))
      .build();
  }

  public AccessAccounts mapToAccessAccount(PoaDocument pofDocument) {
    return AccessAccounts.builder()
      .grantor(pofDocument.getGrantorName())
      .accountNumber(pofDocument.getAccount().getAccountNumber())
      .accountHolderName(pofDocument.getAccount().getAccountHolderName())
      .accountType(mapAccountType(pofDocument.getAccount()))
      .authorizationType(pofDocument.getAuthorization().toString())
      .build();
  }

  private String mapAccountType(Account account) {
    return account instanceof PaymentAccount ? "PAYMENT" : "SAVING";
  }
}
