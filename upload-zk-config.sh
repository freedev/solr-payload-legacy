#!/bin/bash

SOLR_HOME=~/workdir/solrcloud/solr-5.3.1
ZK_HOST=127.0.0.1:9983

$SOLR_HOME/server/scripts/cloud-scripts/zkcli.sh -cmd upconfig -confname payloadtest -confdir solr/config/ --zkhost $ZK_HOST
