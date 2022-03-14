#!/usr/bin/env bash

# syncs project dir and all local java repos to tiger server or given target machine
# needed because we may need to rebuild on the deployment target at a festival without
# internet access. NOTE: expects ssh config for target host to define username. Works
# best with pubkey auth, otherwise multiple password prompts will occur
# target machine is assumed to use src/christo as location of project dir

# usage: tiger-sync.sh [target_host]

export TIGER=${1:-tiger.local}

export SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
export PROJECT_DIR=$(dirname "$SCRIPT_DIR")

export PROCESSING_SKETCH_DIR=$HOME/src/christo/processing

rsync -vazu --delete-after "$PROJECT_DIR" $TIGER:src/christo
rsync -vazu --delete-after "$PROCESSING_SKETCH_DIR" $TIGER:src/christo
rsync -vazu $HOME/.ivy2 $TIGER:
rsync -vazu $HOME/.gradle $TIGER:
rsync -vazu $HOME/.m2 $TIGER:
