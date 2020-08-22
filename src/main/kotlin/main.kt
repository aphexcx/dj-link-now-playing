package cx.aphex.now_playing

import BeatLinkTrackNotifier
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.yaml
import com.uchuhimo.konf.toValue
import org.deepsymmetry.beatlink.*
import org.deepsymmetry.beatlink.data.ArtFinder
import org.deepsymmetry.beatlink.data.MetadataFinder
import org.slf4j.LoggerFactory
import kotlin.concurrent.thread

object MainConfig {
    val config = Config()
        .from.yaml.file("config.yml")

    inline fun <reified T> get(path: String): T {
        return config.at(path).toValue()
    }
}

fun main(args: Array<String>) {
    val rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
    rootLogger.level = Level.toLevel(MainConfig.get<String>("log-level"))

    BeatLinkDataConsumerServiceDiscovery.start()

    val deviceFinder = DeviceFinder.getInstance()
    deviceFinder.start()
    deviceFinder.addDeviceAnnouncementListener(object : DeviceAnnouncementListener {
        override fun deviceLost(announcement: DeviceAnnouncement) {
            println("Lost device: ${announcement.name}")
        }

        override fun deviceFound(announcement: DeviceAnnouncement) {
            println("New device: ${announcement.name}")
        }
    })

    // To find some kinds of information, like which device is the tempo master, how many beats of a track have been
    // played, or how many beats there are until the next cue point in a track, and any detailed information about
    // the tracks themselves, you need to have beat-link create a virtual player on the network. This causes the
    // other players to send detailed status updates directly to beat-link, so it can interpret and keep track of
    // this information for you.
    // This also means you can't run this when rekordbox is running on the same computer.
    val virtualCdj = VirtualCdj.getInstance()
    virtualCdj.useStandardPlayerNumber = true
    virtualCdj.addLifecycleListener(object : LifecycleListener {
        override fun stopped(sender: LifecycleParticipant?) {
            println("VirtualCdj stopped!")
        }

        override fun started(sender: LifecycleParticipant?) {
            println("VirtualCdj started as device ${virtualCdj.deviceNumber}")
        }
    })
    virtualCdj.start()

    thread(isDaemon = true, name = "VirtualCdj watchdog") {
        while (true) {
            Thread.sleep(5000)

            if (!virtualCdj.isRunning) {
                println("Attempting to restart VirtualCdj...")
                virtualCdj.start()
            }
        }
    }

    MetadataFinder.getInstance().start()

    ArtFinder.getInstance().start()

    val trackSource = TrackSource()

    val beatListener = BeatFinder.getInstance()
    beatListener.addBeatListener(trackSource)
    beatListener.addOnAirListener(trackSource)
    beatListener.start()

    trackSource.nowPlayingTrack.subscribe(FileWriterTrackObserver())
    trackSource.nowPlayingTrack.subscribe(TracklistWriterTrackObserver())
    trackSource.nowPlayingTrack.subscribe(BeatLinkTrackNotifier())

    while (true) {
        Thread.sleep(100)
    }

}
