#!/bin/bash

cd ../../docker

sudo docker-compose -f docker-compose.yml up -d

sudo docker ps
