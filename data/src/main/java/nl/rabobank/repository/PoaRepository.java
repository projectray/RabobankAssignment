package nl.rabobank.repository;

import nl.rabobank.authorizations.Authorization;
import nl.rabobank.document.PoaDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PoaRepository extends MongoRepository<PoaDocument, String> {

  List<PoaDocument> findAllByGranteeNameIgnoreCase(String granteeName);

  boolean existsByGranteeNameAndAuthorizationAndAccount_AccountNumber(String granteeName, Authorization authorization, String accountNumber);
}
