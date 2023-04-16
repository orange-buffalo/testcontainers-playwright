const http = require('http');
const {chromium} = require('playwright');

const port = 3000;
const wsPort = 4444;

function generateRandomString(length) {
  const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  let result = '';
  for (let i = 0; i < length; i++) {
    result += characters.charAt(Math.floor(Math.random() * characters.length));
  }
  return result;
}

async function launchBrowserServer() {
  const wsPath = generateRandomString(20);
  const browserServer = await chromium.launchServer({
    port: wsPort,
    wsPath: wsPath,
  });
  const wsEndpoint = browserServer.wsEndpoint();
  console.log(`Browser WebSocket Endpoint (internal): ${wsEndpoint}`);
  return wsPath;
}

const server = http.createServer(async (req, res) => {
  if (req.url === '/launch') {
    const wsPath = await launchBrowserServer();

    res.writeHead(200, {
      'Content-Type': 'application/json',
      'Access-Control-Allow-Origin': '*'
    });
    res.end(JSON.stringify({wsPath: wsPath}));
  } else {
    res.writeHead(404);
    res.end();
  }
});

server.listen(port, () => {
  console.log(`Server running at http://localhost:${port}/`);
});
