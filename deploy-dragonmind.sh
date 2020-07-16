#!/usr/bin/env bash

# takes the already built tar file in dragonmind/build/distributions/ 
# and does a fresh (re)install of it on the given target machine
# (or dragon.local if no target) to which we must be able to ssh

# usage deploy-dragonmind.sh [target machine]

# config

target_machine=${1:-dragon.local}
#target_machine=192.168.1.112
target_username=christo
deploy_dir=dragonmind
archive_dir=archive

shopt -s failglob

# convenience variables

ssh_target=${target_username}@${target_machine}
unixtime=`date +%s`

# functions

function die() {
  printf "  \\033[31mfailed\\033[0m\n"
  exit 1
}

function ok() {
  printf " \\033[32mok\\033[0m\n"
}

#main

deployable=dragonmind/build/distributions/dragonmind-*.tar
echo -n locating deployable ${deployable} 
test -f $deployable && ok || die

echo build timestamp $unixtime
echo -n verifying connectivity to $target_machine
ping -c 1 $target_machine 2>&1 >/dev/null && ok || die
echo -n testing ssh
ssh $ssh_target mkdir -p $archive_dir && ok || die
echo -n deploying $deployable
rsync -q $deployable $ssh_target: && ok || die
fname=`basename $deployable`
echo -n syncing video dir
rsync -qazu dragonmind/video/ $ssh_target:video && ok || die
ddir=`basename -s .tar $deployable`
echo -n removing existing distribution directory $ddir
ssh $ssh_target "test -d $ddir && rm -r \"$ddir\"" && ok
echo -n unarchiving $fname
ssh $ssh_target tar -xf $fname && ok || die
echo -n linking video dir
ssh $ssh_target "ln -sf ~/video $ddir" && ok || die
