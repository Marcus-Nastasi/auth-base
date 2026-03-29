#!/bin/bash

cd ../docker

sudo docker-compose -f docker-compose-local.yml up -d

sudo docker ps
