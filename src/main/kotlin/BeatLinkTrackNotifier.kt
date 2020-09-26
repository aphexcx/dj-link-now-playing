import cx.aphex.now_playing.BeatLinkDataConsumerServiceDiscovery
import cx.aphex.now_playing.beatlinknotifier.BeatLinkNotifierClient
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable


class BeatLinkTrackNotifier : Observer<Track> {

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
