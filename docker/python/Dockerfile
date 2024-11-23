FROM python:3.10-slim

# Installer psutil
RUN pip install psutil

# Installer timeout (inclus dans coreutils)
RUN apt-get update && apt-get install -y coreutils && apt-get clean

# Créer un répertoire de travail
WORKDIR /workspace

# Copier le script d'exécution
COPY compile.sh /workspace/compile.sh
RUN chmod +x /workspace/compile.sh

# Entrée par défaut du conteneur
ENTRYPOINT ["/workspace/compile.sh"]
