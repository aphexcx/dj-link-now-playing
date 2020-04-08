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
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.imageio.ImageIO

val REMOVE_THESE = listOf(
    " - Extended Mix",
    " (Extended Mix)",
    "(Extended Mix)"
)

/**
 * Listens to channel on-air changes from the mixer to write the currently playing track's
 * title, artist and album art to files.
 *
 * Decides on the currently playing track based on the following logic:
 * We only want to display the track title and artist if there's only one on-air player. While that might sound counter-
 * intuitive, it works great because when you're in the mix, you want to keep people guessing as to the incoming track.
 * They only see its title when you're done mixing it in, right as you slam the outgoing channel's fader down. :)
 */
class TrackSource : BeatListener, OnAirListener {

    private var emptyTrack = Track(0, "twitch.tv/aphexcx", "QUARANTRANCE • Episode #4", art = null)
    private var IDTrack = Track(-1, "ID", "ID", art = null)
    private var nowPlayingTrack: Track = emptyTrack
    private val emptyAlbumArt: ByteArray = File("/Users/afik_cohen/obs/image.png").readBytes()
    private val currentlyAudibleChannels: MutableSet<Int> = hashSetOf()
    private var startTime = LocalDateTime.now()
    private val tracklist = mutableMapOf<LocalDateTime, Track>()
    val dateformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_H.mm.ss")

    override fun newBeat(beat: Beat?) {

    }

    override fun channelsOnAir(audibleChannels: MutableSet<Int>?) {
        if (audibleChannels == null) return

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

            val currentTrack = if (metadata.album.label == "ID") {
                IDTrack
            } else {
                Track(
                    id = metadata.trackReference.rekordboxId,
                    title = title,
                    artist = metadata.artist.label,
                    art = art
                )
            }

            //TODO this isn't comparing same tracks correctly? Test this id check
            if (currentTrack.id == nowPlayingTrack.id) {
                return
            }

            println(currentTrack)
            writeNowPlayingToFiles(currentTrack)
            writeToTrackList(currentTrack)
            nowPlayingTrack = currentTrack

        } ?: run {
            if (nowPlayingTrack != emptyTrack) {
                writeNowPlayingToFiles(emptyTrack)
                nowPlayingTrack = emptyTrack
            }
        }
    }

    private fun writeToTrackList(currentTrack: Track) {
        val now = LocalDateTime.now()
        if (tracklist.isEmpty()) {
            startTime = now
        }
        tracklist[now] = currentTrack

        with(File("/Users/afik_cohen/obs/tracklist.${dateformatter.format(startTime)}.txt")) {
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
                ImageIO.write(track.art.image, "png", outputStream())
//                writeBuffer(track.art.image., outputStream())
            }
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


data class Track(val id: Int, val title: String, val artist: String, val art: AlbumArt?) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Track) return false

        if (id != other.id) return false
        if (title != other.title) return false
        if (artist != other.artist) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + title.hashCode()
        result = 31 * result + artist.hashCode()
        return result
    }
}
