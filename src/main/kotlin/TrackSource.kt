package cx.aphex.now_playing

import org.deepsymmetry.beatlink.Beat
import org.deepsymmetry.beatlink.BeatListener
import org.deepsymmetry.beatlink.OnAirListener
import org.deepsymmetry.beatlink.data.AlbumArt
import org.deepsymmetry.beatlink.data.ArtFinder
import org.deepsymmetry.beatlink.data.MetadataFinder
import java.io.File
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.channels.WritableByteChannel
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDateTime
import java.util.*


val REMOVE_THESE = listOf(
    " - Extended Mix",
    " (Extended Mix)",
    "(Extended Mix)"
)


/**
 * Listens to all connected CDJs' beat and tempo updates.
 *
 * We pick the CDJ to sync shows to based on the following priority:
 * 1) The CDJ that is currently on-air (has its fader up on the mixer)
 * 2) If more than one CDJ is on-air, pick the CDJ that is the Tempo Master
 */
class TrackSource() : BeatListener, OnAirListener {

    private var emptyTrack = Track("twitch.tv/aphexcx", "QUARANTRANCE • Episode #3", art = null)
    private var nowPlayingTrack: Track = emptyTrack
    private val emptyAlbumArt: ByteArray = File("/Users/afik_cohen/obs/image.png").readBytes()
    private val currentlyAudibleChannels: MutableSet<Int> = hashSetOf()
    private var startTime = LocalDateTime.now()
    private val tracklist = mutableMapOf<LocalDateTime, Track>()
    val sdf = SimpleDateFormat("H:mm:ss", Locale.getDefault());

//    private val listeners = mutableListOf<(BeatData) -> Unit>()

    override fun newBeat(beat: Beat?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun channelsOnAir(audibleChannels: MutableSet<Int>?) {
        if (audibleChannels == null) return
        //Only works with two channels rn
        val newChannels = audibleChannels - currentlyAudibleChannels
        val latestChannelOnAir: Int? = newChannels.firstOrNull()
        currentlyAudibleChannels.clear()
        currentlyAudibleChannels.addAll(audibleChannels)

        val onlyChannelOnAir = currentlyAudibleChannels.singleOrNull()
        onlyChannelOnAir?.let {
            val metadata = MetadataFinder.getInstance().getLatestMetadataFor(it)
            val art = ArtFinder.getInstance().getLatestArtFor(it)
            var title = metadata.title

            REMOVE_THESE.forEach {
                title = title.replace(it, "")
            }

            val currentTrack = Track(
                title = title,
                artist = metadata.artist.label,
                art = art
            )
            if (currentTrack == nowPlayingTrack) {
                return
            }

            println(currentTrack)
            writeNowPlayingToFiles(currentTrack)
            writeToTrackList(currentTrack)
            nowPlayingTrack = currentTrack

        } ?: run {
            writeNowPlayingToFiles(emptyTrack)
            nowPlayingTrack = emptyTrack
        }
    }

    private fun writeToTrackList(currentTrack: Track) {
        val now = LocalDateTime.now()
        if (tracklist.isEmpty()) {
            startTime = now
        }
        tracklist[now] = currentTrack

        with(File("/Users/afik_cohen/obs/tracklist.txt")) {
            if (!exists()) createNewFile()
            val writer = printWriter()
            tracklist.keys.forEachIndexed { index, tracktime ->
                val elapsed = Duration.between(startTime, tracktime)
                val currentTrack = tracklist[tracktime]
                writer.println(
                    "${index + 1}. ${currentTrack?.artist} - ${currentTrack?.title} ${formatDuration(elapsed)}"
                )
            }
            writer.close()
        }
    }

    private fun writeNowPlayingToFiles(track: Track) {
        with(File("/Users/afik_cohen/obs/nowplaying-artist.txt")) {
            if (!exists()) createNewFile()
            val writer = printWriter()
            writer.println(track.artist)
            writer.close()
        }

        with(File("/Users/afik_cohen/obs/nowplaying-track.txt")) {
            if (!exists()) createNewFile()
            val writer = printWriter()
            writer.println(track.title)
            writer.close()
        }

        with(File("/Users/afik_cohen/obs/art/nowplaying-art.png")) {
            if (!exists()) createNewFile()
//            val writer = writer()
            if (track.art == null) {
                writeBytes(emptyAlbumArt)
            } else {
                writeBuffer(track.art.rawBytes, outputStream())
            }
//            this.cl()
        }
    }

    fun writeBuffer(buffer: ByteBuffer?, stream: OutputStream?) {
        val channel: WritableByteChannel = Channels.newChannel(stream)
        channel.write(buffer)
        channel.close()
    }

    fun formatDuration(duration: Duration): String? {
        val seconds = duration.seconds
        val absSeconds = Math.abs(seconds)
        val positive = String.format(
            "%d:%02d:%02d",
            absSeconds / 3600,
            absSeconds % 3600 / 60,
            absSeconds % 60
        )
        return if (seconds < 0) "-$positive" else positive
    }

}


data class Track(val title: String, val artist: String, val art: AlbumArt?)
