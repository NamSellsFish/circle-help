package server.circlehelp.services

import org.springframework.stereotype.Service
import server.circlehelp.delegated_classes.EventStatusArbiter
import server.circlehelp.delegated_classes.ExpirationStatusArbiter
import server.circlehelp.delegated_classes.FrontStatusArbiter
import server.circlehelp.delegated_classes.LoggingStatusArbiter
import server.circlehelp.delegated_classes.NoopStatusArbiter
import server.circlehelp.delegated_classes.StatusArbiter
import server.circlehelp.repositories.caches.EventCompartmentCache
import server.circlehelp.repositories.caches.FrontCompartmentCache

@Service
class StatusArbiterManager(
    logic: Logic,
    frontCompartmentCache: FrontCompartmentCache,
    eventCompartmentCache: EventCompartmentCache,
    activeEventsService: ActiveEventsService,
) {

    val frontStatusDecider = FrontStatusArbiter(
        frontCompartmentCache,
        NoopStatusArbiter
    )

    val eventStatusArbiter = EventStatusArbiter(
        eventCompartmentCache,
        activeEventsService,
        NoopStatusArbiter
    )

    val expirationStatusDecider = ExpirationStatusArbiter(
        logic,
        eventStatusArbiter
    )

    val loggingStatusDecider = LoggingStatusArbiter(eventStatusArbiter)

    val topStatusArbiter : StatusArbiter = loggingStatusDecider
}