# Please note, this file should have unix-based end of line
# Use same version as CloudFoundry deployment
#FROM library/mongo:4.4.15
FROM library/mongo:6.0.14-jammy

EXPOSE 27017

ENV MONGO_INITDB_ROOT_USERNAME admin_user
ENV MONGO_INITDB_ROOT_PASSWORD admin_password

#MONGO_INITDB_DATABASE

#COPY init-mongo.sh /docker-entrypoint-initdb.d/init-mongo.sh