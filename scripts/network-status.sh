#!/usr/bin/env bash

# prints out the current network situation of production
# usage network-status.sh [<target machine> [<target user>]]
# default machine: silverbox.local

pushd "$( dirname "${BASH_SOURCE[0]}" )"

source ../functions.sh

target_machine=${1:-tiger.local}

# internet connectivity
echo -n internet connection:
pingtest google.com

echo -n connection to $target_machine:
pingtest "$target_machine"

echo -n ssh authentication to $target_machine:
ssh -o "PubkeyAuthentication=yes" "$target_machine" ls >/dev/null 2>&1 && ok || fail

echo -n pixelpusher connectivity:
./pixelpushers-online.sh
popd
