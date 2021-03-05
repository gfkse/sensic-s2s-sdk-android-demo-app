package org.robolectric;

import org.junit.runners.model.InitializationError;
import org.robolectric.annotation.Config;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.ShadowMap;

/**
 * Test runner customized for running unit tests either through the Gradle CLI or
 * Android Studio. The runner uses the build type and build flavor to compute the
 * resource, asset, and AndroidManifest paths.
 * <p/>
 * This test runner requires that you set the 'constants' field on the @Config
 * annotation (or the org.robolectric.Config.properties file) for your tests.
 *
 * Deprecation note: I will remove this class in a subsequent merge request from every test.
 */
@Deprecated
public class LibTestRunner extends RobolectricTestRunner {

    public LibTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

}

