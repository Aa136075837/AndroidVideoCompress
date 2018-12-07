package com.mac.compress

import android.annotation.TargetApi
import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Build
import com.coremedia.iso.boxes.AbstractMediaHeaderBox
import com.coremedia.iso.boxes.SampleDescriptionBox
import com.coremedia.iso.boxes.SoundMediaHeaderBox
import com.coremedia.iso.boxes.VideoMediaHeaderBox
import com.coremedia.iso.boxes.sampleentry.AudioSampleEntry
import com.coremedia.iso.boxes.sampleentry.VisualSampleEntry
import com.googlecode.mp4parser.boxes.mp4.ESDescriptorBox
import com.googlecode.mp4parser.boxes.mp4.objectdescriptors.AudioSpecificConfig
import com.googlecode.mp4parser.boxes.mp4.objectdescriptors.DecoderConfigDescriptor
import com.googlecode.mp4parser.boxes.mp4.objectdescriptors.ESDescriptor
import com.googlecode.mp4parser.boxes.mp4.objectdescriptors.SLConfigDescriptor
import com.mp4parser.iso14496.part15.AvcConfigurationBox
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author ex-yangjb001
 * @date 2018/12/4.
 */
class Track {
    private val samplingFrequencyIndexMap = HashMap<Int, Int>()
    var trackId = 0L
    var samples = ArrayList<Sample>()
    var duration = 0L
    lateinit var handler: String
    var headerBox: AbstractMediaHeaderBox? = null
    var sampleDescriptionBox: SampleDescriptionBox? = null
    var syncSamples: LinkedList<Int>? = null
    var sampleDurations = ArrayList<Long>()
    var timeScale: Int = 0
    var volume = 0
    var mIsAudio = false
    var width = 0
    var height = 0
    internal var creationTime = Date()
    private var lastPresentationTimeUs = 0L
    private var isFirst = true

    init {
        samplingFrequencyIndexMap[96000] = 0x0
        samplingFrequencyIndexMap[88200] = 0x1
        samplingFrequencyIndexMap[64000] = 0x2
        samplingFrequencyIndexMap[48000] = 0x3
        samplingFrequencyIndexMap[44100] = 0x4
        samplingFrequencyIndexMap[32000] = 0x5
        samplingFrequencyIndexMap[24000] = 0x6
        samplingFrequencyIndexMap[22050] = 0x7
        samplingFrequencyIndexMap[16000] = 0x8
        samplingFrequencyIndexMap[12000] = 0x9
        samplingFrequencyIndexMap[11025] = 0xa
        samplingFrequencyIndexMap[8000] = 0xb
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    constructor(trackId: Int, format: MediaFormat, isAudio: Boolean) {
        this.trackId = trackId.toLong()
        if (!isAudio) {
            sampleDurations.add(3015.toLong())
            duration = 3015
            width = format.getInteger(MediaFormat.KEY_WIDTH)
            height = format.getInteger(MediaFormat.KEY_HEIGHT)
            timeScale = 90000
            syncSamples = LinkedList()
            handler = "video"
            headerBox = VideoMediaHeaderBox()
            sampleDescriptionBox = SampleDescriptionBox()
            var mime = format.getString(MediaFormat.KEY_MIME)
            if ("video/avc" == mime) {
                val visualSampleEntry = VisualSampleEntry("avc1")
                visualSampleEntry.dataReferenceIndex = 1
                visualSampleEntry.depth = 24
                visualSampleEntry.frameCount = 1
                visualSampleEntry.horizresolution = 72.0
                visualSampleEntry.vertresolution = 72.0
                visualSampleEntry.width = width
                visualSampleEntry.height = height
                val avcConfigurationBox = AvcConfigurationBox()
                if (format.getByteBuffer("csd-0") != null) {
                    val spsArray = ArrayList<ByteArray>()
                    val spsBuff = format.getByteBuffer("csd-0")
                    spsBuff.position(4)
                    val spsBytes = ByteArray(spsBuff.remaining())
                    spsBuff.get(spsBytes)
                    spsArray.add(spsBytes)

                    val ppsArray = ArrayList<ByteArray>()
                    val ppsBuff = format.getByteBuffer("csd-1")
                    ppsBuff.position(4)
                    val ppsBytes = ByteArray(ppsBuff.remaining())
                    ppsBuff.get(ppsBytes)
                    ppsArray.add(ppsBytes)
                    avcConfigurationBox.sequenceParameterSets = spsArray
                    avcConfigurationBox.pictureParameterSets = ppsArray
                }
                avcConfigurationBox.avcLevelIndication = 13
                avcConfigurationBox.avcProfileIndication = 100
                avcConfigurationBox.bitDepthLumaMinus8 = -1
                avcConfigurationBox.bitDepthChromaMinus8 = -1
                avcConfigurationBox.chromaFormat = -1
                avcConfigurationBox.configurationVersion = 1
                avcConfigurationBox.lengthSizeMinusOne = 3
                avcConfigurationBox.profileCompatibility = 0

                visualSampleEntry.addBox(avcConfigurationBox)
                sampleDescriptionBox!!.addBox(visualSampleEntry)
            } else if ("video/mp4v" == mime) {
                val visualSampleEntry = VisualSampleEntry("mp4v")
                visualSampleEntry.dataReferenceIndex = 1
                visualSampleEntry.depth = 24
                visualSampleEntry.frameCount = 1
                visualSampleEntry.horizresolution = 72.0
                visualSampleEntry.vertresolution = 72.0
                visualSampleEntry.width = width
                visualSampleEntry.height = height

                sampleDescriptionBox!!.addBox(visualSampleEntry)
            }
        } else {
            sampleDurations.add(1024)
            duration = 1024
            mIsAudio = true
            volume = 1
            timeScale = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            handler = "soun"
            headerBox = SoundMediaHeaderBox()
            sampleDescriptionBox = SampleDescriptionBox()
            val audioSampleEntry = AudioSampleEntry("mp4a")
            audioSampleEntry.channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            audioSampleEntry.sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE).toLong()
            audioSampleEntry.dataReferenceIndex = 1
            audioSampleEntry.sampleSize = 16

            val esds = ESDescriptorBox()
            val descriptor = ESDescriptor()
            descriptor.esId = 0
            val slConfigDescriptor = SLConfigDescriptor()
            slConfigDescriptor.predefined = 2
            descriptor.slConfigDescriptor = slConfigDescriptor

            val decoderConfigDescriptor = DecoderConfigDescriptor()
            decoderConfigDescriptor.objectTypeIndication = 0x40
            decoderConfigDescriptor.streamType = 5
            decoderConfigDescriptor.bufferSizeDB = 1536
            decoderConfigDescriptor.maxBitRate = 96000
            decoderConfigDescriptor.avgBitRate = 96000

            val audioSpecificConfig = AudioSpecificConfig()
            audioSpecificConfig.audioObjectType = 2
            audioSpecificConfig.samplingFrequencyIndex = samplingFrequencyIndexMap[audioSampleEntry.sampleRate.toInt()]!!
            audioSpecificConfig.channelConfiguration = audioSampleEntry.channelCount
            decoderConfigDescriptor.audioSpecificInfo = audioSpecificConfig

            descriptor.decoderConfigDescriptor = decoderConfigDescriptor

            val data = descriptor.serialize()
            esds.esDescriptor = descriptor
            esds.data = data
            audioSampleEntry.addBox(esds)
            sampleDescriptionBox!!.addBox(audioSampleEntry)
        }
    }

    fun addSample(offset: Long, bufferInfo: MediaCodec.BufferInfo) {
        val isSyncFrame = !mIsAudio && bufferInfo.flags and MediaCodec.BUFFER_FLAG_SYNC_FRAME != 0
        samples.add(Sample(offset, bufferInfo.size.toLong()))
        if (syncSamples != null && isSyncFrame) {
            syncSamples!!.add(samples.size)
        }

        var delta = bufferInfo.presentationTimeUs - lastPresentationTimeUs
        lastPresentationTimeUs = bufferInfo.presentationTimeUs
        delta = (delta * timeScale + 500000L) / 1000000L
        if (!isFirst) {
            sampleDurations.add(sampleDurations.size - 1, delta)
            this.duration += delta
        }
        isFirst = false
    }

    fun getSyncSamples(): LongArray? {
        if (syncSamples == null || syncSamples!!.isEmpty()) {
            return null
        }
        val returns = LongArray(syncSamples!!.size)
        for (i in syncSamples!!.indices) {
            returns[i] = syncSamples!!.get(i).toLong()
        }
        return returns
    }
}