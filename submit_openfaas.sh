#!/usr/bin/env bash

set -X

zip -r exercise.zip ./build.sbt ./project ./exercises-1 ./grading

curl -v -F exercise=@exercise.zip http://127.0.0.1:8080/function/http4s_openfaas

rm exercise.zip