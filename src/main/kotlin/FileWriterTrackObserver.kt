package cx.aphex.now_playing

import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import java.io.File
import java.nio.file.Paths
import javax.imageio.ImageIO

class FileWriterTrackObserver : Observer<Track> {
    private val outputFolder: String = MainConfig.get("output-folder")

    private val emptyAlbumArt: ByteArray = File(MainConfig.get<String>("empty-track-album-art-path")).readBytes()

    override fun onNext(track: Track) {
        writeNowPlayingToFiles(track)
    }

    override fun onComplete() {}

    override fun onSubscribe(d: Disposable?) {}

    override fun onError(e: Throwable?) {}


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
                ImageIO.write(track.art, "png", outputStream())
            }
        }
    }
}
