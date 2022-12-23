package org.quad.plex;

import ws.schild.jave.FFMPEGLocator;
import java.io.File;

public class LocalFFMPEGLocator extends FFMPEGLocator {

    private final File ffmpegPath;

    public LocalFFMPEGLocator(File ffmpegFile) {
        this.ffmpegPath = ffmpegFile;
    }

    @Override
    protected String getFFMPEGExecutablePath() {
        return ffmpegPath.getAbsolutePath();
    }
}
