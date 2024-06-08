# This is a default value that is overridden during a build
ARG PLAYWRIGHT_VERSION=1.32
# This is a default value that is overridden during a build based on Playwright version
ARG BASE_OS=focal

FROM mcr.microsoft.com/playwright:v${PLAYWRIGHT_VERSION}.0-${BASE_OS}

# This is a default value that is overridden during a build
ARG PLAYWRIGHT_VERSION=1.32

WORKDIR /app

RUN chown pwuser:pwuser /app

USER pwuser

COPY --chown=pwuser:pwuser package.json ./

RUN npm i playwright@${PLAYWRIGHT_VERSION} --save --save-exact

RUN echo "console.log('This image is not intended to be ran without Testcontainers. Refer to documentation in https://github.com/orange-buffalo/testcontainers-playwright');" >> app.js && \
    echo "${PLAYWRIGHT_VERSION}" >> version.txt

CMD ["node", "app.js"]
