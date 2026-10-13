#!/usr/bin/env bash
# Prepara um servidor Ubuntu (22.04/24.04) para receber os deploys de staging e producao.
# Uso (no servidor): bash setup-server.sh
set -euo pipefail

sudo apt-get update
sudo apt-get install -y docker.io docker-compose-v2 curl
sudo systemctl enable --now docker
sudo usermod -aG docker "$USER"

# Swap ajuda o Oracle Free em VMs com pouca memoria
if [ ! -f /swapfile ]; then
  sudo fallocate -l 4G /swapfile
  sudo chmod 600 /swapfile
  sudo mkswap /swapfile
  sudo swapon /swapfile
  echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
fi

mkdir -p ~/esg-api/staging ~/esg-api/production

docker --version
docker compose version
echo "Servidor pronto. Saia e entre novamente na sessao SSH para aplicar o grupo 'docker'."
