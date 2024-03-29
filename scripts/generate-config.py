#!/usr/bin/env python3

# generate pixel pusher config file for the given pixel pusher number

import sys
import time
from datetime import date

# rainbow
# Violet 148, 0, 211 #9400D3 
# Indigo 75, 0, 130 #4B0082 
# Blue 0, 0, 255 #0000FF 
# Green 0, 255, 0 #00FF00 
# Yellow 255, 255, 0 #FFFF00 
# Orange 255, 127, 0 #FF7F00 
# Red 255, 0 , 0 #FF0000

rainbow=(
    "9400D3"
    , "4B0082"
    , "0000FF"
    , "00FF00"
    , "FFFF00"
    , "FF7F00"
    , "FF0000"
)

common = """
pixels=300
group=1
controller={0}
stripsattached={1}
blank_strips_on_idle=0
# dhcp_timeout=24
ether=10.10.10.{0}
netmask=255.255.255.0
gateway=192.168.0.1
"""[1:-1]

perstrip = """
start{0}={1}
strip{0}=ws2811
order{0}=grb
"""[1:-1]

if len(sys.argv) != 3:
    print("usage: " + sys.argv[0] + " c n\nwhere c is the pp number and n is the number of strips from 1-8")
else:
    controller = int(sys.argv[1])
    strips = int(sys.argv[2]) # will blow up on integer parse exception
    print("#generated by " + sys.argv[0] + " on " + date.today().isoformat())
    print(common.format(str(controller), str(strips)))
    for s in range(1, strips+1):
        colour = rainbow[s % len(rainbow)]
        print(perstrip.format(str(s), colour))


