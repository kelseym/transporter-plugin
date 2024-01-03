package org.nrg.xnatx.plugins.transporter.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.transporter.config.TransporterRestTestConfig;
import org.nrg.xnatx.plugins.transporter.config.TransporterTestConfig;
import org.nrg.xnatx.plugins.transporter.services.TransporterService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.testSecurityContext;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@Transactional
@ContextConfiguration(classes = TransporterRestTestConfig.class)
public class TransporterRestTest {
    private Authentication ADMIN_AUTH;
    private MockMvc mockMvc;

    private final String FAKE_URL = "mock://url";
    private final MediaType JSON = MediaType.APPLICATION_JSON_UTF8;
    //TODO:
    private final String snapJson = "";

    @Autowired private TransporterService transporterService;
    @Autowired private WebApplicationContext wac;
    @Autowired private ObjectMapper mapper;
    @Autowired private SiteConfigPreferences mockSiteConfigPreferences;


    @Rule public TemporaryFolder folder = new TemporaryFolder(new File("/tmp"));

    @Before
    public void setup() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();

        // Mock the userI
        final UserI admin = mock(UserI.class);
        final String adminUsername = "admin";
        final String adminPassword = "admin-pass";
        ADMIN_AUTH = new TestingAuthenticationToken(admin, adminPassword);

        when(admin.getLogin()).thenReturn(adminUsername);
        when(admin.getPassword()).thenReturn(adminPassword);

        // Mock the site config preferences
        when(mockSiteConfigPreferences.getSiteUrl()).thenReturn(FAKE_URL);
        when(mockSiteConfigPreferences.getProperty("processingUrl", FAKE_URL)).thenReturn(FAKE_URL);
        when(mockSiteConfigPreferences.getBuildPath()).thenReturn(folder.newFolder().getAbsolutePath()); // transporter makes a directory under build
        when(mockSiteConfigPreferences.getArchivePath()).thenReturn(folder.newFolder().getAbsolutePath()); // container logs get stored under archive



    }

    @Test
    public void testGetTransporter() throws Exception {
        mockMvc.perform(get("/xapi/transporter"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(content().string("[]"));
    }

    @Test
    public void storeDataSnap() throws Exception {
        final String path = "/xapi/transporter/datasnap";

        final MockHttpServletRequestBuilder request = post(path)
                .content(snapJson).contentType(JSON)
                .with(authentication(ADMIN_AUTH))
                .with(csrf())
                .with(testSecurityContext());

        ResultActions response = mockMvc.perform(request)
                .andExpect(status().isOk());

    }

    @Test
    public void mirrorSnapData() throws Exception {
        final String path = "/xapi/transporter/datasnap/mirror/";

        final MockHttpServletRequestBuilder request = post(path)
                .with(authentication(ADMIN_AUTH))
                .with(csrf())
                .with(testSecurityContext());

        ResultActions response = mockMvc.perform(request)
                .andExpect(status().isOk());

    }

}
