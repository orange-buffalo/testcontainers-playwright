FROM mcr.microsoft.com/playwright:focal

WORKDIR /app

COPY package.json ./

ARG PLAYWRIGHT_VERSION=1.32.0

RUN npm i playwright@${PLAYWRIGHT_VERSION}

COPY app.js .

EXPOSE 3000
EXPOSE 4444

CMD ["node", "app.js"]
