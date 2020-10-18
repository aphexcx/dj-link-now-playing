 package cx.aphex.now_playing

import ch.qos.logback.classic.Logger
import ealvatag.audio.AudioFileIO
import ealvatag.tag.FieldKey
import ealvatag.tag.NullTag
import org.deepsymmetry.beatlink.data.TrackMetadata
import org.slf4j.LoggerFactory
import java.awt.image.BufferedImage
import java.io.File

class HqAlbumArtFinder {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java.simpleName) as Logger

    val paths: List<String> = MainConfig.get("music-folders")
    private val validExtensions: List<String> = MainConfig.get("music-file-extensions")

    private val artMap: HashMap<ArtHash, String> = hashMapOf()

    init {
        logger.info("Reading music files from $paths...")
        paths.forEach {
            processDirectory(File(it))
        }
        logger.info("Read ${artMap.size} music files, will use them for high quality album art :)")
    }

    private fun processDirectory(dir: File) {
        dir.walkBottomUp().forEach { processFile(it) }
    }

    private fun processFile(file: File) {
        if (!file.extension.isValidExtension()) {
            return
        }

        val audioFile = AudioFileIO.read(file)

        val tag = audioFile.tag.or(NullTag.INSTANCE)
        if (tag.getValue(FieldKey.TITLE).isPresent &&
            tag.getValue(FieldKey.ARTIST).isPresent
        ) {

            val artHash = ArtHash(
                tag.getValue(FieldKey.TITLE).get(),
                tag.getValue(FieldKey.ARTIST).get(),
//                tag.getValue(FieldKey.ALBUM).or(""),
                tag.getValue(FieldKey.COMMENT).or("")
            )
            artMap[artHash] = file.path
        }
    }

    fun getHQAlbumArt(metadata: TrackMetadata): BufferedImage? {
        return artMap[ArtHash(metadata)]?.let {
            with(File(it)) {
                val audioFile = AudioFileIO.read(this)
                audioFile.tag.orNull()?.let { tag ->
                    tag.firstArtwork.orNull()?.image as? BufferedImage?
                }
            }
        }
    }

    private fun String.isValidExtension(): Boolean = this in validExtensions
}

data class ArtHash(
    val title: String,
    val artist: String,
//    val album: String = "",
    val comment: String = ""
) {
    constructor(metadata: TrackMetadata) : this(
        metadata.title,
        metadata.artist.label,
//        metadata.album.label,
        metadata.comment
    )
}
