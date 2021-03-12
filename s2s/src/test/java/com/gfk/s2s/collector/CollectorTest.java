package com.gfk.s2s.collector;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.AdvertisingIdClientShadow;
import org.robolectric.LibTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.gfk.s2s.collector.Collector.SHARED_PREFS_FILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

@RunWith(LibTestRunner.class)
public class CollectorTest {

    private Collector collector;

    @Before
    public void setup() {
        RuntimeEnvironment.application.getSharedPreferences(SHARED_PREFS_FILE, 0).edit().clear().commit();
        Context context = RuntimeEnvironment.application;

        Config config = new Config();
        config.setConfigVersion("1.0");
        config.setStreamCustom((new ArrayList<>(Arrays.asList("cp1", "cp2"))));
        config.setContentCustom(new ArrayList<>(Collections.singletonList("cp3")));
        config.setDnt(true);
        config.setEnabled(true);
        config.setProjectName("Test App");
        config.setProjectVersion("0.1");
        config.setSuiGeneratorUrl("https://demo-config-preproduction.sensic.net/suigenerator");
        config.setTech("123");
        config.setTrackingUrl("http://127.0.0.1:8881");

        collector = new Collector(context);
        collector.setConfig(config);
    }

    @Test
    @org.robolectric.annotation.Config(shadows = {AdvertisingIdClientShadow.class})
    public void testCommon() {
        assertThat(collector.isProjectEnabled()).isTrue();
        assertThat(collector.getTrackingUrl()).isNotEmpty();
        assertThat(collector.getSuiUrl()).isNotEmpty();
        assertThat(collector.getUserAgent().length()).isGreaterThan(4);
        assertThat(collector.getTechnology()).isNotEmpty();
        assertThat(collector.getOrigin()).isEmpty();
        assertThat(collector.getAdvertisingId()).isNotEmpty();
        assertThat(collector.getStreamCustomParameter().size()).isEqualTo(2);
        assertThat(collector.getContentCustomParameter().size()).isEqualTo(1);
        assertThat(collector.getProjectName()).isEqualTo("Test App");
        assertThat(collector.getDeviceType()).isEqualTo("SMARTPHONE");
    }

    @Test
    public void testEmpty() {
        collector.setConfig(null);
        assertThat(collector.isProjectEnabled()).isTrue();
        assertThat(collector.getTrackingUrl()).isEmpty();
        assertThat(collector.getSuiUrl()).isEmpty();
        assertThat(collector.getTechnology()).isEmpty();
        assertThat(collector.getStreamCustomParameter().size()).isEqualTo(0);
        assertThat(collector.getContentCustomParameter().size()).isEqualTo(0);
        assertThat(collector.getProjectName()).isEmpty();
    }

    @Test
    public void testLanguage() {
        Matcher matcher = Pattern.compile("[a-z]{2}_[A-Z]{2}").matcher(collector.getLanguage());
        assertThat(matcher.matches()).isTrue();
    }

    @Test
    public void testVersion() {
        Matcher matcher = Pattern.compile("[0-9].[0-9]/{1}[0-9]\\.[0-9]\\.[0-9]/{1}[0-9].[0-9]{1}").matcher(collector.getVersion());
        assertThat(matcher.matches()).isTrue();
    }

    @Test
    public void testLoadSui() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        collector.setCollectorSuiCallback(countDownLatch::countDown);
        collector.loadSui();
        countDownLatch.await();
        assertFalse(collector.getSui().isEmpty());
    }

    @Test
    public void gettingSuiWithOptedInWorks() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final String[] sui = { null, null };
        collector.setOptin(true);
        collector.setCollectorSuiCallback(() -> {
            sui[0] = collector.getSui();
            countDownLatch.countDown();
        });
        collector.loadSui();
        countDownLatch.await();
        final CountDownLatch anotherCountDownLatch = new CountDownLatch(1);
        collector.setCollectorSuiCallback(() -> {
            sui[1] = collector.getSui();
            countDownLatch.countDown();
        });
        collector.loadSui();
        anotherCountDownLatch.await(10, TimeUnit.SECONDS);
        assertEquals(sui[1], sui[0]);
    }

    @Test
    public void gettingSuiWithOptedOutWorks() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final String[] sui = { null, null };
        collector.setOptin(false);
        collector.setCollectorSuiCallback(() -> {
            sui[0] = collector.getSui();
            countDownLatch.countDown();
        });
        collector.loadSui();
        countDownLatch.await();
        final CountDownLatch anotherCountDownLatch = new CountDownLatch(1);
        collector.setCollectorSuiCallback(() -> {
            sui[1] = collector.getSui();
            countDownLatch.countDown();
        });
        collector.loadSui();
        anotherCountDownLatch.await(10, TimeUnit.SECONDS);
        assertNotEquals(sui[1], sui[0]);
    }

    @Test
    public void gettingSuiWithFirstOptedInThenOutWorks() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final String[] sui = { null, null };
        collector.setOptin(true);
        collector.setCollectorSuiCallback(() -> {
            sui[0] = collector.getSui();
            countDownLatch.countDown();
        });
        collector.loadSui();
        countDownLatch.await();
        final CountDownLatch anotherCountDownLatch = new CountDownLatch(1);
        collector.setOptin(false);
        collector.setCollectorSuiCallback(() -> {
            sui[1] = collector.getSui();
            countDownLatch.countDown();
        });
        collector.loadSui();
        anotherCountDownLatch.await(10, TimeUnit.SECONDS);
        assertNotEquals(sui[1], sui[0]);
    }

    @Test
    public void addParametersToTPRequestWorks() {
        String suiUrl = "https://demo-config-preproduction.sensic.net/suigenerator";
        suiUrl = collector.addParameters(suiUrl);
        assertEquals("https://demo-config-preproduction.sensic.net/suigenerator?r=com.gfk.s2s.test&p=demo-config-preproduction", suiUrl);
    }
}
