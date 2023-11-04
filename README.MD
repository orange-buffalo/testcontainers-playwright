# Playwright Testcontainers

![](https://maven-badges.herokuapp.com/maven-central/io.orange-buffalo/testcontainers-playwright/badge.svg?style=flat-square)

This library provides a [Testcontainers](https://www.testcontainers.org/)-based container for 
[Playwright](https://playwright.dev/java/) that enables running 
browser automation and testing without installing Playwright browsers locally.


It offers a low-level API to directly work with the container and a high-level API via JUnit 5 
extension for seamless integration with your tests.

- [Key Features](#key-features)
- [Limitations](#limitations)
- [Compatibility](#compatibility)
- [Usage](#usage)
  - [Core API](#core-api)
  - [Using container directly](#using-container-directly)
  - [Playwright JUnit 5 Extension Usage](#playwright-junit-5-extension-usage)
    - [Injecting Primitives](#injecting-primitives)
    - [Configuring the Extension](#configuring-the-extension)
    - [Local Development Experience](#local-development-experience)

## Key Features

* Supports Chromium, Firefox, and WebKit browsers.
* Manages the lifecycle of the Playwright container and browser instances.
* Provides a thread-bound wrapper around the Playwright API.
* Offers a JUnit 5 extension for seamless integration with your tests.

## Limitations

This library has a few limitations that you should be aware of:

* File system operations. Currently, file system operations like HAR dumps and tracing are not supported. 
   If you need this feature, please create an issue or vote for an existing one in the repository.

* Custom browsers. Custom browser support is not yet available. If you require this feature, 
   please create an issue or vote for an existing one in the repository.

* Versions compatibility. For remote execution, Playwright requires the client and server to be on 
   the same **minor** version; otherwise, they cannot communicate, and the client will switch to a local browser. 
   The Playwright Testcontainers library warns about version mismatches in the logs 
   if the classpath version does not match the container version.
   
   This is only relevant if you provide a custom image. Otherwise, we automatically select a compatible image
   based on the Playwright version on the classpath.

Please consider these limitations when deciding whether this library is suitable for your testing needs.

## Compatibility

* Playwright 1.26.0+
* Testcontainers 1.17.0+
* Java 11+

## Usage

To use this library, you'll need to add it as a dependency to your project.

Maven:
```xml
<dependency>
    <groupId>io.orange-buffalo</groupId>
    <artifactId>testcontainers-playwright</artifactId>
    <scope>test</scope>
    <version>0.11.4</version>
</dependency>
```
Gradle:
```kotlin
testImplementation("io.orange-buffalo:testcontainers-playwright:0.11.4")
```

Then, you can create a `PlaywrightContainer` instance and use the provided API to access the browser instances.

For JUnit 5, it is recommended to use the `PlaywrightExtension` to benefit from additional features.

### Core API

* `PlaywrightApi`: Provides access to the Playwright API for browser automation and testing.

  Instances of this interface are not thread-safe, so each thread should have its own instance. 
  Use `PlaywrightContainer.getPlaywrightApi()` or declare a JUnit 5 test method parameter to obtain a thread-bound instance.
  
  Please note, this is a low-level API. Typically, your JUnit 5 test will inject `Page` or `BrowserContext`.
  See below for extension details.

### Using container directly

Using `PlaywrightContainer` directly allows you to manage the container lifecycle and obtain connected browser instances. 
Browsers are started lazily upon the first request to the API.

```java
import io.orangebuffalo.testcontainers.playwright.PlaywrightApi;
import io.orangebuffalo.testcontainers.playwright.PlaywrightContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class MyPlaywrightTests {

    @RegisterExtension
    public static final PlaywrightContainer playwrightContainer = new PlaywrightContainer();

    private PlaywrightApi playwrightApi;

    @BeforeEach
    void setUp() {
        playwrightApi = playwrightContainer.getPlaywrightApi();
    }

    @Test
    void myTest() {
        Browser browser = playwrightApi.chromium();
        Page page = browser.newPage();
        page.navigate("https://www.example.com");
        // Perform test actions and assertions here
    }
}
```

### Playwright JUnit 5 Extension Usage
The Playwright JUnit 5 Extension makes it easy to use Playwright in your JUnit 5 tests. 
The extension supports injecting `PlaywrightApi`, `Page`, and `BrowserContext` instances directly into your test methods.

#### Injecting Primitives
By default, the injected instances are bound to the Chromium browser, but you can use the provided annotations to 
select a specific browser. Use `@RequiresChromium`, `@RequiresFirefox`, or `@RequiresWebkit` annotations to 
specify the browser for the injected instances. The annotations can be applied to a parameter, 
test method, or test class, both directly and through meta-annotations.

Here's a Java example of using the extension:

```java
import io.orangebuffalo.testcontainers.playwright.junit.PlaywrightExtension;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(PlaywrightExtension.class)
class MyTests {

    @Test
    void testWithInjectedPage(@RequiresChromium Page page) {
        page.navigate("https://example.com");
        // Perform your test actions
    }
}
```
Please note that the extension is only responsible for managing the lifecycle of the injected primitives. 
If you create any derived instances from these primitives, it is your responsibility to close them within the test. 
The extension will close the instances it provides but not any derived ones.

Here's an example demonstrating the injection of `PlaywrightApi` and `BrowserContext`, as well as the creation 
and manual management of derived instances:

```java
import io.orangebuffalo.testcontainers.playwright.junit.*;
import com.microsoft.playwright.*;

@ExtendWith(PlaywrightExtension.class)
class MyTests {

    @Test
    void testWithInjectedApi(PlaywrightApi playwrightApi) {
        try (BrowserContext derivedContext = playwrightApi.chromium().newContext();
             Page derivedPage = derivedContext.newPage()) {
            // Perform your test actions
        }
    }

    @Test
    void testWithInjectedBrowserContext(BrowserContext browserContext) {
        try (Page derivedPage = browserContext.newPage()) {
            // Perform your test actions
        }
    }
}
```
In this example, the derived instances `derivedPage` and `derivedContext` are created from the injected primitives. 
These instances must be closed manually within the test using a try-with-resources block, 
as the extension will not manage their lifecycle.

#### Configuring the Extension
The extension can be configured using the `@PlaywrightConfig` annotation and the `PlaywrightConfigurer` interface. 
You can customize the container used to start Playwright, the browser context options, or even replace 
the `PlaywrightApi` with a custom implementation.

For example, to set the default URL for the browser context or to connect the container to a network to access 
a web app under test, implement the `PlaywrightConfigurer` interface and provide the configuration class in the 
`@PlaywrightConfig` annotation on your test class.

Here's an example of configuring the extension:

```java
import io.orangebuffalo.testcontainers.playwright.junit.*;

@PlaywrightConfig(configurer = MyPlaywrightConfigurer.class)
@ExtendWith(PlaywrightExtension.class)
class MyTests {

    @Test
    void testWithInjectedPage(Page page) {
        // Perform your test actions
    }
}

class MyPlaywrightConfigurer implements PlaywrightConfigurer {

    @Override
    public NewContextOptions createBrowserContextOptions() {
        return new NewContextOptions().setBaseURL("https://example.com");
    }
}
```
You can also enable the extension and its configuration using a custom annotation:

```java
import io.orangebuffalo.testcontainers.playwright.junit.*;
import org.junit.jupiter.api.extension.ExtendWith;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtendWith(PlaywrightExtension.class)
@PlaywrightConfig(configurer = CustomPlaywrightConfigurer.class)
@interface CustomPlaywrightTests {
}

class CustomPlaywrightConfigurer implements PlaywrightConfigurer {
    // Your custom configuration methods
}

@CustomPlaywrightTests
class MyCustomTests {

    @Test
    void testWithInjectedPage(Page page) {
        // Perform your test actions
    }
}
```
In this example, the `CustomPlaywrightTests` annotation enables the extension and specifies the custom 
configuration through the `CustomPlaywrightConfigurer` class. 
This allows you to reuse the same configuration across multiple test classes.

#### Local Development Experience
The `createPlaywrightApiProvider` method in the `PlaywrightConfigurer` interface allows you to replace the 
`PlaywrightApi` with a custom implementation, bypassing Testcontainers and Docker. This is useful for improving 
the local development experience by running tests with local browsers when working in an IDE, 
while still running tests in CI with Docker.

The provided `LocalPlaywrightApiProvider` class can be used to create local browser instances in non-headless mode, 
allowing you to visually see the browser actions during test execution.

Please refer to the provided code examples for a more in-depth understanding 
of the available API and configuration options.
