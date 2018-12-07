package com.mac.compress

import android.annotation.TargetApi
import android.media.*
import android.os.Build
import android.util.Log
import com.mac.compress.NativeCompress.convertVideoFrame
import java.io.File
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author ex-yangjb001
 * @date 2018/12/4.
 */
class MediaController {
    var path: String = ""

    val MIME_TYPE = "video/avc"
    private val PROCESSOR_TYPE_OTHER = 0
    private val PROCESSOR_TYPE_QCOM = 1
    private val PROCESSOR_TYPE_INTEL = 2
    private val PROCESSOR_TYPE_MTK = 3
    private val PROCESSOR_TYPE_SEC = 4
    private val PROCESSOR_TYPE_TI = 5

    //Default values
    private val DEFAULT_VIDEO_WIDTH = 640
    private val DEFAULT_VIDEO_HEIGHT = 360
    private val DEFAULT_VIDEO_BITRATE = 450000

    private var videoConvertFirstWrite = true

    interface CompressProgressListener {
        fun onProgress(percent: Float)
    }

    companion object {

        var cachedFile: File? = null
        @Volatile
        private var mInstance: MediaController? = null

        fun getInstance(): MediaController {
            var localInstance: MediaController? = null
            if (localInstance == null) {
                synchronized(MediaController.javaClass) {
                    localInstance = mInstance
                    if (localInstance == null) {
                        mInstance = MediaController()
                        localInstance = mInstance
                    }
                }
            }
            return localInstance!!
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        fun selectColorFormat(codecInfo: MediaCodecInfo, mimeType: String): Int {
            val capabilities = codecInfo.getCapabilitiesForType(mimeType)
            var lastColorFromat = 0
            for (i in 0..capabilities.colorFormats.size) {
                val colorFormat = capabilities.colorFormats[i]
                if (isRecognizedFormat(colorFormat)) {
                    lastColorFromat = colorFormat
                    if (!("OMX.SEC.AVC.Encoder" == codecInfo.name && colorFormat == 19)) {
                        return colorFormat
                    }
                }
            }
            return lastColorFromat
        }

        private fun isRecognizedFormat(colorFormat: Int): Boolean {
            return when (colorFormat) {
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar -> true
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar -> true
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar -> true
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar -> true
                MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar -> true
                else -> false
            }
        }
    }

    class VideoConvertRunnable(videoPath: String, dest: File) : Runnable {

        companion object {
            fun runConversion(videoPath: String, dest: File) {
                Thread(Runnable {
                    try {
                        val wrapper = VideoConvertRunnable(videoPath, dest)
                        val th = Thread(wrapper, "VideoConvertRunnable")
                        th.start()
                        th.join()
                    } catch (e: Exception) {
                        Log.e("MediaController", e.message)
                    }
                }).start()
            }
        }

        override fun run() {
            MediaController.mInstance
        }

    }

    fun convertVideo(sourcePath: String, destDir: File, listener: CompressProgressListener): Boolean {
        return convertVideo(sourcePath, destDir, 0, 0, 0, listener)
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    fun convertVideo(sourcePath: String, destDir: File, outWidth: Int, outHeight: Int, outBitrate: Int, listener: CompressProgressListener): Boolean {
        this.path = sourcePath
        var retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        val originalWidth = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT).toInt()
        val originalHeight = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH).toInt()
        var rotationValue = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION).toInt()
        val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toLong() * 1000

        val startTime = -1L
        val endTime = -1L

        var resultWidth = if (outWidth > 0) {
            outWidth
        } else {
            DEFAULT_VIDEO_WIDTH
        }

        var resultHeight = if (outHeight > 0) {
            outHeight
        } else {
            DEFAULT_VIDEO_HEIGHT
        }

        val bitrate = if (outBitrate > 0) {
            outBitrate
        } else {
            DEFAULT_VIDEO_BITRATE
        }
        var rotateRender = 0

