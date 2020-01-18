/*
    Implements the data access layer (DAL).
    This file implements the database queries used to fetch and insert rows in our database tables.

    See the `mappings` module for the conversions between database rows and Kotlin objects.
 */

package io.pleo.antaeus.data

import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Customer
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.joda.time.DateTime

class AntaeusDal(private val db: Database) {
    fun fetchInvoice(id: Int): Invoice? {
        // transaction(db) runs the internal query as a new database transaction.
        return transaction(db) {
            // Returns the first invoice with matching id.
            InvoiceTable
                .select { InvoiceTable.id.eq(id) }
                .firstOrNull()
                ?.toInvoice()
        }
    }

    fun fetchInvoices(): List<Invoice> {
        return transaction(db) {
            InvoiceTable
                .selectAll()
                .map { it.toInvoice() }
        }
    }

    fun fetchInvoices(statusFilter: InvoiceStatus): List<Invoice>? {
        return transaction(db) {
            InvoiceTable
                .selectAll()
                .map { it.toInvoice() }
                .filter { it.status == statusFilter }
        }
    }

    fun createInvoice(amount: Money, customer: Customer, status: InvoiceStatus = InvoiceStatus.PENDING, dueDate: DateTime): Invoice? {
        val id = transaction(db) {
            // Insert the invoice and returns its new id.
            InvoiceTable
                .insert {
                    it[this.value] = amount.value
                    it[this.currency] = amount.currency.toString()
                    it[this.status] = status.toString()
                    it[this.customerId] = customer.id
                    it[this.dueDate] = dueDate
                    it[this.scheduleDate] = dueDate
                } get InvoiceTable.id
        }

        return fetchInvoice(id!!)
    }

    fun markInvoiceProcessing(invoiceId: Int): Invoice? {
        transaction(db) {
            InvoiceTable
                .update({ InvoiceTable.id.eq(invoiceId) }) {
                    it[this.status] = InvoiceStatus.PROCESSING.toString()
                }
        }
        return fetchInvoice(invoiceId)
    }

    fun markInvoicePaid(invoiceId: Int): Invoice? {
        transaction(db) {
            InvoiceTable
                .update({ InvoiceTable.id.eq(invoiceId) }) {
                    it[this.status] = InvoiceStatus.PAID.toString()
                }
        }
        return fetchInvoice(invoiceId)
    }

    fun rescheduleAndMarkPending(invoiceId: Int): Invoice? {
        transaction(db) {
            InvoiceTable
                .update({ InvoiceTable.id.eq(invoiceId) }) {
                    it[this.status] = InvoiceStatus.PENDING.toString()
                    it[this.scheduleDate] = DateTime.now().plusDays(1)
                }
        }
        return fetchInvoice(invoiceId)
    }

    fun fetchCustomer(id: Int): Customer? {
        return transaction(db) {
            CustomerTable
                .select { CustomerTable.id.eq(id) }
                .firstOrNull()
                ?.toCustomer()
        }
    }

    fun fetchCustomers(): List<Customer> {
        return transaction(db) {
            CustomerTable
                .selectAll()
                .map { it.toCustomer() }
        }
    }

    fun createCustomer(currency: Currency): Customer? {
        val id = transaction(db) {
            // Insert the customer and return its new id.
            CustomerTable.insert {
                it[this.currency] = currency.toString()
            } get CustomerTable.id
        }

        return fetchCustomer(id!!)
    }
}
