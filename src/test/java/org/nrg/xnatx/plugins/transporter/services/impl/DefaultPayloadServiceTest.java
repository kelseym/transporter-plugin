package org.nrg.xnatx.plugins.transporter.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.xnatx.plugins.transporter.config.TransporterTestConfig;
import org.nrg.xnatx.plugins.transporter.model.DataSnap;
import org.nrg.xnatx.plugins.transporter.model.Payload;
import org.nrg.xnatx.plugins.transporter.services.DataSnapEntityService;
import org.nrg.xnatx.plugins.transporter.services.DataSnapResolutionService;
import org.nrg.xnatx.plugins.transporter.services.PayloadService;
import org.nrg.xnatx.plugins.transporter.services.TransporterConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.empty;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TransporterTestConfig.class)
public class DefaultPayloadServiceTest {

    @Autowired private ObjectMapper objectMapper;
    @Autowired private DataSnapEntityService mockDataSnapEntityService;
    @Autowired private DataSnapResolutionService mockDataSnapResolutionService;
    @Autowired private TransporterConfigService mockTransporterConfigService;
    private PayloadService payloadService;


    @Before
    public void setup() throws Exception {
        payloadService = new DefaultPayloadService(mockDataSnapEntityService,
                mockDataSnapResolutionService, mockTransporterConfigService);
    }


    private DataSnap loadSnap001() throws Exception {
        String filePath = Paths.get(ClassLoader.getSystemResource("dataSnap001.json").toURI()).toString();
        return objectMapper.readValue(new File(filePath), DataSnap.class);
    }

    private DataSnap loadResolvedSnap001() throws Exception {
        String filePath = Paths.get(ClassLoader.getSystemResource("resolved_dataSnap001.json").toURI()).toString();
        return objectMapper.readValue(new File(filePath), DataSnap.class);
    }

    private DataSnap loadMirroredSnap001() throws Exception {
        String filePath = Paths.get(ClassLoader.getSystemResource("mirrored_dataSnap001.json").toURI()).toString();
        return objectMapper.readValue(new File(filePath), DataSnap.class);
    }
    @Ignore
    @Test
    public void testCreateFilePayload() {
        //TODO: Feature not yet implemented
    }

    @Test
    public void testCreateDirectoryPayload() throws Exception {
        DataSnap dataSnap = loadMirroredSnap001();
        Payload payload = payloadService.createPayload(dataSnap, Payload.Type.DIRECTORY);
        assertThat(payload.getType(), is(Payload.Type.DIRECTORY));
        assertThat(payload.getFileManifests(), is(not(empty())));
        assertThat(payload.getFileManifests().size(), is(1));
        assertThat(payload.getLabel(), is(dataSnap.getLabel()));
        assertThat(payload.getDescription(), is(dataSnap.getDescription()));
        assertThat(payload.getSnapshotId(), is(dataSnap.getId()));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testFailCreateUnresolvedPayload() throws Exception {
        DataSnap dataSnap = loadSnap001();
        payloadService.createPayload(dataSnap, Payload.Type.DIRECTORY);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testFailCreateNonMirroredPayload() throws Exception {
        DataSnap dataSnap = loadResolvedSnap001();
        payloadService.createPayload(dataSnap, Payload.Type.DIRECTORY);
    }


    @Test
    public void testCreatePayloads() {
    }


}