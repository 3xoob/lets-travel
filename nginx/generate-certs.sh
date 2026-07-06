#!/bin/bash
set -e
mkdir -p "$(dirname "$0")/certs"
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout "$(dirname "$0")/certs/localhost.key" \
  -out "$(dirname "$0")/certs/localhost.crt" \
  -subj "/C=US/ST=Dev/L=Dev/O=LetsTravel/CN=localhost" \
  -extensions v3_req \
  -config <(cat <<EOF
[req]
req_extensions = v3_req
distinguished_name = req_distinguished_name
[req_distinguished_name]
[v3_req]
subjectAltName = @alt_names
[alt_names]
DNS.1 = localhost
IP.1 = 127.0.0.1
EOF
)
echo "Self-signed cert generated at nginx/certs/"
