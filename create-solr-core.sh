#!/bin/bash

## IMPORTANT: CONFIGURE YOUR SOLR HOME
SOLR_HOME=~/workdir/solrcloud/solr-5.3.1

$SOLR_HOME/bin/solr create_core -c payloadtest -d solr/config

curl -X POST "localhost:8983/solr/payloadtest/update" --data-binary @data.json -H 'Content-type:application/json'
curl "http://localhost:8983/solr/payloadtest/update?commit=true"
