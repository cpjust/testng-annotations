package io.github.cpjust.testng_annotations.listeners.annotation_transformers;

import io.github.cpjust.testng_annotations.annotations.EnumSource;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.oneOf;

/**
 * Integration tests for {@link EnumSourceListener}.
 */
public class EnumSourceListenerIT {

    public enum IntegrationTestEnum {
        ALPHA,
        BETA,
        GAMMA
    }

    // NOTE: We only need to provide the dataProvider and dataProviderClass in the @Test annotation if we don't register the ValueSourceListener
    // in the resources/META-INF/services/org.testng.ITestNGListener file.
    @Test(dataProvider = EnumSourceListener.ENUM_SOURCE_PROVIDER, dataProviderClass = EnumSourceListener.class)
    @EnumSource(IntegrationTestEnum.class)
    public void enumSourceProvider_integrationTest_allConstantsProvided(IntegrationTestEnum value) {
        assertThat("The enum shouldn't be null!", value, notNullValue());
    }

    @Test
    @EnumSource(value = IntegrationTestEnum.class, names = {"ALPHA", "BETA"})
    public void enumSourceProvider_integrationTest_specificConstantsProvided(IntegrationTestEnum value) {
        assertThat("Wrong enum value!", value, oneOf(IntegrationTestEnum.ALPHA, IntegrationTestEnum.BETA));
    }

    @Test
    @EnumSource(value = IntegrationTestEnum.class, names = {"ALPHA"}, mode = EnumSource.Mode.EXCLUDE)
    public void enumSourceProvider_integrationTest_excludeMode(IntegrationTestEnum value) {
        // When EXCLUDE is used with ALPHA, the remaining values should be BETA and GAMMA.
        assertThat("Wrong enum value!", value, oneOf(IntegrationTestEnum.BETA, IntegrationTestEnum.GAMMA));
    }

    @Test
    @EnumSource(value = IntegrationTestEnum.class, names = {"^A.*$", "^.*A$"}, mode = EnumSource.Mode.MATCH_ALL)
    public void enumSourceProvider_integrationTest_matchAllMode(IntegrationTestEnum value) {
        // Only ALPHA starts and ends with 'A'.
        assertThat("Wrong enum value!", value, equalTo(IntegrationTestEnum.ALPHA));
    }

    @Test
    @EnumSource(value = IntegrationTestEnum.class, names = {"^A.*$", "^B.*$"}, mode = EnumSource.Mode.MATCH_ANY)
    public void enumSourceProvider_integrationTest_matchAnyMode(IntegrationTestEnum value) {
        // Only ALPHA and BETA start with 'A' or 'B'.
        assertThat("Wrong enum value!", value, oneOf(IntegrationTestEnum.ALPHA, IntegrationTestEnum.BETA));
    }
}
