package io.github.cpjust.testng_annotations.listeners.annotation_transformers;

import io.github.cpjust.testng_annotations.annotations.EnumSource;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
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
}
