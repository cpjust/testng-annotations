package io.github.cpjust.testng_annotations;

import lombok.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.mockito.Mock;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestNGMethod;
import org.testng.internal.ConstructorOrMethod;

import java.lang.reflect.Method;
import java.util.List;

import static org.mockito.Mockito.when;

public abstract class BaseTestEnvListener {
    public static final String ENV = "env";
    public static final String MATCH_ENV = "matchEnv";

    @Mock
    IMethodInstance methodInstance;

    @Mock
    ITestNGMethod testNgMethod;

    protected IMethodInterceptor listener;
    protected String previousEnv;

    @AfterEach
    void tearDown() {
        if (previousEnv == null) {
            System.clearProperty(ENV);
        } else {
            System.setProperty(ENV, previousEnv);
        }
    }

    /**
     * Helper to set the env system property, mock the method instance, and call the listener's intercept method.
     *
     * @param clazz      The class containing the method to test.
     * @param methodName The name of the method to test.
     * @return The list of IMethodInstance returned by the listener's intercept method.
     * @throws NoSuchMethodException If the specified method does not exist on the class.
     */
    protected List<IMethodInstance> setupMethodAndCallIntercept(@NonNull Class<?> clazz, String methodName) throws NoSuchMethodException {
        System.setProperty(ENV, MATCH_ENV);

        Method m = clazz.getMethod(methodName);
        when(methodInstance.getMethod()).thenReturn(testNgMethod);
        when(testNgMethod.getRealClass()).thenReturn(clazz);
        when(testNgMethod.getConstructorOrMethod()).thenReturn(new ConstructorOrMethod(m));

        return listener.intercept(List.of(methodInstance), null);
    }
}
