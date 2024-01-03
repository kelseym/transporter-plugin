package org.nrg.xnatx.plugins.transporter.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.nrg.xdat.security.services.RoleServiceI;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.transporter.config.TransporterRestTestConfig;
import org.nrg.xnatx.plugins.transporter.model.DataSnap;
import org.nrg.xnatx.plugins.transporter.model.RemoteAppHeartbeat;
import org.nrg.xnatx.plugins.transporter.services.DataSnapEntityService;
import org.nrg.xnatx.plugins.transporter.services.DataSnapResolutionService;
import org.nrg.xnatx.plugins.transporter.services.SnapshotPreferences;
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
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.testSecurityContext;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
//@Transactional
@ContextConfiguration(classes = TransporterRestTestConfig.class)
public class TransporterRestTest {
    private final static String ADMIN_USERNAME = "admin";
    private final static String NON_ADMIN_USERNAME = "non-admin";
    private Authentication ADMIN_AUTH;
    private Authentication NONADMIN_AUTH;

    private MockMvc mockMvc;

    private final String FAKE_URL = "mock://url";
    private final MediaType JSON = MediaType.APPLICATION_JSON_UTF8;
    //TODO:
    private final String snapJson = "";

    @Autowired private WebApplicationContext wac;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private SnapshotPreferences mockSnapshotPreferences;
    @Autowired private RoleServiceI mockRoleService;
    @Autowired private UserManagementServiceI mockUserManagementServiceI;
    @Autowired private DataSnapResolutionService mockDataSnapResolutionService;
    @Autowired private DataSnapEntityService mockDataSnapEntityService;

    @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder(new File("/tmp"));

    @Before
    public void setup() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();


        // Mock the userI
        final String adminPassword = "admin";
        final UserI admin = mock(UserI.class);
        when(admin.getLogin()).thenReturn(ADMIN_USERNAME);
        when(admin.getPassword()).thenReturn(adminPassword);
        when(mockRoleService.isSiteAdmin(admin)).thenReturn(true);
        when(mockUserManagementServiceI.getUser(ADMIN_USERNAME)).thenReturn(admin);
        ADMIN_AUTH = new TestingAuthenticationToken(admin, adminPassword);

        final String nonAdminPassword = "non-admin-pass";
        final UserI nonAdmin = mock(UserI.class);
        when(nonAdmin.getLogin()).thenReturn(NON_ADMIN_USERNAME);
        when(nonAdmin.getPassword()).thenReturn(nonAdminPassword);
        when(mockRoleService.isSiteAdmin(nonAdmin)).thenReturn(false);
        when(mockUserManagementServiceI.getUser(NON_ADMIN_USERNAME)).thenReturn(nonAdmin);
        NONADMIN_AUTH = new TestingAuthenticationToken(nonAdmin, nonAdminPassword);

        when(mockSnapshotPreferences.getSnapshotPath()).thenReturn(temporaryFolder.getRoot().getAbsolutePath());

        // Mock the dataSnapResolutionService
        when(mockDataSnapResolutionService.resolveDataSnap(Mockito.any(DataSnap.class)))
                .then(AdditionalAnswers.returnsFirstArg());

        // Mock the dataSnapEntityService
        when(mockDataSnapEntityService.createDataSnap(Mockito.anyString(), Mockito.any(DataSnap.class)))
                .thenReturn(new DataSnap());

    }

    private String loadSnapJson001() throws Exception {
        String filePath = Paths.get(ClassLoader.getSystemResource("dataSnap001.json").toURI()).toString();
        DataSnap dataSnap = objectMapper.readValue(new File(filePath), DataSnap.class);
        return objectMapper.writeValueAsString(dataSnap);
    }

    private String loadHeartbeat() throws Exception{
        String filePath = Paths.get(ClassLoader.getSystemResource("heartbeat.json").toURI()).toString();
        RemoteAppHeartbeat heartbeat = objectMapper.readValue(new File(filePath), RemoteAppHeartbeat.class);
        return objectMapper.writeValueAsString(heartbeat);
    }

    @Test
    public void testCreateSnap() throws Exception {
        String dataSnapJson = loadSnapJson001();
        String path = "/transporter/datasnap";

        final MockHttpServletRequestBuilder request = post(path)
                .contentType(JSON)
                .content(dataSnapJson)
                .with(authentication(ADMIN_AUTH))
                .with(csrf())
                .with(testSecurityContext());

        final String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    @Test
    public void testAddSnapReader() {
    }

    @Test
    public void testAddSnapEditor() {
    }

    @Test
    public void testGetSnaps() {
    }

    @Test
    public void testGetSnap() {
    }

    @Test
    public void testDeleteSnap() {
    }

    @Test
    public void testGetPayloadByLabel() {
    }

    @Test
    public void testGetPayloads() throws Exception {
        String path = "/transporter/payloads";

        final MockHttpServletRequestBuilder request = get(path)
                .contentType(JSON)
                .with(authentication(ADMIN_AUTH))
                .with(csrf())
                .with(testSecurityContext());

        final String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(response, not(nullValue()));
    }

    @Test
    public void testSetMappingJson() {
    }

    @Test
    public void testGetMapping() {
    }

    @Test
    public void testMirror() {
    }

    @Test
    public void testUpdateRemoteHeartbeat() throws Exception {
        String path = "/transporter/heartbeat";
        String heartbeatJson = loadHeartbeat();

        final MockHttpServletRequestBuilder request = post(path)
                .content(heartbeatJson)
                .contentType(JSON)
                .with(authentication(ADMIN_AUTH))
                .with(csrf())
                .with(testSecurityContext());

        final String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(response, not(nullValue()));
    }

    @Test
    public void testGetRemoteHeartbeat() {
    }

    @Test
    public void testGetRemoteHeartbeatById() {
    }

    @Test
    public void testUpdateActivity() {
    }

    @Test
    public void testGetActivity() {
    }

    @Test
    public void testDeleteActivity() {
    }

    @Test
    public void testgetAllActivity() {
    }

}
