#!/usr/bin/env bash

# TODO properly change the file extension rather than adding to it
# TODO properly handle files with spaces in names

# note -an means "no audio"

mkdir -p 400x100
for i in *; do
    if [[ -f "$i" ]]; then
        dimensions=`ffprobe -v error -select_streams v:0 -show_entries stream=width,height -of csv=s=x:p=0 \""$i"\" 2>/dev/null`
        basename=`perl -e '$ARGV[0] =~ s/(.*)\.\w+$/$1/ and print $1' $i`
        if [[ -z "$basename" || -z "$dimensions"]]; then
            echo checking $i
            echo dimensions: $dimensions
            echo basename: $basename
            echo ffmpeg -i "$i" -vf scale=400:100 -an -hide_banner "400x100/$basename.m4v" ;
        fi
    fi
done