#!/usr/bin/env bash

# reports on which pixel pushers are currently online using their known fixed IP addresses

# print $1 in ansi $2
p() { printf "\\033[$2m $1\033[0m "; }

for pp in 1 2 3 4; do
    ((ping -c 2 10.10.10.$pp 2>&1 >/dev/null && p "PP$pp online" 92 ) || p "PP$pp offline" 91 ) &
done
wait 
echo

