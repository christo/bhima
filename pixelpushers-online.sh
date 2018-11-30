#!/usr/bin/env bash

GREEN="\033[92m"
YELLOW="\033[93m"
RED="\033[91m"
NORMAL="\033[0m"

for pp in 1 2 3 4; do 
    ((ping -c 2 10.10.10.$pp 2>&1 >/dev/null && printf " \\033[92mPP${pp} online\\033[0m  ")  \
        || printf "\\033[91m PP${pp} offline\033[0m " ) &
done
wait 
echo

