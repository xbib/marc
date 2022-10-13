package org.xbib.marc.tools;

import org.junit.contrib.java.lang.system.internal.CheckExitCalled;
import org.junit.contrib.java.lang.system.internal.NoExitSecurityManager;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.platform.commons.support.ReflectionSupport;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.System.getSecurityManager;
import static java.lang.System.setSecurityManager;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * This class is original inspired from project {@code system-rules},
 * rewritten to JUnit5. It is a JUnit Jupiter extension that allows
 * in-test specification of expected {@code System.exit(...)} calls.
 */
@Target({TYPE, METHOD})
@Retention(RUNTIME)
@ExtendWith(ExpectedSystemExit.Extension.class)
public @interface ExpectedSystemExit {

    class Extension implements BeforeEachCallback,
            AfterEachCallback,
            BeforeAllCallback,
            ParameterResolver,
            TestExecutionExceptionHandler {

        private NoExitSecurityManager noExitSecurityManager;
        private SecurityManager originalManager;

        @Override
        public void beforeAll(ExtensionContext context) {
            noExitSecurityManager = new NoExitSecurityManager(getSecurityManager());
        }

        @Override
        public void beforeEach(ExtensionContext context) {
            originalManager = getSecurityManager();
            setSecurityManager(noExitSecurityManager);
        }

        @Override
        public void afterEach(ExtensionContext context) {
            NoExitSecurityManager securityManager = (NoExitSecurityManager) getSecurityManager();
            ExitCapture exitCapture = getExitCapture(context);

            if (exitCapture.expectExit) {
                checkSystemExit(securityManager, exitCapture);
            }
            setSecurityManager(originalManager);
        }

        @Override
        public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
            boolean isTestMethodLevel = extensionContext.getTestMethod().isPresent();
            boolean isOutputCapture = parameterContext.getParameter().getType() == ExitCapture.class;
            return isTestMethodLevel && isOutputCapture;
        }

        @Override
        public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
            return getExitCapture(extensionContext);
        }

        @Override
        public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
            ExitCapture exitCapture = getExitCapture(context);
            if (!(throwable instanceof CheckExitCalled) || !exitCapture.expectExit) {
                throw throwable;
            }
        }

        private void checkSystemExit(NoExitSecurityManager securityManager, ExitCapture exitCapture) {
            if (securityManager.isCheckExitCalled()) {
                exitCapture.handleSystemExitWithStatus(securityManager.getStatusOfFirstCheckExitCall());
            } else {
                exitCapture.handleMissingSystemExit();
            }
        }

        private ExitCapture getExitCapture(ExtensionContext context) {
            Store store = context.getStore(Namespace.create(getClass(), context.getRequiredTestMethod()));
            return store.getOrComputeIfAbsent(ExitCapture.class, ReflectionSupport::newInstance, ExitCapture.class);
        }
    }

    class ExitCapture {

        private boolean expectExit = false;

        private Integer expectedStatus = null;

        public void expectSystemExit() {
            expectExit = true;
        }

        public void expectSystemExitWithStatus(int status) {
            expectSystemExit();
            expectedStatus = status;
        }

        private void handleMissingSystemExit() {
            if (expectExit) {
                fail("System.exit has not been called.");
            }
        }

        private void handleSystemExitWithStatus(int status) {
            if (!expectExit) {
                fail("Unexpected call of System.exit(" + status + ").");
            } else if (expectedStatus != null) {
                assertEquals(expectedStatus, Integer.valueOf(status), "Wrong exit status");
            }
        }
    }
}
