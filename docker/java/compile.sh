#!/bin/bash

# Définir une limite de temps pour l'exécution (par exemple, 10 secondes)
TIMEOUT=7

# Lire le code à partir de l'argument
code="$1"

# Nettoyer le code des caractères spéciaux
code=$(echo "$code" | tr -d '\r')

# Normaliser l'encodage pour éviter les erreurs liées à l'UTF-8
code=$(echo "$code" | iconv -f UTF-8 -t UTF-8)

# Extraire le nom de la classe publique
className=$(echo "$code" | grep -oP 'public\s+class\s+\K\w+')

# Vérifier si une classe publique a été trouvée
if [ -z "$className" ]; then
    echo "Error: Could not find a public class declaration."
    exit 3
fi

# Créer un fichier temporaire avec le code
echo "$code" > "$className.java"

# Démarrer le chronométrage
start_time=$(date +%s%N)

# Compiler le fichier Java
timeout $TIMEOUT javac "$className.java" 2> error.log

# Vérifier si la compilation a échoué
if [ $? -ne 0 ]; then
    echo "Compilation failed:"
    cat error.log
    exit 2
fi

# Exécuter le fichier compilé avec timeout
timeout $TIMEOUT java "$className" > output.log 2> error.log

# Vérifier si l'exécution a échoué
if [ $? -ne 0 ]; then
    echo "Execution failed:"
    cat error.log
    exit 1
fi

# Arrêter le chronométrage
end_time=$(date +%s%N) # Temps en millisecondes
execution_time_ns=$((end_time - start_time))  # Durée en nanosecondes
execution_time_s=$(awk "BEGIN {printf \"%.3f\", $execution_time_ns / 1000000000}")  # Conversion en secondes avec 3 décimales


# Afficher la sortie du programme suivie de l'exécution totale
cat output.log
echo "{ExecutionTime}: ${execution_time_s}"

# Nettoyer les fichiers temporaires
rm -f "$className.java" "$className.class" error.log output.log
