#!/usr/bin/env sh
set -eu

# Writable at runtime even when /usr is read-only (Podman/K8s hardening); nginx serves via alias in nginx.conf
cat <<EOF >/tmp/app-config.js
window.__APP_CONFIG__ = {
  keycloak: {
    url: "${KEYCLOAK_URL:-http://localhost:8080}",
    realm: "${KEYCLOAK_REALM:-demo}",
    clientId: "${KEYCLOAK_CLIENT_ID:-my-app}"
  }
};
EOF
