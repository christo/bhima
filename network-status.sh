#!/usr/bin/env bash

# prints out the current network situation of production
cd "$( dirname "${BASH_SOURCE[0]}" )"

source functions.sh

# internet connectivity
echo -n internet connection:
pingtest google.com

echo -n connection to silverbox:
pingtest silverbox.local

echo -n ssh authentication to silverbox:
ssh -o "PubkeyAuthentication=yes" silverbox.local ls >/dev/null 2>&1 && ok || fail

echo -n pixelpusher connectivity:
./pixelpushers-online.sh
