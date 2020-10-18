package cx.aphex.now_playing.beatlinkdata

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.jakewharton.rxrelay3.PublishRelay
import org.slf4j.LoggerFactory
import java.net.InetAddress
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceListener
import javax.jmdns.ServiceTypeListener

/**
 * Discovers any beat-link data consumers on the network that want to get POSTED updates from us.
 */
object BeatLinkDataConsumerServiceDiscovery {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java.simpleName) as Logger

    val beatLinkDataConsumers = mutableSetOf<BeatLinkDataConsumer>()

    init {
        logger.level = Level.INFO
    }

    val onNewConsumerDiscovered: PublishRelay<BeatLinkDataConsumer> = PublishRelay.create()

    //    val nif: NetworkInterface = NetworkInterface.getByName("en10")
//    val nifAddresses: Enumeration<InetAddress> = nif.inetAddresses
//
//    val jmdns: JmDNS = JmDNS.create(nifAddresses.nextElement())
    val jmdns: JmDNS = JmDNS.create(InetAddress.getLocalHost())

    fun start() {

        // Add a service listener
        jmdns.addServiceListener("_beatlinkdata._tcp.local.", SampleListener())
        jmdns.addServiceTypeListener(object : ServiceTypeListener {
            override fun subTypeForServiceTypeAdded(event: ServiceEvent?) {
                logger.debug("Subtype for Service type added: $event")
            }

            override fun serviceTypeAdded(event: ServiceEvent?) {
                logger.debug("Service type added: $event")
            }

        })
    }

    private class SampleListener : ServiceListener {
        override fun serviceAdded(event: ServiceEvent) {
            logger.info("Service added: " + event.info)
        }

        override fun serviceRemoved(event: ServiceEvent) {
            logger.info("Service removed: " + event.info)
            beatLinkDataConsumers.removeIf { it.address == event.info.inet4Addresses.first() }
        }

        override fun serviceResolved(event: ServiceEvent) {
            logger.info("Service resolved: " + event.info)
            val info = event.info
            val newConsumer = BeatLinkDataConsumer(
                info.inet4Addresses.first(),
                info.port
            )
            if (beatLinkDataConsumers.add(newConsumer)) {
                onNewConsumerDiscovered.accept(newConsumer)
            }
        }
    }
}
