package org.nrg.xnatx.plugins.transporter.services;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.transporter.config.TransporterTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TransporterTestConfig.class)
public class TransporterServiceTest {

    @Autowired private TransporterService transporterService;
    @Autowired private SnapshotPreferences mockSnapshotPreferences;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private UserI user;
    private String username;

    @Before
    public void before() {
        // Mock the user
        user = mock(UserI.class);
        username = "user";
        when(user.getUsername()).thenReturn(username);

        when(mockSnapshotPreferences.getSnapshotPath()).thenReturn(temporaryFolder.getRoot().getAbsolutePath());
    }

    @After
    public void after() {
        user = null;
        username = null;
    }

    @Test
    public void test() {
        log.info("test");
    }
}
