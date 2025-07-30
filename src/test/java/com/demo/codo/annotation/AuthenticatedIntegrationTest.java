package com.demo.codo.annotation;

import com.demo.codo.TestContainerConfig;
import com.demo.codo.constant.TestUser;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestContainerConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@WithUserDetails(value = TestUser.EMAIL, setupBefore = TestExecutionEvent.TEST_METHOD)
public @interface AuthenticatedIntegrationTest {
}
