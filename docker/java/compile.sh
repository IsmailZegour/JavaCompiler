#!/bin/bash

# Extraire le nom de la classe publique
className=$(echo "$1" | grep -oP 'public\s+class\s+\K\w+')

# Vérifier si le nom de la classe a été trouvé
if [ -z "$className" ]; then
    echo "Error: Could not find a public class declaration."
    exit 1
fi

# Créer un fichier temporaire contenant le code avec le bon nom
echo "$1" > "$className.java"

# Compiler le fichier
javac "$className.java" 2> error.log

# Si la compilation réussit, exécuter le fichier
if [ $? -eq 0 ]; then
    java "$className"
else
    # Si la compilation échoue, afficher les erreurs
    cat error.log
fi
