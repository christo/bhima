#!/usr/bin/env bash

# $1 = egrep options
# $2 = pattern
# $3 = message
function grepdev() {
    flags=$1
    pattern=$2
    mesg=$3
    output=$(ls -lt /dev | grep '^c' | egrep "$flags" "$pattern" | head -n 12)
    if [[ ! -z "$output" ]]; then 
        echo $mesg
        echo -n "$output"
        echo
    fi
}

grepdev -i usb "smart guess:"
grepdev -vi '(disk|Bluetooth|BIGJAMBOX|Bose|autofs|console)' "filtered:"
grepdev -i . raw

#echo smart guess:
#ls -lt /dev |grep '^c' |grep -i usb |head -n 5
#echo filtered:
#ls -lt /dev |grep '^c' |egrep -vi '(disk|Bluetooth|BIGJAMBOX|Bose|autofs|console)' |head -n 8
#echo raw:
#ls -lt /dev |grep '^c' |head -n 8
