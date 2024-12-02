package dev.formation.JavaCompiler.controller;

import dev.formation.JavaCompiler.dto.CodeRequest;
import dev.formation.JavaCompiler.dto.CodeResponse;
import dev.formation.JavaCompiler.service.CompilerService;
import dev.formation.JavaCompiler.validator.LanguageValidator;
import dev.formation.JavaCompiler.validator.ValidatorFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping
//@CrossOrigin(origins = "${frontend.url}") // Autorise uniquement Angular
@CrossOrigin(origins = "*") // Autorise tout (test)
public class CompilerController {

    private final ValidatorFactory validatorFactory;

    private final CompilerService compilerService;

    public CompilerController( ValidatorFactory validatorFactory,RestTemplate restTemplate,CompilerService compilerService) {
        this.validatorFactory = validatorFactory;
        this.compilerService = compilerService;
    }

    @PostMapping("/compile")
    public ResponseEntity<CodeResponse> compileAndRun(@RequestBody CodeRequest codeRequest) {
        try {
            System.out.println("Requête reçue par le front");
            String language = codeRequest.getLanguage();
            String code = codeRequest.getCode();

            LanguageValidator validator = validatorFactory.getValidator(language);
            validator.validate(code); // Validation avant traitement

            String instrumentedCode = validator.injectMeasurements(code);

            return ResponseEntity.ok(compilerService.compileAndExecute(instrumentedCode));

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new CodeResponse(e.getMessage(), null,null));
        } catch (Exception e) {
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
