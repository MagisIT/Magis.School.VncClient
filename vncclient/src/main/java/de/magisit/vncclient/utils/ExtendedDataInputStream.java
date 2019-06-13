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

    public int readCompactLength() throws IOException {
        int[] portion = new int[3];
        portion[0] = this.readUnsignedByte();
        int len = portion[0] & 0x7F;
        if ((portion[0] & 0x80) != 0) {
            portion[1] = this.readUnsignedByte();
            len |= (portion[1] & 0x7F) << 7;
            if ((portion[1] & 0x80) != 0) {
                portion[2] = this.readUnsignedByte();
                len |= (portion[2] & 0xFF) << 14;
            }
        }
        return len;
    }
}
