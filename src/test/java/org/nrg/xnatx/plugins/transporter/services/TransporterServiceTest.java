package org.nrg.xnatx.plugins.transporter.services;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.nrg.xnatx.plugins.transporter.config.TransporterTestConfig;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TransporterTestConfig.class)
public class TransporterServiceTest {
    @Test
    public void test() {
        log.info("test");
    }
}
