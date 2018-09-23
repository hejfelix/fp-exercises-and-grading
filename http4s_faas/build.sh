#!/usr/bin/env bash
set -e
sbt assembly
rm -fv openfaas/app.jar
cp -v target/scala-2.12/http4s_faas-assembly-0.0.1-SNAPSHOT.jar openfaas/app.jar
faas-cli build -f http4s_faas.yml