#!/bin/bash

cd ../docker

sudo docker-compose \
    -f docker-compose-docker-local.yml \
    up -d \
    && sudo docker ps
