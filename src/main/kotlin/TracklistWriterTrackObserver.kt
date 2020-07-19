package cx.aphex.now_playing

import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import java.io.File
import java.nio.file.Paths
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TracklistWriterTrackObserver : Observer<Track> {
    private val outputFolder: String = MainConfig.get("output-folder")

    private val emptyAlbumArt: ByteArray = File(MainConfig.get<String>("empty-track-album-art-path")).readBytes()

    private var startTime = LocalDateTime.now()
    private val tracklist = mutableMapOf<LocalDateTime, Track>()

    val dateformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_H.mm.ss")

    override fun onNext(track: Track) {
        writeToTrackList(track)
    }

    override fun onComplete() {}

    override fun onSubscribe(d: Disposable?) {}

    override fun onError(e: Throwable?) {}


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

    private fun formatDuration(duration: Duration): String? {
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
