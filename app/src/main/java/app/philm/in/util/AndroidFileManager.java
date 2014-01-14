package app.philm.in.util;

import com.google.common.base.Preconditions;

import java.io.File;

public class AndroidFileManager implements FileManager {

    private final File mBaseDir;

    public AndroidFileManager(File baseDir) {
        mBaseDir = Preconditions.checkNotNull(baseDir, "baseDir cannot be null");
    }

    @Override
    public File getFile(String filename) {
        return new File(mBaseDir, filename);
    }
}
