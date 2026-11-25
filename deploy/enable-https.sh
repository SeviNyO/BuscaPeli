#!/usr/bin/env bash
set -euo pipefail

cd /home/ubuntu/BuscaPeli
sudo apt-get update -qq
sudo DEBIAN_FRONTEND=noninteractive apt-get install -y certbot
sudo install -d -m 0755 /var/www/letsencrypt

if ! sudo test -s /etc/letsencrypt/live/buscapeli.duckdns.org/fullchain.pem; then
    sudo install -m 0644 deploy/nginx-bootstrap.conf /etc/nginx/sites-available/buscapeli
    sudo nginx -t
    sudo systemctl reload nginx
    sudo certbot certonly --webroot -w /var/www/letsencrypt -d buscapeli.duckdns.org --non-interactive --agree-tos --register-unsafely-without-email
fi

sudo install -d -m 0755 /etc/letsencrypt/renewal-hooks/deploy
sudo install -m 0755 deploy/reload-nginx.sh /etc/letsencrypt/renewal-hooks/deploy/buscapeli-nginx
sudo install -m 0644 deploy/nginx.conf /etc/nginx/sites-available/buscapeli
sudo nginx -t
sudo systemctl reload nginx
sudo systemctl enable --now certbot.timer

python3 -c 'from pathlib import Path; p=Path(".env"); s=p.read_text(); lines=["COOKIE_SECURE=true" if line.startswith("COOKIE_SECURE=") else line for line in s.splitlines()]; p.write_text("\n".join(lines)+"\n")'
chmod 600 .env
docker compose up -d --no-deps backend
