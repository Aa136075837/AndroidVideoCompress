package com.mac.androidvideocompress;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class BufferedByteArrayOutputStream extends ByteArrayOutputStream {
    public BufferedByteArrayOutputStream() {
    }

    public BufferedByteArrayOutputStream(int size) {
        super(size);
    }

    public boolean switchBuffer(byte[] newBuf) {
        if(newBuf != null && newBuf.length == this.buf.length) {
            byte[] o = this.buf;
            this.buf = newBuf;
            newBuf = (byte[])((byte[])o);
            return true;
        } else {
            return false;
        }
    }

    public byte[] getBuffer() {
        return this.buf;
    }

    public int getBufferSize() {
        return this.buf.length;
    }

    public void write(ByteBuffer buffer) throws IOException {
        this.write(buffer, buffer.limit());
    }

    public void write(ByteBuffer buffer, int size) throws IOException {
        if(this.buf.length - this.count >= size) {
            buffer.get(this.buf, this.count, size);
            this.count += size;
        } else {
            byte[] outData = new byte[size];
            buffer.get(outData);
            this.write(outData);
        }

    }
}
