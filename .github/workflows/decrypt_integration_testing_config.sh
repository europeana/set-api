#!/bin/sh

# Decrypt the client properties file
mkdir -p /opt/app/config
# --batch to prevent interactive command
# --yes to assume "yes" for questions
gpg --quiet --batch --yes --decrypt --passphrase="$SECRET_PROPS_PASSPHRASE" \
--output /opt/app/config/set-integration-testing.user.properties ./.github/workflows/set-integration-testing.user.properties.gpg