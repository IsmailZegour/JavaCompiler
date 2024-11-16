#!/bin/bash

# Lire le code depuis l'argument ou l'entrée standard
if [ $# -gt 0 ]; then
    code="$1"
else
    code=$(cat)
fi

# Créer un fichier temporaire
echo "$code" > temp.c

# Compiler le fichier C
gcc temp.c -o temp.out 2> error.log

# Si la compilation échoue, afficher les erreurs
if [ $? -ne 0 ]; then
    cat error.log
    exit 1
fi

# Exécuter le fichier compilé
./temp.out
