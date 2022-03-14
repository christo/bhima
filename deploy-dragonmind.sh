#!/usr/bin/env bash

# takes the already built tar file in dragonmind/build/distributions/
# and does a fresh (re)install of it on the given target machine
# (or a default if no target) to which we must be able to ssh
# so make sure the username for the target host is in the ssh config.

# usage deploy-dragonmind.sh [<target machine> [<target user>]]

# TODO move to scripts/

# config

target_machine=${1:-tiger.local}
#target_machine=${1:-silverbox.local}
#target_machine=192.168.1.112
deploy_dir=dragonmind
archive_dir=archive

shopt -s failglob

cd "$( dirname "${BASH_SOURCE[0]}" )"

source functions.sh

# convenience variables

ssh_target=${target_machine}
unixtime=`date +%s`

# changes foreground colour so unexpected stdout from following command can be visually distnct
function yy() {
  printf " \\033[33m"
}
#main

deployable=dragonmind/build/distributions/dragonmind-*.tar
echo -n locating deployable ${deployable} 
yy
test -f $deployable && ok || die

echo build timestamp $unixtime
echo -n verifying connectivity to $target_machine 
yy
ping -c 1 $target_machine 2>&1 >/dev/null && ok || die

echo -n testing ssh 
yy
ssh $ssh_target mkdir -p $archive_dir && ok || die

echo -n deploying $deployable 
yy
rsync -q $deployable $ssh_target: && ok || die
fname=`basename $deployable`

echo -n syncing video dir 
yy
rsync -qazu dragonmind/video/ $ssh_target:video && ok || die
ddir=`basename -s .tar $deployable`

echo -n removing existing distribution directory $ddir 
yy
ssh $ssh_target "mkdir -p $ddir && rm -r \"$ddir\"" && ok || die

echo -n unarchiving $fname 
yy
ssh $ssh_target "tar -xf $fname" && ok || die

echo -n linking video dir 
yy
ssh $ssh_target "ln -sf ~/video $ddir" && ok || die
