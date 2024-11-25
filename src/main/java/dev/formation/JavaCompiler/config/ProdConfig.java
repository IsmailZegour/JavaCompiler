package dev.formation.JavaCompiler.config;

import dev.formation.JavaCompiler.service.AWSCompilerService;
import dev.formation.JavaCompiler.service.ICompilerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
@Configuration
@Profile("prod")
public class ProdConfig {
    @Bean
    public ICompilerService compilerService() {
        return new AWSCompilerService();
    }
}