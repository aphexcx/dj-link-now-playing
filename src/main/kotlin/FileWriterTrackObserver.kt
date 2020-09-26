package cx.aphex.now_playing

import Track
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import java.nio.file.Paths
import javax.imageio.ImageIO

class FileWriterTrackObserver : Observer<Track> {
    private val outputFolder: String = MainConfig.get("output-folder")



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
//                         writer.println("QUARANTRANCE • Episode #10 • WE FOUND OUR BEACH")
            writer.println(track.artist)
            writer.close()
        }

        with(Paths.get(outputFolder, "nowplaying-track.txt").toFile()) {
            if (!exists()) createNewFile()
            val writer = printWriter()
//            writer.println("twitch.tv/aphexcx")
            writer.println(track.title)
            writer.close()
        }

        with(Paths.get(outputFolder, "nowplaying-both.txt").toFile()) {
            if (!exists()) createNewFile()
            val writer = printWriter()
            writer.println(track.artist + " • " + track.title)
            writer.close()
        }

        with(Paths.get(outputFolder, "art/nowplaying-art.png").toFile()) {

            val tempFile = createTempFile(outputFolder)

//            if (track.art == null) {
//                tempFile.writeBytes(emptyAlbumArt)
//            } else {
            ImageIO.write(track.art, "png", tempFile.outputStream())
//            }

            if (exists()) {
                delete()
            }
            tempFile.copyTo(this)
            tempFile.delete()
        }

    }

}
