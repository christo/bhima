#!/usr/bin/env zsh

set -vx

if [[ -z "$1" ]]; then 
    echo usage: $0 input.mov
    exit 1
fi
infile="$1"
outfile="`basename "$infile" mov`m4v"

ffmpeg -i "$infile" -vf scale=400:100 -an -n "~/src/christo/bhima/dragonmind/video/$outfile" && rm "$infile"

