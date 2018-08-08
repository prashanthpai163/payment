package com.dhc.api.domain.domain;


import javax.persistence.*;

import org.springframework.data.elasticsearch.annotations.Document;
import java.io.Serializable;
import java.util.Objects;

/**
 * A Invoice.
 */
@Entity
@Table(name = "invoice")
@Document(indexName = "invoice")
public class Invoice implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_id")
    private Integer invoiceId;

    @Column(name = "invoice_date")
    private String invoiceDate;

    @Column(name = "amount")
    private Integer amount;

    @OneToOne
    @JoinColumn(unique = true)
    private Transaction transaction;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getInvoiceId() {
        return invoiceId;
    }

    public Invoice invoiceId(Integer invoiceId) {
        this.invoiceId = invoiceId;
        return this;
    }

    public void setInvoiceId(Integer invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public Invoice invoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
        return this;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public Integer getAmount() {
        return amount;
    }

    public Invoice amount(Integer amount) {
        this.amount = amount;
        return this;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public Invoice transaction(Transaction transaction) {
        this.transaction = transaction;
        return this;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Invoice invoice = (Invoice) o;
        if (invoice.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), invoice.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Invoice{" +
            "id=" + getId() +
            ", invoiceId=" + getInvoiceId() +
            ", invoiceDate='" + getInvoiceDate() + "'" +
            ", amount=" + getAmount() +
            "}";
    }
}
