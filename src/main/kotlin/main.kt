package cx.aphex.now_playing

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.yaml
import org.deepsymmetry.beatlink.*
import org.deepsymmetry.beatlink.data.ArtFinder
import org.deepsymmetry.beatlink.data.MetadataFinder
import kotlin.concurrent.thread

fun main(args: Array<String>) {

    val config = Config()
        .from.yaml.file("config.yml")

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

    MetadataFinder.getInstance().getLatestMetadataFor(2)

    ArtFinder.getInstance().start()

    val trackSource = TrackSource(config)

    val beatListener = BeatFinder.getInstance()
    beatListener.addBeatListener(trackSource)
    beatListener.addOnAirListener(trackSource)
    beatListener.start()

    while (true) {
        Thread.sleep(100)
    }

}
