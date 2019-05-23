package de.magisit.vncclient.utils;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ExtendedDataInputStream extends DataInputStream {
    // TODO convert to kotlin

    public ExtendedDataInputStream(InputStream in) {
        super(in);
    }

    public long readUInt32() throws IOException {
        return 0xFFFFFFFFL & this.readInt();
    }

    public void readFullyUnsignedBytes(int[] unsignedBytes) throws IOException {
        byte[] signedBytes = new byte[unsignedBytes.length];
        this.readFully(signedBytes);

        for (int i = 0; i < signedBytes.length; i++) {
            unsignedBytes[i] = 0xFF & signedBytes[i];
        }
    }
}
