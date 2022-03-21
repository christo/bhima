#!/usr/bin/env zsh

# imovie can't even deal when it comes to mkv, but that's just a "container" and you can recontain it in mp4
# without transcoding.

# for other formats, let ffmpeg decide whether to transcode, simply do something like this:
# ffmpeg -i input.webm output.mp4

# depends on ffmpeg


if [[ -z "$1" ]]; then
  echo usage: $0 foobar.mkv
  exit 1
fi

mkvFile="$1"
mp4File="`basename "$mkvFile" mkv`mp4"
ffmpeg -i "$mkvFile" -codec copy "$mp4File"
echo converted $mkvFile to $mp4File
