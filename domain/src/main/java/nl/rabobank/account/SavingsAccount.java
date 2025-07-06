package nl.rabobank.account;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

@Value
@Getter
@Builder
public class SavingsAccount implements Account
{
    String accountNumber;
    String accountHolderName;
    Double balance;
}
