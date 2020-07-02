package cx.aphex.now_playing

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.toValue
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
import java.nio.file.Paths
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.imageio.ImageIO


/**
 * Listens to channel on-air changes from the mixer to write the currently playing track's
 * title, artist and album art to files.
 *
 * Decides on the currently playing track based on the following logic:
 * We only want to display the track title and artist if there's only one on-air player. While that might sound counter-
 * intuitive, it works great because when you're in the mix, you want to keep people guessing as to the incoming track.
 * They only see its title when you're done mixing it in, right as you slam the outgoing channel's fader down. :)
 */
class TrackSource(config: Config) : BeatListener, OnAirListener {

    private val outputFolder = config.at("output-folder").toValue<String>()

    private val REMOVE_THESE = config.at("remove-these-from-track-titles").toValue<List<String>>()

    private var emptyTrack = config.at("empty-track").toValue<Track>()

    private val emptyAlbumArt: ByteArray = File(config.at("empty-track-album-art-path").toValue<String>()).readBytes()

    private val hidelabel = config.at("hide-track-with-this-album-name").toValue<String>()

    private val IDlabel = config.at("id-track-with-this-album-name").toValue<String>()
    private var IDTrack = Track(
        id = -1,
        title = "ID",
        artist = "ID",
        art = null,
        precedingTrackPlayedAtBpm = null
    )
    private var nowPlayingTrack: Track = emptyTrack
    private val currentlyAudibleChannels: MutableSet<Int> = hashSetOf()
    private var startTime = LocalDateTime.now()
    private val tracklist = mutableMapOf<LocalDateTime, Track>()
    val dateformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_H.mm.ss")

    private var currentBpm: Double? = null

    override fun newBeat(beat: Beat?) {
        currentBpm = beat?.effectiveTempo
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

            val currentTrack = when (metadata.album.label) {
                IDlabel -> IDTrack
                hidelabel -> emptyTrack
                else -> {
                    Track(
                        id = metadata.trackReference.rekordboxId,
                        title = title,
                        artist = metadata.artist.label,
                        art = art,
                        precedingTrackPlayedAtBpm = currentBpm
                    )
                }
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

        with(Paths.get(outputFolder, "tracklist.${dateformatter.format(startTime)}.txt").toFile()) {
            if (!exists()) createNewFile()
            val writer = printWriter()
            tracklist.keys.forEachIndexed { index, tracktime ->
                val elapsed = Duration.between(startTime, tracktime)
                val track = tracklist[tracktime]
                writer.println(
                    "${index + 1}. ${track?.artist} - ${track?.title} ${formatDuration(elapsed)}"
                )
            }
            writer.close()
        }

        with(Paths.get(outputFolder, "tracklist.bpm.${dateformatter.format(startTime)}.txt").toFile()) {
            if (!exists()) createNewFile()
            val writer = printWriter()
            tracklist.keys.forEachIndexed { index, tracktime ->
                val elapsed = Duration.between(startTime, tracktime)
                val track = tracklist[tracktime]
                writer.println(
                    "${index + 1}. ${track?.artist} - ${track?.title} ${formatDuration(elapsed)} |${track?.precedingTrackPlayedAtBpm}"
                )
            }
            writer.close()
        }
    }

    private fun writeNowPlayingToFiles(track: Track) {
        with(Paths.get(outputFolder, "nowplaying-artist.txt").toFile()) {
            if (!exists()) createNewFile()
            val writer = printWriter()
            writer.println(track.artist)
            writer.close()
        }

        with(Paths.get(outputFolder, "nowplaying-track.txt").toFile()) {
            if (!exists()) createNewFile()
            val writer = printWriter()
            writer.println(track.title)
            writer.close()
        }

        with(Paths.get(outputFolder, "art/nowplaying-art.png").toFile()) {
            if (!exists()) createNewFile()
            if (track.art == null) {
                writeBytes(emptyAlbumArt)
            } else {
                ImageIO.write(track.art.image, "png", outputStream())
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

data class Track(
    val id: Int,
    val title: String,
    val artist: String,
    val art: AlbumArt?,
    val precedingTrackPlayedAtBpm: Double?
) {

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
