#!/usr/bin/env bash

set -X

zip -r exercise.zip ./*

curl -v -X POST -F exercise=@exercise.zip http://localhost:8080/submit

rm exercise.zip