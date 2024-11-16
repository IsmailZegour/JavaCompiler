#!/bin/bash

# Lire le code depuis l'argument ou l'entrée standard
if [ $# -gt 0 ]; then
    code="$1"
else
    code=$(cat)
fi

# Exécuter le code Python
python3 -c "$code"
