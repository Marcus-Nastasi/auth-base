#!/bin/sh
set -e

echo "Buscando segredos do SSM..."

# Função auxiliar para buscar parâmetro do SSM
get_param() {
  aws ssm get-parameter \
    --name "/${APP_NAME}/$1" \
    --with-decryption \
    --query "Parameter.Value" \
    --output text
}

# Chaves RSA — escritas em arquivo (a app lê por path)
get_param "rsa_private_pem" > /app/private.pem
get_param "rsa_public_pem"  > /app/public.pem

# Credenciais exportadas como variáveis de ambiente
export DB_URL=$(get_param "db_url")
export DB_USERNAME=$(get_param "db_username")
export DB_PASSWORD=$(get_param "db_password")
export OAUTH2_ISSUER_URI=$(get_param "oauth2_issuer_uri")
export OAUTH2_KID=$(get_param "oauth2_kid")
export MAIL_HOST=$(get_param "mail_host")
export MAIL_PORT=$(get_param "mail_port")
export MAIL_USERNAME=$(get_param "mail_username")
export MAIL_PASSWORD=$(get_param "mail_password")
export MAIL_TEAM=$(get_param "mail_team")

echo "Segredos carregados. Iniciando aplicação..."

exec java -jar ./application/application-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
