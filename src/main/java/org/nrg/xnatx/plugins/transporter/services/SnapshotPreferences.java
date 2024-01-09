package org.nrg.xnatx.plugins.transporter.services;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.configuration.ConfigPaths;
import org.nrg.framework.utilities.OrderedProperties;
import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.beans.AbstractPreferenceBean;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.services.NrgPreferenceService;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
@NrgPreferenceBean(toolId = "snapshot",
        toolName = "Data Snapshot Prefs",
        description = "Data Snapshot preferences.")
public class SnapshotPreferences extends AbstractPreferenceBean {
    public static final String SNAPSHOT_PATH = "snapshotPath";

    protected SnapshotPreferences(NrgPreferenceService preferenceService) {
        super(preferenceService);
    }

    @Autowired
    public SnapshotPreferences(NrgPreferenceService preferenceService,
                               final ConfigPaths configFolderPaths,
                               final OrderedProperties initPref) {
        super(preferenceService, configFolderPaths, initPref);
    }

    @NrgPreference(defaultValue = "/data/xnat/snapshots")
    public String getSnapshotPath() {
        checkSnapshotPath(getValue(SNAPSHOT_PATH));
        return getValue(SNAPSHOT_PATH);
    }

    public void setSnapshotPath(final String snapshotPath) {
        try {
            checkSnapshotPath(snapshotPath);
            set(snapshotPath, SNAPSHOT_PATH);
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name {}: something is wrong here.", SNAPSHOT_PATH, e);
        }
    }

    private void checkSnapshotPath(String path) {
        if (!Files.isDirectory(Paths.get(path))){
            log.error("Snapshot path {} is not a directory.", path);
        }
    }
}