        val cacheFile = File(destDir, "VIDEO_" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date()) + ".mp4")

        if (Build.VERSION.SDK_INT < 18 && resultHeight > resultWidth && resultWidth != originalWidth && resultHeight != originalHeight) {
            val temp = resultHeight
            resultHeight = resultWidth
            resultWidth = temp
            rotationValue = 90
            rotateRender = 270
        } else if (Build.VERSION.SDK_INT > 20) {
            if (rotationValue == 90) {
                val temp = resultHeight
                resultHeight = resultWidth
                resultWidth = temp
                rotationValue = 0
                rotateRender = 270
            } else if (rotationValue == 180) {
                rotateRender = 180
                rotationValue = 0
            } else if (rotationValue == 270) {
                val temp = resultHeight
                resultHeight = resultWidth
                resultWidth = temp
                rotationValue = 0
                rotateRender = 90
            }
        }
        val inputFile = File(path)
        if (!inputFile.canRead()) {
            didWriteData(true, true)
            return false
        }
        videoConvertFirstWrite = true
        var error = false
        var videoStartTime = startTime
        val time = System.currentTimeMillis()
        if (resultWidth != 0 && resultHeight != 0) {
            val mediaMuxer: Mp4Builder? = null
            val extractor: MediaExtractor? = null

            try {
                val info = MediaCodec.BufferInfo()
                val movie = Mp4Movie()
                movie.cacheFile = cacheFile
                movie.setRotation(rotationValue)
                movie.setSize(resultWidth, resultHeight)
                val mediaMuxer = Mp4Builder().createMovie(movie)
                val extractor = MediaExtractor()
                extractor.setDataSource(inputFile.toString())

                if (resultWidth != originalWidth || resultHeight != originalHeight) {
                    var videoIndex: Int
                    videoIndex = selectTrack(extractor, false)

                    if (videoIndex >= 0) {
                        var decoder: MediaCodec? = null
                        var encoder: MediaCodec? = null
                        var inputSurface: InputSurface? = null
                        var outputSurface: OutputSurface? = null

                        try {
                            var videoTime = -1L
                            var outputDone = false
                            var inputDone = false
                            var decoderDone = false
                            var swapUV = 0
                            var videoTrackIndex = -5

                            var colorFormat: Int
                            var processorType = PROCESSOR_TYPE_OTHER
                            var manufacturer = Build.MANUFACTURER.toLowerCase()

                            if (Build.VERSION.SDK_INT < 18) {
                                val codecInfo = selectCodec(MIME_TYPE)
                                colorFormat = selectColorFormat(codecInfo!!, MIME_TYPE)
                                if (colorFormat == 0) {
                                    throw RuntimeException("")
                                }
                                val codecName = codecInfo.name
                                if (codecName.contains("OMX.qcom.")) {
                                    processorType = PROCESSOR_TYPE_QCOM
                                    if (Build.VERSION.SDK_INT == 16) {
                                        if (manufacturer == "lge" || manufacturer == "nokia") {
                                            swapUV = 1
                                        }
                                    }
                                } else if (codecName.contains("OMX.Intel.")) {
                                    processorType = PROCESSOR_TYPE_INTEL
                                } else if (codecName == "OMX.MTK.VIDEO.ENCODER.AVC") {
                                    processorType = PROCESSOR_TYPE_MTK
                                } else if (codecName == "OMX.SEC.AVC.Encoder") {
                                    processorType = PROCESSOR_TYPE_SEC
                                    swapUV = 1
                                } else if (codecName == "OMX.TI.DUCATI1.VIDEO.H264E") {
                                    processorType = PROCESSOR_TYPE_TI
                                }
                            } else {
                                colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
                            }

                            var resultHeightAligned = resultHeight
                            var padding = 0
                            var bufferSize = resultWidth * resultHeight * 3 / 2

                            if (processorType == PROCESSOR_TYPE_OTHER) {
                                if (resultHeight % 16 != 0) {
                                    resultHeightAligned += 16 - resultHeight % 16
                                    padding = resultWidth * (resultHeightAligned - resultHeight)
                                    bufferSize += padding * 5 / 4
                                }
                            } else if (processorType == PROCESSOR_TYPE_QCOM) {
                                if (manufacturer.toLowerCase() != "lge") {
                                    val uvoffset = resultWidth * resultHeight + 2047 and 2047.inv()
                                    padding = uvoffset - resultWidth * resultHeight
                                    bufferSize += padding
                                }
                            } else if (processorType == PROCESSOR_TYPE_TI) {
                                //resultHeightAligned = 368;
                                //bufferSize = resultWidth * resultHeightAligned * 3 / 2;
                                //resultHeightAligned += (16 - (resultHeight % 16));
                                //padding = resultWidth * (resultHeightAligned - resultHeight);
                                //bufferSize += padding * 5 / 4;
                            } else if (processorType == PROCESSOR_TYPE_MTK) {
                                if (manufacturer == "baidu") {
                                    resultHeightAligned += 16 - resultHeight % 16
                                    padding = resultWidth * (resultHeightAligned - resultHeight)
                                    bufferSize += padding * 5 / 4
                                }
                            }
                            extractor.selectTrack(videoIndex)
                            if (startTime > 0) {
                                extractor.seekTo(startTime.toLong(), MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
                            } else {
                                extractor.seekTo(0, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
                            }
                            val inputFormat = extractor.getTrackFormat(videoIndex)
                            val outputFormat = MediaFormat.createVideoFormat(MIME_TYPE, resultWidth, resultHeight)
                            outputFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat)
                            outputFormat.setInteger(MediaFormat.KEY_BIT_RATE, if (bitrate != 0) bitrate else 921600)
                            outputFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 25)
                            outputFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10)
                            if (Build.VERSION.SDK_INT < 18) {
                                outputFormat.setInteger("stride", resultWidth + 32)
                                outputFormat.setInteger("slice-height", resultHeight)
                            }
                            encoder = MediaCodec.createEncoderByType(MIME_TYPE)
                            encoder.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                            if (Build.VERSION.SDK_INT >= 18) {
                                inputSurface = InputSurface(encoder.createInputSurface())
                                inputSurface.makeCurrent()
                            }
                            encoder.start()

                            decoder = MediaCodec.createDecoderByType(inputFormat.getString(MediaFormat.KEY_MIME))
                            if (Build.VERSION.SDK_INT >= 18) {
                                outputSurface = OutputSurface()
                            } else {
                                outputSurface = OutputSurface(resultWidth, resultHeight, rotateRender)
                            }
                            decoder.configure(inputFormat, outputSurface.getSurface(), null, 0)
                            decoder.start()

                            val TIMEOUT_USEC = 2500
                            var decoderInputBuffers: Array<ByteBuffer>? = null
                            var encoderOutputBuffers: Array<ByteBuffer>? = null
                            var encoderInputBuffers: Array<ByteBuffer>? = null
                            if (Build.VERSION.SDK_INT < 21) {
                                decoderInputBuffers = decoder.getInputBuffers()
                                encoderOutputBuffers = encoder.getOutputBuffers()
                                if (Build.VERSION.SDK_INT < 18) {
                                    encoderInputBuffers = encoder.getInputBuffers()
                                }
                            }
                            while (!outputDone) {
                                if (!inputDone) {
                                    var eof = false
                                    val index = extractor.sampleTrackIndex
                                    if (index == videoIndex) {
                                        val inputBufIndex = decoder.dequeueInputBuffer(TIMEOUT_USEC.toLong())
                                        if (inputBufIndex >= 0) {
                                            val inputBuf: ByteBuffer?
                                            if (Build.VERSION.SDK_INT < 21) {
                                                inputBuf = decoderInputBuffers!![inputBufIndex]
                                            } else {
                                                inputBuf = decoder.getInputBuffer(inputBufIndex)
                                            }
                                            val chunkSize = extractor.readSampleData(inputBuf!!, 0)
                                            if (chunkSize < 0) {
                                                decoder.queueInputBuffer(inputBufIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                                                inputDone = true
                                            } else {
                                                decoder.queueInputBuffer(inputBufIndex, 0, chunkSize, extractor.sampleTime, 0)
                                                extractor.advance()
                                            }
                                        }
                                    } else if (index == -1) {
                                        eof = true
                                    }
                                    if (eof) {
                                        val inputBufIndex = decoder.dequeueInputBuffer(TIMEOUT_USEC.toLong())
                                        if (inputBufIndex >= 0) {
                                            decoder.queueInputBuffer(inputBufIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                                            inputDone = true
                                        }
                                    }
                                }

                                var decoderOutputAvailable = !decoderDone
                                var encoderOutputAvailable = true
                                while (decoderOutputAvailable || encoderOutputAvailable) {
                                    val encoderStatus = encoder.dequeueOutputBuffer(info, TIMEOUT_USEC.toLong())
                                    if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                                        encoderOutputAvailable = false
                                    } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                                        if (Build.VERSION.SDK_INT < 21) {
                                            encoderOutputBuffers = encoder.outputBuffers
                                        }
                                    } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                                        val newFormat = encoder.outputFormat
                                        if (videoTrackIndex == -5) {
                                            videoTrackIndex = mediaMuxer.addTrack(newFormat, false)
                                        }
                                    } else if (encoderStatus < 0) {
                                        throw RuntimeException("unexpected result from encoder.dequeueOutputBuffer: $encoderStatus")
                                    } else {
                                        val encodedData: ByteBuffer?
                                        if (Build.VERSION.SDK_INT < 21) {
                                            encodedData = encoderOutputBuffers!![encoderStatus]
                                        } else {
                                            encodedData = encoder.getOutputBuffer(encoderStatus)
                                        }
                                        if (encodedData == null) {
                                            throw RuntimeException("encoderOutputBuffer $encoderStatus was null")
                                        }
                                        if (info.size > 1) {
                                            if (info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG == 0) {
                                                if (mediaMuxer.writeSampleData(videoTrackIndex, encodedData, info, false)) {
                                                    didWriteData(false, false)
                                                }
                                            } else if (videoTrackIndex == -5) {
                                                val csd = ByteArray(info.size)
                                                encodedData.limit(info.offset + info.size)
                                                encodedData.position(info.offset)
                                                encodedData.get(csd)
                                                var sps: ByteBuffer? = null
                                                var pps: ByteBuffer? = null
                                                for (a in info.size - 1 downTo 0) {
                                                    if (a > 3) {
                                                        if (csd[a].toInt() == 1 && csd[a - 1].toInt() == 0 && csd[a - 2].toInt() == 0 && csd[a - 3].toInt() == 0) {
                                                            sps = ByteBuffer.allocate(a - 3)
                                                            pps = ByteBuffer.allocate(info.size - (a - 3))
                                                            sps!!.put(csd, 0, a - 3).position(0)
                                                            pps!!.put(csd, a - 3, info.size - (a - 3)).position(0)
                                                            break
                                                        }
                                                    } else {
                                                        break
                                                    }
                                                }

                                                val newFormat = MediaFormat.createVideoFormat(MIME_TYPE, resultWidth, resultHeight)
                                                if (sps != null && pps != null) {
                                                    newFormat.setByteBuffer("csd-0", sps)
                                                    newFormat.setByteBuffer("csd-1", pps)
                                                }
                                                videoTrackIndex = mediaMuxer.addTrack(newFormat, false)
                                            }
                                        }
                                        outputDone = info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0
                                        encoder.releaseOutputBuffer(encoderStatus, false)
                                    }
                                    if (encoderStatus != MediaCodec.INFO_TRY_AGAIN_LATER) {
                                        continue
                                    }

                                    if (!decoderDone) {
                                        val decoderStatus = decoder.dequeueOutputBuffer(info, TIMEOUT_USEC.toLong())
                                        if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                                            decoderOutputAvailable = false
                                        } else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {

                                        } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                                            val newFormat = decoder.outputFormat
                                            Log.e("tmessages", "newFormat = $newFormat")
                                        } else if (decoderStatus < 0) {
                                            throw RuntimeException("unexpected result from decoder.dequeueOutputBuffer: $decoderStatus")
                                        } else {
                                            var doRender: Boolean
                                            if (Build.VERSION.SDK_INT >= 18) {
                                                doRender = info.size != 0
                                            } else {
                                                doRender = info.size != 0 || info.presentationTimeUs != 0L
                                            }
                                            if (endTime > 0 && info.presentationTimeUs >= endTime) {
                                                inputDone = true
                                                decoderDone = true
                                                doRender = false
                                                info.flags = info.flags or MediaCodec.BUFFER_FLAG_END_OF_STREAM
                                            }
                                            if (startTime > 0 && videoTime == -1L) {
                                                if (info.presentationTimeUs < startTime) {
                                                    doRender = false
                                                    Log.e("tmessages", "drop frame startTime = " + startTime + " present time = " + info.presentationTimeUs)
                                                } else {
                                                    videoTime = info.presentationTimeUs
                                                }
                                            }
                                            decoder.releaseOutputBuffer(decoderStatus, doRender)
                                            if (doRender) {
                                                var errorWait = false
                                                try {
                                                    outputSurface.awaitNewImage()
                                                } catch (e: Exception) {
                                                    errorWait = true
                                                    Log.e("tmessages", e.message)
                                                }

                                                if (!errorWait) {
                                                    if (Build.VERSION.SDK_INT >= 18) {
                                                        outputSurface.drawImage(false)
                                                        inputSurface!!.setPresentationTime(info.presentationTimeUs * 1000)
                                                        if (listener != null) {
                                                            listener.onProgress(info.presentationTimeUs.toFloat() / duration.toFloat() * 100)
                                                        }
                                                        inputSurface.swapBuffers()
                                                    } else {
                                                        val inputBufIndex = encoder.dequeueInputBuffer(TIMEOUT_USEC.toLong())
                                                        if (inputBufIndex >= 0) {
                                                            outputSurface.drawImage(true)
                                                            val rgbBuf = outputSurface.getFrame()
                                                            val yuvBuf = encoderInputBuffers!![inputBufIndex]
                                                            yuvBuf.clear()
                                                            convertVideoFrame(rgbBuf, yuvBuf, colorFormat, resultWidth, resultHeight, padding, swapUV)
                                                            encoder.queueInputBuffer(inputBufIndex, 0, bufferSize, info.presentationTimeUs, 0)
                                                        } else {
                                                            Log.e("tmessages", "input buffer not available")
                                                        }
                                                    }
                                                }
                                            }
                                            if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                                                decoderOutputAvailable = false
                                                Log.e("tmessages", "decoder stream end")
                                                if (Build.VERSION.SDK_INT >= 18) {
                                                    encoder.signalEndOfInputStream()
                                                } else {
                                                    val inputBufIndex = encoder.dequeueInputBuffer(TIMEOUT_USEC.toLong())
                                                    if (inputBufIndex >= 0) {
                                                        encoder.queueInputBuffer(inputBufIndex, 0, 1, info.presentationTimeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (videoTime != -1L) {
                                videoStartTime = videoTime
                            }
                        } catch (e: Exception) {
                            Log.e("tmessages", e.message)
                            error = true
                        }
                        extractor.unselectTrack(videoIndex)

                        if (outputSurface != null) {
                            outputSurface.release()
                        }
                        if (inputSurface != null) {
                            inputSurface.release()
                        }
                        if (decoder != null) {
                            decoder.stop()
                            decoder.release()
                        }
                        if (encoder != null) {
                            encoder.stop()
                            encoder.release()
                        }
                    }
                } else {
                    val videoTime = readAndWriteTrack(extractor, mediaMuxer, info, startTime, endTime, cacheFile, false)
                    if (videoTime != -1L) {
                        videoStartTime = videoTime
                    }
                }
                if (error) {
                    readAndWriteTrack(extractor, mediaMuxer, info, videoStartTime, endTime, cacheFile, true)
                }
            } catch (e: Exception) {
                error = true
                Log.e("tmessages", e.message)
            } finally {
                if (extractor != null) {
                    extractor.release()
                }
                if (mediaMuxer != null) {
                    try {
                        mediaMuxer.finishMovie(false)
                    } catch (e: Exception) {
                        Log.e("tmessages", e.message)
                    }
                }
                Log.e("tmessages", "time = " + (System.currentTimeMillis() - time))
            }
        } else {
            didWriteData(true, true)
            return false
        }
        didWriteData(true, error)
        cachedFile = cacheFile
        Log.e("ViratPath", path + "")
        Log.e("ViratPath", cacheFile.path + "")
        Log.e("ViratPath", inputFile.path + "")
        return true
    }

    @TargetApi(16)
    @Throws(Exception::class)
    private fun readAndWriteTrack(extractor: MediaExtractor, mediaMuxer: Mp4Builder, info: MediaCodec.BufferInfo, start: Long, end: Long, file: File, isAudio: Boolean): Long {
        val trackIndex = selectTrack(extractor, isAudio)
        if (trackIndex >= 0) {
            extractor.selectTrack(trackIndex)
            val trackFormat = extractor.getTrackFormat(trackIndex)
            val muxerTrackIndex = mediaMuxer.addTrack(trackFormat, isAudio)
            val maxBufferSize = trackFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
            var inputDone = false
            if (start > 0) {
                extractor.seekTo(start, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
            } else {
                extractor.seekTo(0, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
            }
            val buffer = ByteBuffer.allocateDirect(maxBufferSize)
            var startTime: Long = -1
            while (!inputDone) {
                var eof = false
                val index = extractor.sampleTrackIndex
                if (index == trackIndex) {
                    info.size = extractor.readSampleData(buffer, 0)

                    if (info.size < 0) {
                        info.size = 0
                        eof = true
                    } else {
                        info.presentationTimeUs = extractor.sampleTime
                        if (start > 0 && startTime == -1L) {
                            startTime = info.presentationTimeUs
                        }
                        if (end < 0 || info.presentationTimeUs < end) {
                            info.offset = 0
                            info.flags = extractor.sampleFlags
                            if (mediaMuxer.writeSampleData(muxerTrackIndex, buffer, info, isAudio)) {
                                // didWriteData(messageObject, file, false, false);
                            }
                            extractor.advance()
                        } else {
                            eof = true
                        }
                    }
                } else if (index == -1) {
                    eof = true
                }
                if (eof) {
                    inputDone = true
                }
            }
            extractor.unselectTrack(trackIndex)
            return startTime
        }
        return -1
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun selectTrack(extractor: MediaExtractor, b: Boolean): Int {
        val numTracks = extractor.trackCount
        for (i in 0..numTracks) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (b) {
                if (mime.startsWith("audio/")) {
                    return i
                }
            } else {
                if (mime.startsWith("video/")) {
                    return i
                }
            }
        }
        return -5
    }

    fun didWriteData(last: Boolean, error: Boolean) {
        val firstWrite = videoConvertFirstWrite
        if (firstWrite) {
            videoConvertFirstWrite = false
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    fun selectCodec(mimeType: String): MediaCodecInfo? {
        val numCodecs = MediaCodecList.getCodecCount()
        var lastCodecInfo: MediaCodecInfo? = null
        for (i in 0 until numCodecs) {
            val codecInfo = MediaCodecList.getCodecInfoAt(i)
            if (!codecInfo.isEncoder) {
                continue
            }
            val types = codecInfo.supportedTypes
            for (type in types) {
                if (type.equals(mimeType, ignoreCase = true)) {
                    lastCodecInfo = codecInfo
                    if (lastCodecInfo!!.name != "OMX.SEC.avc.enc") {
                        return lastCodecInfo
                    } else if (lastCodecInfo.name == "OMX.SEC.AVC.Encoder") {
                        return lastCodecInfo
                    }
                }
            }
        }
        return lastCodecInfo
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    fun selectColorFormat(codecInfo: MediaCodecInfo, mimeType: String): Int {
        val capabilities = codecInfo.getCapabilitiesForType(mimeType)
        var lastColorFormat = 0
        for (i in capabilities.colorFormats.indices) {
            val colorFormat = capabilities.colorFormats[i]
            if (isRecognizedFormat(colorFormat)) {
                lastColorFormat = colorFormat
                if (!(codecInfo.name == "OMX.SEC.AVC.Encoder" && colorFormat == 19)) {
                    return colorFormat
                }
            }
        }
        return lastColorFormat
    }
}