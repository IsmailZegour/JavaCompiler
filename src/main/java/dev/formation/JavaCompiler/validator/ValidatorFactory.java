package dev.formation.JavaCompiler.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ValidatorFactory {

    private final Map<String, LanguageValidator> validators;
    @Autowired
    public ValidatorFactory(Map<String, LanguageValidator> validators) {
        this.validators = validators;
    }

    public LanguageValidator getValidator(String language) {
        LanguageValidator validator = validators.get(language.toLowerCase());
        if (validator == null) {
            throw new IllegalArgumentException("Unsupported language: " + language);
        }
        return validator;
    }
}
