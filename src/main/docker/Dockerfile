ARG PLAYWRIGHT_VERSION=1.32

FROM mcr.microsoft.com/playwright:v${PLAYWRIGHT_VERSION}.0-focal

WORKDIR /app

RUN chown pwuser:pwuser /app

USER pwuser

COPY --chown=pwuser:pwuser package.json ./

RUN npm i playwright@${PLAYWRIGHT_VERSION}

RUN echo "console.log('This image is not intended to be ran without Testcontainers. Refer to documentation in https://github.com/orange-buffalo/testcontainers-playwright');" >> app.js && \
    echo "${PLAYWRIGHT_VERSION}" >> version.txt

EXPOSE 3000
EXPOSE 4444

CMD ["node", "app.js"]
