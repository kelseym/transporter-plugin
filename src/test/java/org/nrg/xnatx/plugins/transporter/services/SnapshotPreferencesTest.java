package org.nrg.xnatx.plugins.transporter.services;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.nrg.framework.configuration.ConfigPaths;
import org.nrg.framework.utilities.OrderedProperties;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.xnatx.plugins.transporter.config.MockConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MockConfig.class)
public class SnapshotPreferencesTest {

    @Autowired private NrgPreferenceService mockNrgPreferenceService;
    @Autowired private ConfigPaths mockConfigPaths;
    @Autowired private OrderedProperties mockOrderedProperties;

    private SnapshotPreferences snapshotPreferences;

    @Before
    public void setUp() throws Exception {
        assertNotNull(mockNrgPreferenceService);
        assertNotNull(mockConfigPaths);
        assertNotNull(mockOrderedProperties);
        snapshotPreferences = new SnapshotPreferences(mockNrgPreferenceService, mockConfigPaths, mockOrderedProperties);
    }

    @After
    public void tearDown() {
        Mockito.reset(
                mockNrgPreferenceService,
                mockConfigPaths,
                mockOrderedProperties
        );
    }

    @Test
    public void getSnapshotPath() {
        snapshotPreferences.getSnapshotPath();
    }

    @Test
    public void setSnapshotPath() {
    }
}