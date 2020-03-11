# Bhima

Code, docs and config for Burning Seed camp _Dragon Heart_ artcar bus, _Bhima_. #burningman #artcar

Quesions etc. to [bhima@chromosundrift.com](mailto:bhima@chromosundrift.com)

## Summary

* Software-controlled LED surface for the dragon bus
* Video player
* Video import from USB
* Mobile web app for primary control

## TODO

* Mobile web app
    * add instructions and status for stick slurper to mobile web app so people can add content
    * add video file uploader
    * add more system status info 
* parametised geometric algorithms (e.g. sacred geometry shiz) using pixelpusher processing library
* effect: mirrorball sparkle scales
* test shape-aware algorithms, non-rectangular edges e.g. shaded dragon body
* Multiple map model - bundle background and pixelMap into its own node
* enable proper GUI by creating outer swing frame with PApplet component
* Dynamic runtime config of segment wiring
* editing of modular maps include rotation, translation, arbitrary quad-skew with retained local coordinates
* render logical pixelpusher wiring in map
* all GUI image scaling with overlay rendering properly registered
* test full logical layout with 7.5k LEDs
* test ideas: audio-input, accelerometer input, theremin-style control field?

## Features

* semi-automatic pixel point detection within a camera field
* Config file generator for pixelpushers.
* deployment script for config files
* dragonmind processing module

## Random Notes

* use processing sketch *pixelpusher_tester* for running test patterns.

The WS2811 LED strips we are using on Bhima use colour order "grb" (green, red, blue) but the per-strip start colour
(used if the config option `blank_strips_on_idle=1` and when no data is available "for a while") is specified in the
 orthodox RGB hex value, e.g. FF0000 is full red.


