#!/usr/bin/env bash

# writes pixelpusher config files to mounted USB sticks

# TODO move to scripts/

# note only works on mac
unmount() {
    #diskutil unmountDisk $1 
    diskutil eject $1 
}


thisDir=`dirname $0`
pushd $thisDir >/dev/null
for p in 1 2 3 4; do
    ppdir="/Volumes/PP$p"
    if [[ -d $ppdir ]]; then
        echo -n "found USB at $ppdir "
        file="pp$p-pixel.rc"
        if [[ -f $file ]]; then
            echo copying config
            cp $file $ppdir/pixel.rc
            sync
            unmount $ppdir && echo now safe to remove USB PP$p
        else
            echo but no $file here
        fi
    fi
done
popd >/dev/null
