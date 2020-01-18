package io.pleo.antaeus.core.services

import mu.KotlinLogging
import org.joda.time.DateTime
import org.joda.time.Duration

private val logger = KotlinLogging.logger {}

class Scheduler(
    private val billingService: BillingService
) {
    fun run() {
        while (true) {
            billingService.runBilling()
            val now = DateTime.now()

            val millisTillTomorrow = Duration(
                now,
                now.plusDays(1).withTimeAtStartOfDay()
            ).millis

            logger.info { "Waiting $millisTillTomorrow milliseconds until starting again" }
            Thread.sleep(millisTillTomorrow)
        }
    }
}