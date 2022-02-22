#!/usr/bin/env bash

# prints out the current network situation of production
# usage network-status.sh [<target machine> [<target user>]]
# default machine: silverbox.local default user: bhima

cd "$( dirname "${BASH_SOURCE[0]}" )"

source functions.sh

target_machine=${1:-silverbox.local}
target_username=${2:-bhima}
ssh_target=${target_username}@${target_machine}

# internet connectivity
echo -n internet connection:
pingtest google.com

echo -n connection to $target_machine:
pingtest "$target_machine"

echo -n ssh authentication to $target_machine:
ssh -o "PubkeyAuthentication=yes" "$ssh_target" ls >/dev/null 2>&1 && ok || fail

echo -n pixelpusher connectivity:
./pixelpushers-online.sh
