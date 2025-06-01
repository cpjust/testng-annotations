package com.github.cpjust.testng_annotations.listeners;

import com.github.cpjust.testng_annotations.TestBase;
import com.github.cpjust.testng_annotations.annotations.ExcludeOnEnv;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@ExcludeOnEnv(value = {"badEnv"})
public class ClassWithExcludeOnEnvTests extends TestBase {
    @Test
    public void classWithNoExcludeOnEnv_isNotRun() {
        classWithExcludeOnEnvTestsRun.add(getCurrentMethodName());
    }

    @ExcludeOnEnv(value = {"badEnv"})
    @Test
    public void classWithExcludedEnv_isNotRun() {
        classWithExcludeOnEnvTestsRun.add(getCurrentMethodName());
    }

    @ExcludeOnEnv(value = {"badEnv"})
    @Test(dataProvider = "testData")
    public void classWithExcludedEnv_isNotRun(String foo) {
        classWithExcludeOnEnvTestsRun.add(getCurrentMethodName(foo));
    }

    @ExcludeOnEnv(value = {"goodEnv"})
    @Test(dataProvider = "testData")
    public void classWithNonExcludedEnv_isNotRun(String foo) {
        classWithExcludeOnEnvTestsRun.add(getCurrentMethodName(foo));
    }

    @DataProvider
    private String[] testData() {
        return new String[] { "one", "two" };
    }
}
