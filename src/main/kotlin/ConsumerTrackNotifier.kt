package cx.aphex.now_playing

import ch.qos.logback.classic.Logger
import cx.aphex.now_playing.beatlinknotifier.BeatLinkNotifierClient
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import org.slf4j.LoggerFactory


class ConsumerTrackNotifier : Observer<Track> {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java.name) as Logger

    override fun onNext(track: Track) {
        notifyAll(track)
    }

    fun notifyAll(track: Track) {
        BeatLinkDataConsumerServiceDiscovery.beatLinkDataConsumers.forEach {
            // POST track to /currentTrack
            // When do I connect?
            BeatLinkNotifierClient(it).notify(track).subscribe(
                { result ->
                    logger.debug(result)
                },
                { error ->
                    logger.error(error.message)
                }
            )
        }
    }

    override fun onComplete() {}

    override fun onSubscribe(d: Disposable?) {}

    override fun onError(e: Throwable?) {}
}
