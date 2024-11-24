#!/bin/bash

# Définir une limite de temps (en secondes)
TIMEOUT=7

# Lire le code Python depuis l'argument ou l'entrée standard
if [ $# -gt 0 ]; then
    code="$1"
else
    code=$(cat)
fi

# Normaliser le code pour éviter les problèmes d'encodage ou de caractères spéciaux
code=$(echo "$code" | tr -d '\r' | iconv -f UTF-8 -t UTF-8)

# Fichier temporaire pour stocker les erreurs
error_file=$(mktemp)

# Exécuter le code Python avec une limite de temps
output=$(timeout $TIMEOUT python3 -c "$code" 2> "$error_file")
exit_code=$?

# Vérifier si le code a échoué
if [ $exit_code -eq 124 ]; then
    echo "Error: Execution timed out."
    exit $exit_code
elif [ $exit_code -ne 0 ]; then
    echo "Error: "
    cat "$error_file"
    exit $exit_code
else
    echo "$output"
fi

# Nettoyer les fichiers temporaires
rm -f "$error_file"

