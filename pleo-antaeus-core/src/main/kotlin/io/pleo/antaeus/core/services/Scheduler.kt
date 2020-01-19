package io.pleo.antaeus.core.services

import mu.KotlinLogging
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

private val logger = KotlinLogging.logger {}

class Scheduler(
    private val billingService: BillingService
) {
    fun run() {
        while (true) {
            billingService.runBilling()
            val now = LocalDateTime.now()

            val millisTillTomorrow = Duration.between(
                now,
                LocalDateTime.of(now.plusDays(1).toLocalDate(), LocalTime.MIDNIGHT)
            ).toMillis()

            logger.info { "Waiting $millisTillTomorrow milliseconds until starting again" }
            Thread.sleep(millisTillTomorrow)
        }
    }
}