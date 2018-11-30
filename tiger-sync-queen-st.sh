#!/usr/bin/env bash

rsync -vazu --delete-after /Users/christo/src/christo/bhima christo@tiger.local:src/christo
rsync -vazu /Users/christo/src/christo/processing christo@tiger.local:src/christo
rsync -vazu /Users/christo/.ivy2 christo@tiger.local:
rsync -vazu /Users/christo/.gradle christo@tiger.local:
rsync -vazu /Users/christo/.m2 christo@tiger.local:
