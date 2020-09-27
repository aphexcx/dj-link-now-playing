package cx.aphex.now_playing

import ch.qos.logback.classic.Logger
import org.slf4j.LoggerFactory

val Any.logger: Logger
    get() = LoggerFactory.getLogger(this::class.java.simpleName) as Logger
