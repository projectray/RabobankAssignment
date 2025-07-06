package nl.rabobank.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AllowedValuesValidator implements ConstraintValidator<AllowedValues, String> {

  private List<String> allowedValues;
  private boolean ignoreCase;

  @Override
  public void initialize(AllowedValues annotation) {
    ignoreCase = annotation.ignoreCase();
    allowedValues = Arrays.stream(annotation.value())
      .map(v -> ignoreCase ? v.toLowerCase() : v)
      .collect(Collectors.toList());
  }

  @Override
  public boolean isValid(String s, ConstraintValidatorContext context) {
    if (s == null) {
      return true;
    }
    return allowedValues.contains(ignoreCase ? s.toLowerCase() : s);
  }
}
