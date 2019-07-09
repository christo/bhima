# Bhima

Code, docs and config for Burning Seed camp _Dragon Heart_ artcar bus, _Bhima_.

Quesions etc. to [bhima@chromosundrift.com](mailto:bhima@chromosundrift.com)

## TODO

* Multiple map model - bundle background and pixelMap into its own node
* enable proper GUI by creating outer swing frame with PApplet component
* fix bug: drag points is incorrectly scaled
* Dynamic runtime config of segment wiring
* pixel map editing
* editing of modular maps include rotation, translation, arbitrary quad-skew with retained local coordinates
* test video mapped onto wing surface
* test shape-aware algorithms, non-rectangular edges e.g. shaded dragon body
* effect: mirrorball sparkle scales
* model wiring in bhima map
* load images and mappings automatically
* make imagemapping asynchronous with UI - ability to scroll through images from a run
* render logical pixelpusher wiring in map
* include overlay difference mask / composite images from original scans
* all GUI image scaling with overlay rendering properly registered
* mobile web app - test with chrome on ios DragonProgram selection
* web app should also work on desktop web app
* design logical wiring to make pixelpusher-native pixel mirroring work.
* do a half-dragon video mask
* parametised geometric algorithms (e.g. sacred geometry shiz) using pixelpusher processing library
* test full logical layout with 7.5k LEDs
* test ideas: audio-input, accelerometer input, theremin-style control field?

## Features

* semi-automatic pixel point detection within a camera field
* Config file generator for pixelpushers.
* deployment script for config files
* dragonmind processing module

## Random Notes

* use processing sketch *pixelpusher_tester* for running test patterns.

The WS2811 LED strips we are using on Bhima use colour order "grb" (green, red, blue) but the per-strip start colour (used if the config option `blank_strips_on_idle=1` and when no data is available "for a while") is specified in the orthodox RGB hex value, e.g. FF0000 is full red.


