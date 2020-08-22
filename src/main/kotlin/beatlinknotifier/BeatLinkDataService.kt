package cx.aphex.now_playing.beatlinknotifier

import Track
import io.reactivex.rxjava3.core.Single
import retrofit2.http.Body
import retrofit2.http.POST

interface BeatLinkDataService {

    @POST("currentTrack")
    fun currentTrack(@Body track: Track): Single<String>

}
