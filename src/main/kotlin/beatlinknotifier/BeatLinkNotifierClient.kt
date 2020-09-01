package cx.aphex.now_playing.beatlinknotifier

import Track
import cx.aphex.now_playing.BeatLinkDataConsumer
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BeatLinkNotifierClient(val consumer: BeatLinkDataConsumer) {
    private val loggingInterceptor = HttpLoggingInterceptor()
        .setLevel(HttpLoggingInterceptor.Level.BASIC)

//    private val clientInterceptor: Interceptor = Interceptor { chain ->
//        var request: Request = chain.request()
//        val url: HttpUrl = request.url.newBuilder()
//            .addQueryParameter("key", "8630898-e092bf16cb1dd9ff6a483dabf")
//            .addQueryParameter("safesearch", "true")
//            .build()
//        request = request.newBuilder().url(url).build()
//        chain.proceed(request)
//    }

    private val client: OkHttpClient
    private val retrofit: Retrofit
    private val apiService: BeatLinkDataService

    init {
        client = OkHttpClient.Builder()
//            .addNetworkInterceptor(clientInterceptor)
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

    fun notify(track: Track): Single<Boolean> {
        return apiService.currentTrack(track).map { it.success }
    }

}
