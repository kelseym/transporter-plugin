package org.nrg.xnatx.plugins.transporter.services;

import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.transporter.model.DataSnap;
import org.nrg.xnatx.plugins.transporter.model.ResolvedDataSnap;

import java.util.List;

public interface TransporterService {
    List<DataSnap> getDataSnaps(UserI user);

    DataSnap getDataSnap(UserI user, String id);

    ResolvedDataSnap getResolvedDataSnap(UserI user, String id);

    Boolean storeDataSnap(UserI user, DataSnap dataSnap);


    ResolvedDataSnap resolveDataSnap(DataSnap dataSnap);
}
