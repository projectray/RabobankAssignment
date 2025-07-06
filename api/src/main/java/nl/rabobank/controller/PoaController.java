package nl.rabobank.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.rabobank.dto.AccessAccounts;
import nl.rabobank.dto.PoaRequest;
import nl.rabobank.dto.PoaResponse;
import nl.rabobank.service.AuthorizationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/poa")
@RequiredArgsConstructor
@Slf4j
public class PoaController {
  private final AuthorizationService authorizationService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public PoaResponse create(@Valid @RequestBody PoaRequest request) {
    log.info("Received request to create Power of Attorney, grantor: {}, grantee: {}", request.getGrantor(), request.getGrantor());
    PoaResponse poaResponse = authorizationService.grantAccess(request);
    log.info("Create Power of Attorney with id: {} for grantee: {}", poaResponse.getId(), poaResponse.getGrantee());
    return poaResponse;
  }


  @GetMapping
  public List<AccessAccounts> accessAccounts(@RequestParam("grantee") String granteeName) {
    log.info("Listing access accounts for grantee: {}", granteeName);
    List<AccessAccounts> accounts = authorizationService.getAllAccessAccounts(granteeName);
    log.info("Found {} authorized accounts for grantee: {}", accounts.size(), granteeName);
    return accounts;
  }

}
