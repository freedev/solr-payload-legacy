#!/bin/bash

curl -X POST "localhost:8983/solr/payloadtest/update" --data-binary @data.json -H 'Content-type:application/json'
curl "http://localhost:8983/solr/payloadtest/update?commit=true"
