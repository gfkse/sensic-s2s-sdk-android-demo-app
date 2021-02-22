package com.gfk.s2s.collector;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.LibTestRunner;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(LibTestRunner.class)
public class ConfigTest {

    final static String JSON_STRING_WITHOUT_ENABLED= "{ \"enabled\": false," +
            "\"projectVersion\": \"0.1\"," +
            "\"configVersion\": \"1\"," +
            "\"tech\": \"s2s-android\"," +
            "\"projectName\": \"dev\"," +
            "\"trackingUrl\": \"http://127.0.0.1:8881\"," +
            "\"dnt\": false," +
            "\"tsUrl\": \"http://127.0.0.1:8881/ts.json\"," +
            "\"suiApiUrl\": \"http://127.0.0.1:8881/suiapi.html\"," +
            "\"suiGeneratorUrl\": \"http://127.0.0.1:8881/suigenerator\"," +
            "\"streamCustom\": [\"cp1\",\"cp2\",\"cp3\",\"cp4\",\"cp5\"]," +
            "\"contentCustom\": [\"cp1\",\"cp2\"]," +
            "\"segment\": {\"maxSegmentStateItems\": 86400, \"maxSegments\": 400, \"minSegmentDuration\": 2000}" +
            "}";

    final static String JSON_EMPTY = "{}";

    final static String JSON_STRING =
        "{" +
            "    configVersion: \"1\"," +
            "    tech: \"s2s-a\"," +
            "    enabled: true," +
            "    projectName: \"xx1preprod\"," +
            "    projectVersion: \"1.0\"," +
            "    trackingUrl: \"https://xx1preprod-s2s.sensic.net\"," +
            "    dnt: true," +
            "    suiApiUrl: \"https://xx-config-preproduction.sensic.net/suiapi.html\"," +
                "    suiGeneratorUrl: \"https://xx-config-preproduction.sensic.net/suigenerator\"," +
            "    tsUrl: \"https://xx-config-preproduction.sensic.net/ts.json\"," +
            "    contentCustom: [ ]," +
            "    streamCustom: [" +
            "        \"cliptype\"" +
            "    ]," +
            "    segment: {" +
            "        maxSegmentStateItems: 86400," +
            "        maxSegments: 400," +
            "        minSegmentDuration: 2000" +
            "    }" +
        "}";

    @Before
    public void setUp() {

    }

    @Test
    public void testCreateFromJsonAtPreprod() {
        Config config=Config.createFromJson(JSON_STRING);
        assertNotNull(config);
        assertThat(config.getConfigVersion()).isEqualTo("1");
        assertThat(config.getTech()).isEqualTo("s2s-a");
        assertThat(config.isEnabled()).isTrue();
        assertThat(config.getProjectName()).isEqualTo("xx1preprod");
        assertThat(config.getProjectVersion()).isEqualTo("1.0");
        assertThat(config.getTrackingUrl()).isEqualTo("https://xx1preprod-s2s.sensic.net");
        assertThat(config.isDnt()).isTrue();
        assertThat(config.getSuiGeneratorUrl()).isEqualTo("https://xx-config-preproduction.sensic.net/suigenerator");
        assertThat(config.getTsUrl()).isEqualTo("https://xx-config-preproduction.sensic.net/ts.json");
        assertThat(config.getStreamCustom().get(0)).isEqualTo("cliptype");
        assertThat(config.getStreamCustom().size()).isEqualTo(1);
        assertThat(config.getContentCustom().size()).isEqualTo(0);
        assertThat(config.getSegmentConfig().getMaxStateItemsNumber()).isEqualTo(86400);
        assertThat(config.getSegmentConfig().getMaxSegmentNumber()).isEqualTo(400);
        assertThat(config.getSegmentConfig().getMinSegmentDuration()).isEqualTo(2000);
    }

    @Test
    public void testCreateFromNull() {
        Config config=Config.createFromJson(null);
        assertNotNull(config);
    }

    @Test
    public void testCreateFromEmptyJson() {
        Config config=Config.createFromJson(JSON_EMPTY);
        assertThat(config.isEnabled()).isNull();
        assertThat(config.getProjectVersion()).isEqualTo("");
        assertThat(config.getTech()).isEqualTo("");
        assertThat(config.getProjectName()).isEqualTo("");
        assertThat(config.getTrackingUrl()).isEqualTo("");
        assertThat(config.isDnt()).isNull();
        assertThat(config.getSuiGeneratorUrl()).isEqualTo("");
        assertThat(config.getStreamCustom().size()).isEqualTo(0);
        assertThat(config.getContentCustom().size()).isEqualTo(0);
    }

    @Test
    public void testCreateFromJsonEnabled() {
        Config config=Config.createFromJson(JSON_STRING);
        assertTrue(config.isEnabled());
    }

    @Test
    public void testCreateFromJsonWithoutEnabled() {
        Config config=Config.createFromJson(JSON_STRING_WITHOUT_ENABLED);
        assertFalse(config.isEnabled());
    }
}
