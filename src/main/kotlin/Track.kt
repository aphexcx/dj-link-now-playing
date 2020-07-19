package cx.aphex.now_playing

import java.awt.image.BufferedImage

data class Track(
    val id: Int,
    val title: String,
    val artist: String,
    @Transient val art: BufferedImage?,
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
