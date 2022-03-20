# Bhima

Code, docs and config for Burning Seed camp _Bhima the Dragon_ artcar bus, _Bhima_. #burningseed #burningman #artcar

Questions etc. to [bhima@chromosundrift.com](mailto:bhima@chromosundrift.com)

## Summary

* Software-controlled LED surface for the dragon bus
    * Video player
* Video import from USB
* Mobile web app for primary control
* semi-automatic pixel point detection within a camera field
* Config file generator for pixelpushers.
* deployment script for config files

## Installation

* Installation on Linux relies on java and libgstreamer being installed (normal on ubuntu) as well as a gstreamer plugin (seemingly not all installed by default) which can be installed with:
    
    sudo apt install default-jdk gstreamer1.0-libav ubuntu-restricted-extras

## Architecture

* Java application
    * Processing used as application API
    * Pixelpusher LED controllers drive lights
* "API" module contains common code and interfaces 

## Building

The main build tool is `gradle` with a root module and the following submodules:
* api : common module that contains interfaces and commonly used components between `dragonmind` and `dragonmind-web`.
* dragonmind : main implementation module for driving the dragon LEDs 
* dragonmind-web : web app including mobile web app for control

To build with gradle at the command line from the root:

```
gradle build
```

Or to build a production distribution that can be deployed with the deployment shell script, run: 

```
gradle dragonmind:distTar
```

To run tests:

```
gradle test
```

To see other build targets (mostly standard gradle things):
```
gradle tasks
```

Building in IntelliJ should be a matter of maintaining synchronisation with gradle. Building with other IDEs is 
assumed to work the same way.

## Dependencies

* java 1.8 : (back off! this is imposed solely by processing which has been a total pain in the neck)
* processing : library for 2d graphics and application framework
* processing-video : library that extends processing with the ability to process video files
* junit : unit testing
* slf4j : logging API
* dropwizard : web api for dragonmind-web
* onsen : js ui framework for dragonmind-web
* jquery : boomer js library

---

## TODO

* configure production logging to file
* make PusherMan monitor pixelpushers in case new ones come online after app starts
* make work with recent jdk (jee modules were removed, jaxb etc.)
    * ? can we upgrade dropwizard to fix this?
    * Will upgrading processing help this?
* build: gradle build should make an executable that runs on linux or mac for default config.
    * confirm linux 64 bit native libs result in running video (paths etc)
* make system console log (show on mobile app / front panel)
* add to SystemInfo:
    * performance metrics: e.g. render time
    * led drivers connected, latency?
    * temperatures?
    * database size and disk usage
    * network info
    * runtime info e.g. os, ram usage etc.

### Mobile web app
    
* add instructions and status for stick slurper to mobile web app so people can add content
* add video file uploader
* security - authentication

### Main System

* move test pattern into its own Program
* effect: rotozoomer (tesselation, kaliedoscope)
* performance tuning for rpi target (try pi4, it's probably memory bound)
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
* VJ proposal: implement NDI stream sink for display


## Random Notes

The WS2811 LED strips we are using on Bhima use colour order "grb" (green, red, blue) but the per-strip start colour
(used if the config option `blank_strips_on_idle=1` and when no data is available "for a while") is specified in the
 orthodox RGB hex value, e.g. FF0000 is full red.


