package com.mac.compress

import android.media.MediaCodec
import android.media.MediaFormat
import com.googlecode.mp4parser.util.Matrix
import java.io.File


/**
 * @author ex-yangjb001
 * @date 2018/12/5.
 */
class Mp4Movie {
    var matrix = Matrix.ROTATE_0
    val tracks = ArrayList<Track>()
    lateinit var cacheFile: File
    var width: Int = 0
    var height: Int = 0

    fun setRotation(angle: Int) {
        when (angle) {
            0 -> matrix = Matrix.ROTATE_0
            90 -> matrix = Matrix.ROTATE_90
            180 -> matrix = Matrix.ROTATE_180
            270 -> matrix = Matrix.ROTATE_270
        }
    }

    fun setSize(w: Int, h: Int) {
        width = w
        height = h
    }

    fun addSample(trackIndex: Int, offset: Long, bufferInfo: MediaCodec.BufferInfo) {
        if (trackIndex < 0 || trackIndex >= tracks.size) {
            return
        }
        val track = tracks[trackIndex]
        track.addSample(offset.toLong(), bufferInfo)
    }

    fun addTrack(mediaFormat: MediaFormat, isAudio: Boolean): Int {
        tracks.add(Track(tracks.size, mediaFormat, isAudio))
        return tracks.size - 1
    }
}