#!/usr/bin/env bash

# builds distribution then unpacks and runs it locally
# assumes we are connected to the dragon

# TODO move to scripts/

set -vx 
shopt -s failglob

echo  $JAVA_HOME

if [[ -f "sourceme.sh" ]]; then
  source "sourceme.sh"
fi

./gradlew clean || exit
./gradlew distTar || exit
mkdir -p tmp
rm -rf tmp/*
tar xf dragonmind/build/distributions/dragonmind-*.tar -C tmp/
dd=tmp/dragonmind-*
ln -s $PWD/dragonmind/video ${dd#}/
cd ${dd#}
./bin/dragonmind
