package com.mac.compress;

import java.nio.ByteBuffer;

/**
 * @author ex-yangjb001
 * @date 2018/12/4.
 */
public class NativeCompress {
    /**
     * @param src
     * @param dest
     * @param destFormat
     * @param width
     * @param height
     * @param padding
     * @param swap
     * @return
     */
    public native static int convertVideoFrame(ByteBuffer src, ByteBuffer dest, int destFormat, int width, int height, int padding, int swap);
}
