#!/usr/bin/env bash

# reports on which pixel pushers are currently online using their known fixed IP addresses

for pp in 1 2 3 4; do 
    ((ping -c 2 10.10.10.$pp 2>&1 >/dev/null && printf " \\033[92mPP${pp} online\\033[0m  ")  \
        || printf "\\033[91m PP${pp} offline\033[0m " ) &
done
wait 
echo

