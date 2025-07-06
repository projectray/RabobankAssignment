package nl.rabobank.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.rabobank.authorizations.Authorization;
import nl.rabobank.document.PoaDocument;
import nl.rabobank.dto.AccessAccounts;
import nl.rabobank.dto.PoaRequest;
import nl.rabobank.dto.PoaResponse;
import nl.rabobank.exception.AccountAccessNotFoundException;
import nl.rabobank.exception.BadRequestException;
import nl.rabobank.exception.PoaExistException;
import nl.rabobank.mapper.PoaMapper;
import nl.rabobank.repository.PoaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthorizationService {
  private final PoaRepository poaRepository;
  private final PoaMapper mapper;

  public PoaResponse grantAccess(PoaRequest poaRequest) {

    log.info("Grantor give access to grantee");

    if (poaRequest.getGrantor().equals(poaRequest.getGrantee())) {
      log.error("Illegal Request: User request to give access to him/herself, grantor name: {}, grantee name: {} ", poaRequest.getGrantor(), poaRequest.getGrantee());
      throw new BadRequestException("Grantor user cannot be same as grantee user");
    }

    if (poaRepository.existsByGranteeNameAndAuthorizationAndAccount_AccountNumber(poaRequest.getGrantee(),
      Authorization.valueOf(poaRequest.getAuthorizationType().toUpperCase()),
      poaRequest.getAccountNumber())) {
      log.error("Illegal Request: Power of Attorney is already exist, grantee name: {}, account number: {}, authorization: {}", poaRequest.getGrantee(), poaRequest.getAccountNumber(), poaRequest.getAuthorizationType());
      throw new PoaExistException(String.format("Authorization request for the grantee user %s is already existed", poaRequest.getGrantee()));
    }

    PoaDocument pofDocument = poaRepository.save(mapper.mapToDocument(poaRequest));

    log.info("Grantor give access to grantee successfully.");

    return mapper.mapToResponse(pofDocument);
  }

  public List<AccessAccounts> getAllAccessAccounts(String user) {
    log.info("Retrieve list of access accounts for user {}", user);

    List<PoaDocument> pofDocumentList = poaRepository.findAllByGranteeNameIgnoreCase(user);
    if (pofDocumentList.isEmpty()) {
      log.error("Illegal Request: No account access found for the user {}", user);
      throw new AccountAccessNotFoundException(String.format("No access accounts found for user %s", user));
    }
    log.info("Retrieve list of access accounts for user {} successfully.", user);

    return pofDocumentList.stream().map(mapper::mapToAccessAccount).collect(Collectors.toList());
  }
}
