#!/bin/bash

# Définir une limite de temps pour l'exécution (par exemple, 10 secondes)
TIMEOUT=5

# Vérifier si un fichier est monté
if [ -f "/workspace/Main.java" ]; then
    # Lire le contenu du fichier
    code=$(cat /workspace/Main.java)
else
    # Lire depuis stdin ou arguments
    if [ $# -gt 0 ]; then
        code="$1"
    else
        code=$(cat)
    fi
fi

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

# Compiler le fichier Java
timeout $TIMEOUT javac "$className.java" 2> error.log

# Vérifier si la compilation a échoué
if [ $? -ne 0 ]; then
    echo "Compilation failed :"
    cat error.log
    exit 2
fi

# Exécuter le fichier compilé avec timeout
timeout $TIMEOUT java "$className" > output.log 2> error.log

# Vérifier si l'exécution a échoué
if [ $? -ne 0 ]; then
    echo "Execution failed :"
    cat error.log
    exit 1
fi

end_time=$(date +%s%3N) # Temps en millisecondes
execution_time=$((end_time - start_time)) # Durée en millisecondes

echo "Execution time: ${execution_time}ms"
# Afficher uniquement la sortie du programme
cat output.log

# Nettoyer les fichiers temporaires
rm -f "$className.java" "$className.class" error.log output.log
