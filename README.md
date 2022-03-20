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
* make work with recent jdk (jee modules were removed, jaxb etc.)
    * ? can we upgrade dropwizard to fix this?
    * Will upgrading processing help this?
* fix linux 64 bit native libs result in running video (video scaling is currently wrong)
* make system console log (show on mobile app / front panel)
* add to SystemInfo:
    * performance metrics: e.g. render time
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
* parametised geometric algorithms (e.g. sacred geometry shiz) using pixelpusher processing library
* effect: mirrorball sparkle scales
* test shape-aware algorithms, non-rectangular edges e.g. shaded dragon body, shadows, specular highlight
* enable proper GUI by creating outer swing frame with PApplet component
* Dynamic runtime config of segment wiring
* editing of modular maps include rotation, translation, arbitrary quad-skew with retained local coordinates
* all GUI image scaling with overlay rendering properly registered
* test ideas: audio-input, accelerometer input, theremin-style control field?
* better and more flexible server-side video transcoding, shaping, trimming etc. using service wrapping ffmpeg or 
gstreamer

## References

Bookmarks from initial research into using pixelpushers for this project:

* [PixelPusher BetterPixel Singles (10 pack) - PixelPusher &amp; LED Strips](https://www.illumn.com/pixelpusher-and-led-strips/pixelpusher-betterpixel-singles-10-pack.html)
* [PixelPusher LPD8806 RGB LED Strip, IP65 adhesive backing - PixelPusher &amp; LED Strips](https://www.illumn.com/pixelpusher-and-led-strips/pixelpusher-lpd8806-rgb-led-strip-ip65-adhesive-backing.html)
* [PixelPusher 60x32 pixel Video Wall Kit - PixelPusher &amp; LED Strips](https://www.illumn.com/pixelpusher-and-led-strips/60x32-pixel-video-wall-kit.html)
* [PixelPusher WS2801 RGB 2&quot; Pixel Modules 12V - PixelPusher &amp; LED Strips](https://www.illumn.com/pixelpusher-and-led-strips/pixelpusher-ws2801-rgb-2-pixel-modules-12v.html)
* [Heroic Robotics PixelPusher - PixelPusher &amp; LED Strips](https://www.illumn.com/pixelpusher-and-led-strips/heroic-robotics-pixelpusher.html)
* [PixelPusher: a large scale LED controller for Processing - Processing Forum](https://forum.processing.org/one/topic/pixelpusher-a-large-scale-led-controller-for-processing.html)
* [PixelPusher-processing-sketches/pixelpusher_starfield at master · robot-head/PixelPusher-processing-sketches · GitHub](https://github.com/robot-head/PixelPusher-processing-sketches/tree/master/pixelpusher_starfield)
* [GitHub - robot-head/PixelPusher-artnet: ArtNet bridge for PixelPusher](https://github.com/robot-head/PixelPusher-artnet)
* [PixelController « PIXELinvaders](http://pixelinvaders.ch/?page_id=160)
* [poly-gone/PixelPusher.java at master · wurzle/poly-gone · GitHub](https://github.com/wurzle/poly-gone/blob/master/libraries/PixelPusher/src/com/heroicrobot/dropbit/devices/pixelpusher/PixelPusher.java)
* [Light Painting Wand - Hackster.io](https://www.hackster.io/heroic/light-painting-wand-6dd160)
* [Simple Ping Pong (led mapping) +PixelPusher – ArmyRdz](https://armyrdz.wordpress.com/2018/03/16/simple-ping-pong-led-mapping-pixelpusher/)
* [PixelPusher-java/DeviceImpl.java at master · robot-head/PixelPusher-java · GitHub](https://github.com/robot-head/PixelPusher-java/blob/master/src/com/heroicrobot/dropbit/devices/DeviceImpl.java)
* [full hd resolution - Google Search](https://www.google.com.au/search?q=full+hd+resolution&oq=full+hd+&aqs=chrome.1.69i57j0l5.2406j1j7&sourceid=chrome&ie=UTF-8)
* [How to build a flexible LED Curtain display by LED strips T1000S SD card controler Soft Display DIY - YouTube](https://www.youtube.com/watch?v=sbdvrfwr6Sg)
* [Manipulating large number of LED fixtures in MadLight - garageCube](http://forum.garagecube.com/viewtopic.php?t=9497)
* [How to Build a LED Display flexible Soften CHEAP and Easy](http://buildleddisplay.blogspot.com/)
* [elektric-junkys.com | Flexible LED Video Screens | WS2813 | 3D Stretch Ceilings | LED Panels | 3D LED Infinity Mirrors | LED DIY Instructions |](https://elektric-junkys.com/)
* [how to build your own flexible LED Display XXL WS2811 real time Video Transmission 2mX1.5m cortina - YouTube](https://www.youtube.com/watch?v=e-rdgB_19Fg)
* [Giant PixelPusher array - YouTube](https://www.youtube.com/watch?v=8ROekOZQC6g)
* [Pixelpusher video wall visuals to music - YouTube](https://www.youtube.com/watch?v=NIKhsCvsI6M)
* [PixelPusher Hardware Configuration Guide - PixelPusher](https://sites.google.com/a/heroicrobot.com/pixelpusher/home/getting-started)
* [PixelPusher: Intelligent Networked LED Controller by Heroic Robotics, Inc. — Kickstarter](https://www.kickstarter.com/projects/1319139499/pixelpusher)
* [Kreative Software · GitHub](https://github.com/kreativekorp)
* [rainbowdash/Makefile at master · kreativekorp/rainbowdash · GitHub](https://github.com/kreativekorp/rainbowdash/blob/master/RainbowStudio/Makefile)
* [Search · topic:classic-mac-os org:kreativekorp fork:true · GitHub](https://github.com/search?q=topic%3Aclassic-mac-os+org%3Akreativekorp+fork%3Atrue)
* [PixelPusher&#39;s community hub](https://www.hackster.io/pixelpusher)
* [Blinkdom - Hackster.io](https://www.hackster.io/r26d/blinkdom-b690e4)




## Random Notes

The WS2811 LED strips we are using on Bhima use colour order "grb" (green, red, blue) but the per-strip start colour
(used if the config option `blank_strips_on_idle=1` and when no data is available "for a while") is specified in the
 orthodox RGB hex value, e.g. FF0000 is full red.


