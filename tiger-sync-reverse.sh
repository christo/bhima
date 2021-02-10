#!/usr/bin/env bash

rsync -vazu --delete-after christo@10.10.10.101:src/christo/bhima /Users/christo/src/christo/bhima-tiger/
#rsync -vazu /Users/christo/src/christo/processing christo@10.10.10.101:src/christo
#rsync -vazu christo@10.10.10.101:.ivy2 /Users/christo/.ivy2 
#rsync -vazu christo@10.10.10.101:.gradle /Users/christo/.gradle 
#rsync -vazu christo@10.10.10.101:.m2 /Users/christo/.m2 
