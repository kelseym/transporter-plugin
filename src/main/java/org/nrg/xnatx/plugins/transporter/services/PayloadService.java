package org.nrg.xnatx.plugins.transporter.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xnatx.plugins.transporter.model.DataSnap;
import org.nrg.xnatx.plugins.transporter.model.Payload;

import java.util.List;

public interface PayloadService {
    Payload createPayload(String username, String snapId) throws Exception;

    Payload createPayload(DataSnap dataSnap, Payload.Type payloadType) throws Exception;

    List<Payload> createPayloads(List<DataSnap> dataSnaps);
}
