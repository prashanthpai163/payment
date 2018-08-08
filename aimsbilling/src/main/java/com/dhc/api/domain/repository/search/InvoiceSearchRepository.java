package com.dhc.api.domain.repository.search;

import com.dhc.api.domain.domain.Invoice;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the Invoice entity.
 */
public interface InvoiceSearchRepository extends ElasticsearchRepository<Invoice, Long> {
}
