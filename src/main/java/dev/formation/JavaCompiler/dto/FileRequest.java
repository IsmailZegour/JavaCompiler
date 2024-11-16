package dev.formation.JavaCompiler.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileRequest {
    private String filePath; // Chemin vers le fichier contenant le code Java
}
