package de.magisit.vncclient.utils;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ExtendedDataInputStream extends DataInputStream {
    public ExtendedDataInputStream(InputStream in) {
        super(in);
    }

    /**
     * This read an unsigned integer
     *
     * @return
     * @throws IOException
     */
    public long readUInt32() throws IOException {
        return 0xFFFFFFFFL & this.readInt();
    }

    /**
     * This reads an array as unsigned bytes.
     * This method isn't very performant. In some cases it might be better to just work with the signed bytes and negative values
     * to improve the speed
     *
     * @param unsignedBytes
     */
    public void readFullyUnsignedBytes(int[] unsignedBytes) throws IOException {
        byte[] signedBytes = new byte[unsignedBytes.length];
        this.readFully(signedBytes);

        for (int i = 0; i < signedBytes.length; i++) {
            unsignedBytes[i] = 0xFF & signedBytes[i];
        }
    }
}
