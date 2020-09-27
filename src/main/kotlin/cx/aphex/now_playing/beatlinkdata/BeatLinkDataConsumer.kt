package cx.aphex.now_playing.beatlinkdata

import cx.aphex.now_playing.Track
import cx.aphex.now_playing.logger
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.net.Inet4Address
import javax.imageio.ImageIO

data class BeatLinkDataConsumer(
    val address: Inet4Address,
    val port: Int,
) {
    private val client: BeatLinkNotifierClient = BeatLinkNotifierClient(this)

    fun notify(track: Track) {
        client.notify(track)
    }

    inner class BeatLinkNotifierClient(consumer: BeatLinkDataConsumer) {
        private val loggingInterceptor = HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BASIC)
        private val client: OkHttpClient
        private val retrofit: Retrofit
        private val apiService: BeatLinkDataService

        init {
            client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl("http://${consumer.address.hostAddress}:8080/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(
                    RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io())
                )
                .build()

            apiService = retrofit.create(BeatLinkDataService::class.java)
        }

        fun notify(track: Track) {
            logger.info("Notifying for track: $track")
            apiService.currentTrack(track)
                .flatMap { trackResponse ->
                    notifyAlbumArt(track.art)
                        .map { trackResponse.success }
                }.subscribe(
                    { result ->
                        logger.info("Notified. Data consumer server reported success = $result")
                    },
                    { error ->
                        logger.error(error.message)
                    }
                )
        }

        private fun notifyAlbumArt(art: BufferedImage): Single<BeatLinkPostResponse> {
            val baos = ByteArrayOutputStream()
            ImageIO.write(art, "png", baos)
            val imageBytes = baos.toByteArray()

            val requestBytes: RequestBody =
                imageBytes.toRequestBody("multipart/form-data".toMediaType(), 0, imageBytes.size)

            // MultipartBody.Part is used to send also the actual file name
            val imageBody: MultipartBody.Part =
                MultipartBody.Part.createFormData("image", "currentAlbumArt.png", requestBytes)

            return apiService.currentAlbumArt(imageBody).doOnError {
                logger.error(it.message)
            }
        }
    }
}