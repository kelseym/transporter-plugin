package org.nrg.xnatx.plugins.transporter.services;

import org.nrg.xnat.archive.ValidationException;
import org.nrg.xnatx.plugins.transporter.model.DataSnap;
import org.nrg.xnatx.plugins.transporter.model.TransporterPathMapping;

import javax.annotation.Nonnull;
import java.io.IOException;

public interface DataSnapResolutionService {
    DataSnap mirrorDataSnap(DataSnap dataSnap) throws Exception;

    void validateDataSnap(DataSnap dataSnap, Boolean expectResolved) throws ValidationException;

    DataSnap resolveDataSnap(DataSnap dataSnap);

    DataSnap getRemappedDataSnap(DataSnap dataSnap, TransporterPathMapping transporterPathMapping);
}
