package cx.aphex.now_playing

import java.net.Inet4Address

data class BeatLinkDataConsumer(
    val address: Inet4Address,
    val port: Int
)
