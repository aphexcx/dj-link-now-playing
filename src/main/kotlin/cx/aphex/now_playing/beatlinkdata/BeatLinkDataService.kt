package cx.aphex.now_playing.beatlinkdata

import cx.aphex.now_playing.Track
import io.reactivex.rxjava3.core.Single
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface BeatLinkDataService {

    @POST("currentTrack")
    fun currentTrack(@Body track: Track): Single<BeatLinkPostResponse>

    @Multipart
    @POST("currentAlbumArt")
    fun currentAlbumArt(@Part image: MultipartBody.Part): Single<BeatLinkPostResponse>

}
