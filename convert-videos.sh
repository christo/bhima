#!/usr/bin/env bash

# TODO move to scripts/

# for each file of supported container format, reformat to target resolution and known-playable encoding/container.

pushd dragonmind/video || exit 1
mkdir -p 400x100
for i in *.mp4 *.mov *.avi *.m4v *.mkv *.webm; do 

    echo checking resolution of $i

    res=`ffprobe -v error -select_streams v:0 -show_entries stream=width,height -of csv=s=x:p=0 "$i"`
    bname=${i%.*} 
    targ="400x100/$bname.m4v"
    if [[ "$res" != "400x100" ]]; then 
        echo resolution is $res
        if [[ -e "$targ" ]]; then 
            echo "already have output file, skipping $targ"
        else
            # transform the file
            ffmpeg -i "$i" -vf scale=400:100 -an -n "$targ"
        fi
    else
        echo ! $i is already $res - copying existing
        cp -n "$i" "$targ"
    fi
    echo 
done
popd
