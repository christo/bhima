package com.chromosundrift.bhima.dragonmind.processing;

import org.gstreamer.elements.PlayBin2;
import processing.core.PApplet;
import processing.video.Movie;
import processing.video.Video;

import java.io.File;
import java.net.URI;

public class BhimaMovie extends Movie {

    public BhimaMovie(PApplet parent, String filename) {
        super(parent, filename);
    }

    @Override
    protected void initGStreamer(PApplet parent, String filename) {
        this.parent = parent;
        playbin = null;

        File file;

        //Video.init(); // TODO fork the video library  - can't call this because it's static protected FAAARK!

        // first check to see if this can be read locally from a file.
        try {
            try {
                // first try a local file using the dataPath. usually this will
                // work ok, but sometimes the dataPath is inside a jar file,
                // which is less fun, so this will crap out.
                file = new File(parent.dataPath(filename));
                if (file.exists()) {
                    playbin = new PlayBin2("Movie Player");
                    playbin.setInputFile(file);
                }
            } catch (Exception e) {
            } // ignored

            // read from a file just hanging out in the local folder.
            // this might happen when the video library is used with some
            // other application, or the person enters a full path name
            if (playbin == null) {
                try {
                    file = new File(filename);
                    if (file.exists()) {
                        playbin = new PlayBin2("Movie Player");
                        playbin.setInputFile(file);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (playbin == null) {
                // Try network read...
                for (int i = 0; i < supportedProtocols.length; i++) {
                    if (filename.startsWith(supportedProtocols[i] + "://")) {
                        try {
                            playbin = new PlayBin2("Movie Player");
                            playbin.setURI(URI.create(filename));
                            break;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (SecurityException se) {
            // online, whups. catch the security exception out here rather than
            // doing it three times (or whatever) for each of the cases above.
        }

        if (playbin == null) {
            parent.die("Could not load movie file " + filename, null);
        }

        // we've got a valid movie! let's rock.
        try {
            this.filename = filename; // for error messages

            // register methods
            parent.registerMethod("dispose", this);
            parent.registerMethod("post", this);

            setEventHandlerObject(parent);

            rate = 1.0f;
            frameRate = -1;
            volume = -1;
            sinkReady = false;
            bufWidth = bufHeight = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
