#!/bin/bash

# Créer un fichier temporaire contenant le code
echo "$1" > script.py

# Exécuter le fichier Python
python3 script.py 2> error.log

# Si erreur, afficher les logs
if [ $? -ne 0 ]; then
    cat error.log
fi
