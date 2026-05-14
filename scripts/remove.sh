#!/bin/bash

cd ../docker

sudo docker-compose -f docker-compose-local.yml down --volumes && sudo docker ps
