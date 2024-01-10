package org.nrg.xnatx.plugins.transporter.model;

import org.nrg.framework.ajax.hibernate.HibernatePaginatedRequest;

public class TransporterActivityPaginatedRequest extends HibernatePaginatedRequest {
    @Override
    public String getDefaultSortColumn() {
        return "timestamp";
    }
}
