package org.nrg.xnatx.plugins.transporter.services;

import org.nrg.xnat.archive.ValidationException;
import org.nrg.xnatx.plugins.transporter.model.DataSnap;

import javax.annotation.Nonnull;
import java.io.IOException;

public interface DataSnapResolutionService {
    DataSnap mirrorDataSnap(DataSnap dataSnap) throws RuntimeException, IOException;

    void validateDataSnap(DataSnap dataSnap, Boolean expectResolved) throws ValidationException;

    DataSnap resolveDataSnap(DataSnap dataSnap);
}
