import ch.qos.logback.classic.Logger
import cx.aphex.now_playing.BeatLinkDataConsumerServiceDiscovery
import cx.aphex.now_playing.beatlinknotifier.BeatLinkNotifierClient
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import org.slf4j.LoggerFactory


class BeatLinkTrackNotifier : Observer<Track> {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java.simpleName) as Logger

    override fun onNext(track: Track) {
        notifyAll(track)
    }

    fun notifyAll(track: Track) {
        BeatLinkDataConsumerServiceDiscovery.beatLinkDataConsumers.forEach {
            // POST track to /currentTrack
            // When do I connect?
            logger.info("Notifying for track: $track")
            BeatLinkNotifierClient(it).notify(track).subscribe(
                { result ->
                    logger.info("Notified. Data consumer server reported success = $result")
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
