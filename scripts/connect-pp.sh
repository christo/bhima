#!/usr/bin/env bash

echo attempting to launch screen to talk to pixelpusher on serial port
echo to quit, send the sequence: ^a ^\\
EXPECTED='/dev/tty.usbmodem12341'
if [[ -c $EXPECTED ]]; then 
    screen $EXPECTED 115200
    #reset
else
    echo could not find pixelpusher serial device on $EXPECTED
    echo here\'s a possibly empty list of what I could find:
    ls /dev/tty.usbmodem*
fi
