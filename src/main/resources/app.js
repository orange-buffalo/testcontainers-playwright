const http = require('http');
const {chromium, firefox, webkit} = require('playwright');

const apiPort = 3000;

// to prevent garbage collection
const browsers = [];

function generateRandomString(length) {
  const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  let result = '';
  for (let i = 0; i < length; i++) {
    result += characters.charAt(Math.floor(Math.random() * characters.length));
  }
  return result;
}

async function launchBrowserServer(request) {
  console.info('Launching browser with settings', request);

  const wsPath = generateRandomString(20);
  const config = {
    port: request.port,
    wsPath: wsPath,
  }
  let browserType;
  if (request.browser === 'CHROMIUM') {
    browserType = chromium;
    console.info('Will start Chromium')
  } else if (request.browser === 'FIREFOX') {
    browserType = firefox;
    console.info('Will start Firefox')
  } else if (request.browser === 'WEBKIT') {
    browserType = webkit;
    console.info('Will start Webkit')
  } else {
    throw new Error(`Unknown browser: ${request.browser}`);
  }
  const browserServer = await browserType.launchServer(config);
  browsers.push(browserServer);
  const wsEndpoint = browserServer.wsEndpoint();
  console.info(`Browser WebSocket Endpoint (internal): ${wsEndpoint}`);
  return wsPath;
}

const server = http.createServer(async (req, res) => {
  if (req.url === '/launch') {
    let data = '';
    for await (const chunk of req) {
      data += chunk;
    }
    console.debug(`Received request to launch browser: ${data}`);
    const body = JSON.parse(data)

    try {
      const wsPath = await launchBrowserServer(body);

      res.writeHead(200, {
        'Content-Type': 'application/json',
        'Access-Control-Allow-Origin': '*'
      });
      res.end(JSON.stringify({wsPath: wsPath}));
    } catch (e) {
      console.error(e);
      res.writeHead(500);
      res.end();
    }
  } else {
    console.info(`Unknown request: ${req.url}`)

    res.writeHead(404);
    res.end();
  }
});

server.listen(apiPort, () => {
  console.info(`Server running at http://localhost:${apiPort}/`);
});
