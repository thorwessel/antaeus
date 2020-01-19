/*
    Implements endpoints related to invoices.
 */

package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import java.time.LocalDateTime

class InvoiceService(private val dal: AntaeusDal) {
    fun fetchAll(): List<Invoice> {
       return dal.fetchInvoices()
    }

    fun fetch(id: Int): Invoice {
        return dal.fetchInvoice(id) ?: throw InvoiceNotFoundException(id)
    }

    fun fetchAllWithStatus(status: InvoiceStatus): List<Invoice>? {
        return dal.fetchInvoices(statusFilter = status)
    }

    fun markInvoiceProcessing(id: Int): Invoice {
        return dal.markInvoiceProcessing(id) ?: throw InvoiceNotFoundException(id)
    }

    fun markInvoicePaid(id: Int): Invoice {
        return dal.markInvoicePaid(id) ?: throw InvoiceNotFoundException(id)
    }

    fun markInvoiceFailed(id: Int): Invoice {
        return dal.markInvoiceFailed(id) ?: throw InvoiceNotFoundException(id)
    }

    fun rescheduleAndMarkPending(id: Int, scheduleDate: LocalDateTime = LocalDateTime.now().plusDays(1)): Invoice {
        return dal.rescheduleAndMarkPending(id, scheduleDate) ?: throw InvoiceNotFoundException(id)
    }

    fun updateInvoice(id: Int, status: InvoiceStatus): Invoice {
        return dal.updateInvoiceStatus(id, status) ?: throw InvoiceNotFoundException(id)
    }
}
