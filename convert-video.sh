#!/usr/bin/env bash

# TODO properly change the file extension rather than adding to it

# note -an means "no audio"

mkdir -p 400x100
for i in *; do ffmpeg -i "$i" -vf scale=400:100 -an -hide_banner "400x100/$i.mp4" ; done