package io.pleo.antaeus.core.services

import io.mockk.Runs
import io.mockk.just
import io.mockk.every
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import io.pleo.antaeus.core.external.PaymentProvider
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BillingServiceTest {

    private val paymentProvider = mockk<PaymentProvider>()
    private val billingService = BillingService(paymentProvider)

    @Test
    fun `runBilling will call payment provider charge`() {
        every { paymentProvider.charge(any()) } returns true

        billingService.runBilling()

        verify { paymentProvider.charge(any()) }
    }
}