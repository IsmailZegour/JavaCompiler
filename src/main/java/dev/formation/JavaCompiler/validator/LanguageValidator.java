package dev.formation.JavaCompiler.validator;

public interface LanguageValidator {
    void validate(String code);
    String injectMeasurements(String code);
    String getImageName();
}
