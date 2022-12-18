package org.quad.plex;

import java.awt.datatransfer.*;
import java.io.File;

public class FileTransferable implements Transferable {
    private final DataFlavor[] flavors = {DataFlavor.javaFileListFlavor};
    private final File file;

    public FileTransferable(File file) {
        this.file = file;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(DataFlavor.javaFileListFlavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (flavor.equals(DataFlavor.javaFileListFlavor)) {
            return java.util.Collections.singletonList(file);
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }
}
