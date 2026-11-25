#!/usr/bin/env bash
set -euo pipefail

cd /home/ubuntu/BuscaPeli
test -f .env

sudo apt-get update -qq
sudo apt-get install -y ca-certificates curl nginx
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo tee /etc/apt/keyrings/docker.asc >/dev/null
sudo chmod a+r /etc/apt/keyrings/docker.asc
sudo install -m 0644 deploy/docker.sources /etc/apt/sources.list.d/docker.sources
sudo apt-get update -qq
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo usermod -aG docker ubuntu

if ! swapon --show | grep -q .; then
    sudo fallocate -l 1G /swapfile
    sudo chmod 600 /swapfile
    sudo mkswap /swapfile
    sudo swapon /swapfile
    if ! grep -q '^/swapfile ' /etc/fstab; then
        printf '/swapfile none swap sw 0 0\n' | sudo tee -a /etc/fstab >/dev/null
    fi
fi

sudo install -m 0644 deploy/nginx.conf /etc/nginx/sites-available/buscapeli
sudo ln -sfn /etc/nginx/sites-available/buscapeli /etc/nginx/sites-enabled/buscapeli
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t
sudo systemctl enable --now docker nginx
sudo systemctl reload nginx
sudo install -m 0644 deploy/buscapeli-deploy.service /etc/systemd/system/buscapeli-deploy.service
sudo install -m 0644 deploy/buscapeli-deploy.timer /etc/systemd/system/buscapeli-deploy.timer
sudo systemctl daemon-reload
sudo systemctl enable --now buscapeli-deploy.timer
sudo systemctl start buscapeli-deploy.service
