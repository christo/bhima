#!/usr/bin/env bash

# builds distribution then unpacks and runs it locally
# assumes we are connected to the dragon

set -vx 
shopt -s failglob

gradle clean
gradle distTar
mkdir -p tmp
rm -rf tmp/*
tar xf dragonmind/build/distributions/dragonmind-*.tar -C tmp/
dd=tmp/dragonmind-*
ln -s dragonmind/video ${dd#}/
${dd#}/bin/dragonmind
