package com.gfk.s2s.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.LibTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(LibTestRunner.class)
public class HTTPClientTest {

    final String unitTestConfigUrl = "http://localhost:8881/s2s-android-unittest.json";

    @Test
    public void testGetOK() throws InterruptedException {
        final String[] result = new String[1];

        HTTPClient.get(unitTestConfigUrl, new IHttpClientFullCallback<String>() {

            @Override
            public void onCompletion(String data) {
                result[0] = data;
            }

            @Override
            public void onFinished() {

            }
        });

        Thread.sleep(50);
        assertThat(result[0].isEmpty()).isFalse();
    }

    @Test
    public void testGetBadURL() throws InterruptedException {
        final String[] result = new String[1];

        HTTPClient.get("ht", new IHttpClientFullCallback<String>() {
            @Override
            public void onCompletion(String data) {
                result[0] = data;
            }

            @Override
            public void onFinished() {

            }
        });

        Thread.sleep(5);
        assertThat(result[0]).isNull();
    }

    @Test
    public void testGetNotReachable() throws InterruptedException {
        final String[] result = new String[1];

        HTTPClient.get("http://localhost:8881", new IHttpClientFullCallback<String>() {
            @Override
            public void onCompletion(String data) {
                result[0] = data;
            }

            @Override
            public void onFinished() {

            }
        });

        Thread.sleep(5);
        assertThat(result[0]).isNull();
    }

    @Test
    public void testGetRetry() throws InterruptedException {
        final String[] result = new String[1];

        HTTPClient.get("http://localhost:8881/dev-s2s.json-not-existing", 3, new IHttpClientFullCallback<String>() {
            @Override
            public void onCompletion(String data) {
                result[0] = data;
            }

            @Override
            public void onFinished() {

            }
        });

        Thread.sleep(50);
        assertThat(result[0]).isNull();
    }

    @Test
    public void testPost() throws InterruptedException {
        String postData = "bla=123";
        final boolean[] result = {false};

        HTTPClient.post("http://localhost:8881", postData, success -> result[0] = success);

        Thread.sleep(5);
        assertThat(result[0]);
    }

    @Test
    public void testPostWithInstantiable() throws InterruptedException {
        String postData = "bla=123";
        final boolean[] result = {false};

        new HTTPClient.Instantiable().post("http://localhost:8881", postData, success -> result[0] = success);

        Thread.sleep(5);
        assertThat(result[0]);
    }

    @Test
    public void testPostBadURL() throws InterruptedException {
        String postData = "bla=123";
        final boolean[] result = {true};

        HTTPClient.post("http://localhost:8881/das", postData, success -> result[0] = success);

        Thread.sleep(5);
        assertThat(!result[0]);
    }

    @Test
    public void testPostNotReachable() throws InterruptedException {
        String postData = "bla=123";
        final boolean[] result = {true};

        HTTPClient.post("http://localhost:8881/not-exisitng", postData, success -> result[0] = success);

        Thread.sleep(5);
        assertThat(!result[0]);
    }
}
