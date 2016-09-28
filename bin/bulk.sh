#!/usr/bin/env bash

# This example file sends compressed JSON lines formatted files to Elasticsearch bulk endpoint
# It assumes the index settings and the mappings are already created and configured.

# Warning: bulk responses are not evaluated. To send large volumes without errors, more
# precautions have to be considered.

for f in build/*.jsonl.gz; do
  curl -XPOST -H "Accept-Encoding: gzip" -H "Content-Encoding: gzip" \
   --data-binary @$f --compressed localhost:9200/_bulk
done
