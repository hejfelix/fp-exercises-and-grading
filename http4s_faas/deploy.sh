#!/usr/bin/env bash
faas-cli remove http4s_openfaas
faas-cli deploy --replace --update=false -f http4s_faas.yml