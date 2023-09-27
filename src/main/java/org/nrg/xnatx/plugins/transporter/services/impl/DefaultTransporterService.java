package org.nrg.xnatx.plugins.transporter.services.impl;

import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.transporter.model.DataSnap;
import org.nrg.xnatx.plugins.transporter.services.TransporterService;

import java.util.List;

public class DefaultTransporterService implements TransporterService {

    List<DataSnap> getDataSnaps(UserI user) {
        return null;
    }

    DataSnap getDataSnap(UserI user, String id) {
        return null;
    }
}
