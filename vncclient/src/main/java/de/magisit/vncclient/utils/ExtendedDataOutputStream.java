package de.magisit.vncclient.utils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ExtendedDataOutputStream extends DataOutputStream {
    // TODO convert to kotlin

    public ExtendedDataOutputStream(OutputStream out) {
        super(out);
    }

    public void writeUInt32(long uint32) throws IOException {
        this.writeInt((int) uint32);
    }

    public void writeUInt16(int uint16) throws IOException {
        this.writeShort(uint16);
    }
}