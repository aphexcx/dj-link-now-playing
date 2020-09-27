package cx.aphex.now_playing.beatlinkdata

import cx.aphex.now_playing.Track
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable


class BeatLinkTrackNotifier : Observer<Track> {

    override fun onNext(track: Track) {
        notifyAllKnownConsumers(track)
    }

    fun notifyConsumer(consumer: BeatLinkDataConsumer, track: Track) {
        consumer.notify(track)
    }

    fun notifyAllKnownConsumers(track: Track) {
        BeatLinkDataConsumerServiceDiscovery.beatLinkDataConsumers.forEach {
            // POST track to /currentTrack
            it.notify(track)
        }
    }

    override fun onComplete() {}

    override fun onSubscribe(d: Disposable?) {}

    override fun onError(e: Throwable?) {}
}
