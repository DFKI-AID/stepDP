package de.dfki.tocalog.output;

import de.dfki.tocalog.model.Mode;

import java.io.File;

/**
 */
public class ImageOutput implements Output {
    private File file;

    public ImageOutput(File file) {
        this.file = file;
    }

    @Override
    public Mode getMode() {
        return Mode.Vision;
    }

    public File getFile() {
        return file;
    }
}
