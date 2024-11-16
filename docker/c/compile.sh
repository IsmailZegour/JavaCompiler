#!/bin/bash

# Créer un fichier temporaire contenant le code
echo "$1" > program.c

# Compiler le fichier
gcc program.c -o program.out 2> error.log

# Si compilation réussie, exécuter le programme
if [ $? -eq 0 ]; then
    ./program.out
else
    cat error.log
fi
