package org.nrg.xnatx.plugins.transporter.services;

import org.nrg.xnatx.plugins.transporter.model.DataSnap;

public interface DataSnapResolutionService {
    DataSnap resolveDataSnap(DataSnap dataSnap);
}
