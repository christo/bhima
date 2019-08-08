# Dragon Mind Bhima Electronics Control Module

> M.I.N.D: Modular Illuminated Networked Dragon

Is that the acronym? Let's go with yes. In 2018, the primary purpose of this software component is to control
the art car LEDs on the dragon body, head, tail and wings. There are two halves of the body, one on each side of the bus
which should play a mirror image of each other. Both wings are expected to be wholly visible from either side of the bus.

This module includes a Processing sketch for configuring the pixelpushers and also for driving the pixelpushers.

## config file

Plan:

The `cameraMask` defines a bounding polygon outside which all camera input is ignored for the purposes of mapping. This
helps reduce the effects of a certain dog walking in frame while scanning testing is in progress. It is expected to work
for other lifeforms and disturbances.

The possibly esoteric `pixelmap` part of the config file contains a sequence of `segment` objects. Each segment
represents a quadrilateral panel located at some location relative to the whole dragon. Transforms such as `translate`
(x,y array), `scale` (x, y array), `rotate` (float in radians), executed in order, relative to the origin (top left). 
At least that's the plan at the time of writing.

The `pixels` array contains arrays with four elements: strip-number, pixel-number, x co-ordinate, y co-ordinate. All
are zero-based. At this stage strip numbers are globally unique but we might need to add pixelpusher number and group
number as well.

The transforms in the `pixelmap` `segment`s compose the whole model. They also enable software-defined mirroring of
effects by effectively placing one segment over the other, possibly flipping one segment to make mirror symmetry.
Pixelpushers can do their own pixel copying within one pixelpusher by copying pixels on one strip to another. This
requires mirror layout to achieve the mirror imaging. Because Bhima's LED layout is currently incomplete, the constraint
that the pixelpusher-based mirroring puts on us means that software-defined mirroring may be necessary.

There is an optional background image that can be placed for each segment (again, quad warp may not be implemented for
the image).


## Scan Notes

The following scans were done today (roughly in time order)

strip num (scan strip num = port - 1): description
17: Right neck (there is no left)
0: Left tail
1: Left headmost single panel, dodgy w/ 100 ish lights, missing other two due to door
2: Left second headmost trio
3: Left third headmost trio
4: Left second headmost trio 
5: Right third headmost trio
6: Right second headmost trio
7: Right headmost trio
8: Right fourth headmost trio
12: Right fifth headmost trio, curving up and thinning
20: Right tail tip 4 panels (several scans)

from earlier scans:
left wing bottom
left wing top
right wing bottom (slightly dodgy)
right wing top