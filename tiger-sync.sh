#!/usr/bin/env bash

export TIGER='tiger.local'

rsync -vazu --delete-after /Users/christo/src/christo/bhima christo@$TIGER:src/christo
rsync -vazu /Users/christo/src/christo/processing christo@$TIGER:src/christo
rsync -vazu /Users/christo/.ivy2 christo@$TIGER:
rsync -vazu /Users/christo/.gradle christo@$TIGER:
rsync -vazu /Users/christo/.m2 christo@$TIGER:
