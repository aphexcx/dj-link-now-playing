package cx.aphex.now_playing

import Track
import io.reactivex.rxjava3.subjects.BehaviorSubject
import org.deepsymmetry.beatlink.Beat
import org.deepsymmetry.beatlink.BeatListener
import org.deepsymmetry.beatlink.OnAirListener
import org.deepsymmetry.beatlink.data.ArtFinder
import org.deepsymmetry.beatlink.data.MetadataFinder
import java.awt.image.BufferedImage


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

    private val REMOVE_THESE: List<String> = MainConfig.get("remove-these-from-track-titles")
    private var emptyTrack: Track = MainConfig.get<Track>("empty-track").copy(isEmpty = true)
    private val hidelabel: String = MainConfig.get("hide-track-with-this-album-name")
    private val IDlabel: String = MainConfig.get("id-track-with-this-album-name")
    private var IDTrack = Track(
        id = -1,
        title = "ID",
        artist = "ID",
        art = null,
        isId = true,
        precedingTrackPlayedAtBpm = null
    )

    val nowPlayingTrack: BehaviorSubject<Track> = BehaviorSubject.createDefault(emptyTrack)

    private val currentlyAudibleChannels: MutableSet<Int> = hashSetOf()
    private var currentBpm: Double? = null

    private val HqAlbumArtFinder = HqAlbumArtFinder()

    override fun newBeat(beat: Beat?) {
        currentBpm = beat?.effectiveTempo
    }

    override fun channelsOnAir(audibleChannels: MutableSet<Int>?) {
        if (audibleChannels == null) return

        currentlyAudibleChannels.clear()
        currentlyAudibleChannels.addAll(audibleChannels)

        val onlyChannelOnAir = currentlyAudibleChannels.singleOrNull()
        onlyChannelOnAir?.let {
            val metadata = MetadataFinder.getInstance().getLatestMetadataFor(it) ?: return@let null

            var title = metadata.title

            REMOVE_THESE.forEach {
                title = title.replace(it, "")
            }

            val art: BufferedImage? = HqAlbumArtFinder.getHQAlbumArt(metadata)
                ?: ArtFinder.getInstance().getLatestArtFor(it)?.image

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
            if (currentTrack.id == nowPlayingTrack.value.id) {
                return
            }

            println(currentTrack)
            nowPlayingTrack.onNext(currentTrack)

        } ?: run {
            if (nowPlayingTrack.value != emptyTrack) {
                nowPlayingTrack.onNext(emptyTrack)
            }
        }
    }
}
