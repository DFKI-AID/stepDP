package de.dfki.step.output;

import de.dfki.step.core.Mode;

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
