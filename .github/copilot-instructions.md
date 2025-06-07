## General

1. Be concise and clear in your responses. Avoid extra details unless specifically requested.
2. Do not provide summaries of the executed actions.
3. Never add noisy comments or explanations to the code. Prefer self-explanatory code. Only add comments for non-trivial
   decisions where it is not clear why the code is written that way.
4. Take your time and think about the task before starting to write code. Provide a plan of the changes.

## Tests

- Use Kotest assertions and matchers via method invocation (e.g., `shouldBe(x)`, `shouldContain(y)`), not postfix notation.
- Prefer concise, self-explanatory test code. Avoid unnecessary comments or boilerplate.
- Use helper functions for repeated test logic (see `TestUtils.kt`).
- Use JUnit 5 for test structure and lifecycle management.
- Group related tests using `@Nested` classes and descriptive `@DisplayName` annotations.
- Use imperative style for test setup and assertions.
- Prefer explicit assertion and error handling (e.g., `shouldThrow { ... }`).
