#!/usr/bin/env bash

rsync -vazu --delete-after /Users/christo/src/christo/bhima christo@10.10.10.101:src/christo
rsync -vazu /Users/christo/src/christo/processing christo@10.10.10.101:src/christo
rsync -vazu /Users/christo/.ivy2 christo@10.10.10.101:
rsync -vazu /Users/christo/.gradle christo@10.10.10.101:
rsync -vazu /Users/christo/.m2 christo@10.10.10.101:
