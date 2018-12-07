package com.mac.compress

import android.media.MediaCodec
import android.media.MediaFormat
import com.coremedia.iso.BoxParser
import com.coremedia.iso.IsoFile
import com.coremedia.iso.IsoTypeWriter
import com.coremedia.iso.boxes.*
import com.googlecode.mp4parser.DataSource
import com.googlecode.mp4parser.util.Matrix
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.channels.WritableByteChannel
import java.util.*

/**
 * @author ex-yangjb001
 * @date 2018/12/5.
 */
class Mp4Builder {
    private var mdat: InterleaveChunkMdat? = null
    private var currentMp4Movie: Mp4Movie? = null
    private var fos: FileOutputStream? = null
    private var fc: FileChannel? = null
    private var dataOffset: Long = 0
    private var writedSinceLastMdat: Long = 0
    private var writeNewMdat = true
    private val track2SampleSizes = HashMap<Track, LongArray>()
    private var sizeBuffer: ByteBuffer? = null

    @Throws(Exception::class)
    fun createMovie(mp4Movie: Mp4Movie): Mp4Builder {
        currentMp4Movie = mp4Movie

        fos = FileOutputStream(mp4Movie.cacheFile)
        fc = fos!!.channel

        val fileTypeBox = createFileTypeBox()
        fileTypeBox.getBox(fc)
        dataOffset += fileTypeBox.size
        writedSinceLastMdat += dataOffset

        mdat = InterleaveChunkMdat()

        sizeBuffer = ByteBuffer.allocateDirect(4)

        return this
    }

    @Throws(Exception::class)
    private fun flushCurrentMdat() {
        val oldPosition = fc!!.position()
        fc!!.position(mdat!!.getOffset())
        mdat!!.getBox(fc!!)
        fc!!.position(oldPosition)
        mdat!!.setDataOffset(0)
        mdat!!.contentSize = 0
        fos!!.flush()
    }

    @Throws(Exception::class)
    fun writeSampleData(trackIndex: Int, byteBuf: ByteBuffer, bufferInfo: MediaCodec.BufferInfo, isAudio: Boolean): Boolean {
        if (writeNewMdat) {
            mdat!!.contentSize = 0
            mdat!!.getBox(this!!.fc!!)
            mdat!!.setDataOffset(dataOffset)
            dataOffset += 16
            writedSinceLastMdat += 16
            writeNewMdat = false
        }

        mdat!!.contentSize = mdat!!.contentSize + bufferInfo.size
        writedSinceLastMdat += bufferInfo.size.toLong()

        var flush = false
        if (writedSinceLastMdat >= 32 * 1024) {
            flushCurrentMdat()
            writeNewMdat = true
            flush = true
            writedSinceLastMdat -= (32 * 1024).toLong()
        }

        currentMp4Movie!!.addSample(trackIndex, dataOffset, bufferInfo)
        byteBuf.position(bufferInfo.offset + if (isAudio) 0 else 4)
        byteBuf.limit(bufferInfo.offset + bufferInfo.size)

        if (!isAudio) {
            sizeBuffer!!.position(0)
            sizeBuffer!!.putInt(bufferInfo.size - 4)
            sizeBuffer!!.position(0)
            fc!!.write(sizeBuffer)
        }

        fc!!.write(byteBuf)
        dataOffset += bufferInfo.size.toLong()

        if (flush) {
            fos!!.flush()
        }
        return flush
    }

    @Throws(Exception::class)
    fun addTrack(mediaFormat: MediaFormat, isAudio: Boolean): Int {
        return currentMp4Movie!!.addTrack(mediaFormat, isAudio)
    }

    @Throws(Exception::class)
    fun finishMovie(error: Boolean) {
        if (mdat!!.contentSize != 0L) {
            flushCurrentMdat()
        }

        for (track in currentMp4Movie!!.tracks) {
            val samples = track.samples
            val sizes = LongArray(samples.size)
            for (i in sizes.indices) {
                sizes[i] = samples[i].size
            }
            track2SampleSizes[track] = sizes
        }

        val moov = createMovieBox(currentMp4Movie!!)
        moov.getBox(fc)
        fos!!.flush()

        fc!!.close()
        fos!!.close()
    }


    private fun createFileTypeBox(): FileTypeBox {
        val minorBrands = LinkedList<String>()
        minorBrands.add("isom")
        minorBrands.add("3gp4")
        return FileTypeBox("isom", 0, minorBrands)
    }

    private inner class InterleaveChunkMdat : Box {
        private var parent: Container? = null
        var contentSize = (1024 * 1024 * 1024).toLong()
        private var dataOffset: Long = 0

        override fun getParent(): Container? {
            return parent
        }

        override fun getOffset(): Long {
            return dataOffset
        }

        fun setDataOffset(offset: Long) {
            dataOffset = offset
        }

        override fun setParent(parent: Container) {
            this.parent = parent
        }

        override fun getType(): String {
            return "mdat"
        }

        override fun getSize(): Long {
            return 16 + contentSize
        }

        private fun isSmallBox(contentSize: Long): Boolean {
            return contentSize + 8 < 4294967296L
        }

        @Throws(IOException::class)
        override fun parse(dataSource: DataSource, header: ByteBuffer, contentSize: Long, boxParser: BoxParser) {

        }

        @Throws(IOException::class)
        override fun getBox(writableByteChannel: WritableByteChannel) {
            val bb = ByteBuffer.allocate(16)
            val size = size
            if (isSmallBox(size)) {
                IsoTypeWriter.writeUInt32(bb, size)
            } else {
                IsoTypeWriter.writeUInt32(bb, 1)
            }
            bb.put(IsoFile.fourCCtoBytes("mdat"))
            if (isSmallBox(size)) {
                bb.put(ByteArray(8))
            } else {
                IsoTypeWriter.writeUInt64(bb, size)
            }
            bb.rewind()
            writableByteChannel.write(bb)
        }
    }

    fun gcd(a: Long, b: Long): Long {
        return if (b == 0L) {
            a
        } else gcd(b, a % b)
    }

    fun getTimescale(mp4Movie: Mp4Movie): Long {
        var timescale: Long = 0
        if (!mp4Movie.tracks.isEmpty()) {
            timescale = mp4Movie.tracks.iterator().next().timeScale.toLong()
        }
        for (track in mp4Movie.tracks) {
            timescale = gcd(track.timeScale.toLong(), timescale)
        }
        return timescale
    }

    private fun createMovieBox(movie: Mp4Movie): MovieBox {
        val movieBox = MovieBox()
        val mvhd = MovieHeaderBox()

        mvhd.creationTime = Date()
        mvhd.modificationTime = Date()
        mvhd.matrix = Matrix.ROTATE_0
        val movieTimeScale = getTimescale(movie)
        var duration: Long = 0

        for (track in movie.tracks) {
            val tracksDuration = track.duration * movieTimeScale / track.timeScale
            if (tracksDuration > duration) {
                duration = tracksDuration
            }
        }

        mvhd.duration = duration
        mvhd.timescale = movieTimeScale
        mvhd.nextTrackId = (movie.tracks.size + 1).toLong()

        movieBox.addBox(mvhd)
        for (track in movie.tracks) {
            movieBox.addBox(createTrackBox(track, movie))
        }
        return movieBox
    }

    fun createTrackBox(track: Track, movie: Mp4Movie): TrackBox {
        val trackBox = TrackBox()
        val tkhd = TrackHeaderBox()

        tkhd.isEnabled = true
        tkhd.isInMovie = true
        tkhd.isInPreview = true
        if (track.mIsAudio) {
            tkhd.matrix = Matrix.ROTATE_0
        } else {
            tkhd.matrix = movie.matrix
        }
        tkhd.alternateGroup = 0
        tkhd.creationTime = track.creationTime
        tkhd.duration = track.duration * getTimescale(movie) / track.timeScale
        tkhd.height = track.height.toDouble()
        tkhd.width = track.width.toDouble()
        tkhd.layer = 0
        tkhd.modificationTime = Date()
        tkhd.trackId = track.trackId + 1
        tkhd.volume = track.volume.toFloat()

        trackBox.addBox(tkhd)

        val mdia = MediaBox()
        trackBox.addBox(mdia)
        val mdhd = MediaHeaderBox()
        mdhd.creationTime = track.creationTime
        mdhd.duration = track.duration
        mdhd.timescale = track.timeScale.toLong()
        mdhd.language = "eng"
        mdia.addBox(mdhd)
        val hdlr = HandlerBox()
        hdlr.name = if (track.mIsAudio) "SoundHandle" else "VideoHandle"
        hdlr.handlerType = track.handler

        mdia.addBox(hdlr)

        val minf = MediaInformationBox()
        minf.addBox(track.headerBox)

        val dinf = DataInformationBox()
        val dref = DataReferenceBox()
        dinf.addBox(dref)
        val url = DataEntryUrlBox()
        url.flags = 1
        dref.addBox(url)
        minf.addBox(dinf)

        val stbl = createStbl(track)
        minf.addBox(stbl)
        mdia.addBox(minf)

        return trackBox
    }

    fun createStbl(track: Track): Box {
        val stbl = SampleTableBox()

        createStsd(track, stbl)
        createStts(track, stbl)
        createStss(track, stbl)
        createStsc(track, stbl)
        createStsz(track, stbl)
        createStco(track, stbl)

        return stbl
    }

    private fun createStsd(track: Track, stbl: SampleTableBox) {
        stbl.addBox(track.sampleDescriptionBox)
    }

    fun createStts(track: Track, stbl: SampleTableBox) {
        var lastEntry: TimeToSampleBox.Entry? = null
        val entries = ArrayList<TimeToSampleBox.Entry>()

        for (delta in track.sampleDurations) {
            if (lastEntry != null && lastEntry.delta == delta) {
                lastEntry.count = lastEntry.count + 1
            } else {
                lastEntry = TimeToSampleBox.Entry(1, delta)
                entries.add(lastEntry)
            }
        }
        val stts = TimeToSampleBox()
        stts.entries = entries
        stbl.addBox(stts)
    }

    fun createStss(track: Track, stbl: SampleTableBox) {
        var syncSamples = track.syncSamples
        if (syncSamples != null && syncSamples!!.size > 0) {
            val stss = SyncSampleBox()
            stss.sampleNumber = track.getSyncSamples()
            stbl.addBox(stss)
        }
    }

    fun createStsc(track: Track, stbl: SampleTableBox) {
        val stsc = SampleToChunkBox()
        stsc.entries = LinkedList()

        var lastOffset: Long = -1
        var lastChunkNumber = 1
        var lastSampleCount = 0

        var previousWritedChunkCount = -1

        val samplesCount = track.samples.size
        for (a in 0 until samplesCount) {
            val sample = track.samples[a]
            val offset = sample.offset
            val size = sample.size

            lastOffset = offset + size
            lastSampleCount++

            var write = false
            if (a != samplesCount - 1) {
                val nextSample = track.samples.get(a + 1)
                if (lastOffset != nextSample.offset) {
                    write = true
                }
            } else {
                write = true
            }
            if (write) {
                if (previousWritedChunkCount != lastSampleCount) {
                    stsc.entries.add(SampleToChunkBox.Entry(lastChunkNumber.toLong(), lastSampleCount.toLong(), 1))
                    previousWritedChunkCount = lastSampleCount
                }
                lastSampleCount = 0
                lastChunkNumber++
            }
        }
        stbl.addBox(stsc)
    }

    fun createStsz(track: Track, stbl: SampleTableBox) {
        val stsz = SampleSizeBox()
        stsz.sampleSizes = track2SampleSizes[track]
        stbl.addBox(stsz)
    }

    fun createStco(track: Track, stbl: SampleTableBox) {
        val chunksOffsets = ArrayList<Long>()
        var lastOffset: Long = -1
        for (sample in track.samples) {
            val offset = sample.offset
            if (lastOffset != (-1).toLong() && lastOffset != offset) {
                lastOffset = -1
            }
            if (lastOffset == (-1).toLong()) {
                chunksOffsets.add(offset)
            }
            lastOffset = offset + sample.size
        }
        val chunkOffsetsLong = LongArray(chunksOffsets.size)
        for (a in chunksOffsets.indices) {
            chunkOffsetsLong[a] = chunksOffsets[a]
        }

        val stco = StaticChunkOffsetBox()
        stco.chunkOffsets = chunkOffsetsLong
        stbl.addBox(stco)
    }
}