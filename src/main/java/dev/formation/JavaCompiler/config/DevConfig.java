package dev.formation.JavaCompiler.config;

import dev.formation.JavaCompiler.service.ICompilerService;
import dev.formation.JavaCompiler.service.LocalCompilerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class DevConfig {
    @Bean
    public ICompilerService compilerService() {
        return new LocalCompilerService();
    }
}

