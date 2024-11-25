package dev.formation.JavaCompiler.controller;

import dev.formation.JavaCompiler.dto.CodeRequest;
import dev.formation.JavaCompiler.dto.CodeResponse;
import dev.formation.JavaCompiler.service.CompilerService;
import dev.formation.JavaCompiler.validator.LanguageValidator;
import dev.formation.JavaCompiler.validator.ValidatorFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/compile")
@CrossOrigin(origins = "${frontend.url}") // Autorise uniquement Angular
public class CompilerController {

    private final CompilerService compilerService;

    private final ValidatorFactory validatorFactory;

    public CompilerController(CompilerService compilerService, ValidatorFactory validatorFactory) {
        this.compilerService = compilerService;
        this.validatorFactory = validatorFactory;
    }

    @PostMapping
    public ResponseEntity<CodeResponse> compileAndRun(@RequestBody CodeRequest codeRequest) {
        try {
            String language = codeRequest.getLanguage();
            String code = codeRequest.getCode();

            LanguageValidator validator = validatorFactory.getValidator(language);
            validator.validate(code); // Validation avant traitement

            String imageName = validator.getImageName();
            String instrumentedCode = validator.injectMeasurements(code);


            return ResponseEntity.ok(compilerService.startContainer(instrumentedCode, imageName));

        } catch (IllegalArgumentException e) {
            // Gère les erreurs de validation (mauvais code, etc.)
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new CodeResponse(e.getMessage(), null,null));
        } catch (Exception e) {
            // Gère les erreurs internes ou inattendues
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CodeResponse(e.getMessage(),null,null));
        }
    }
    @GetMapping("/test")
    public String getHelloWorld(){
        return "Hello, World!";
    }
}
