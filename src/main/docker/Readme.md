Playwright Browser Server Docker Image
This Docker image provides a Playwright browser server that allows clients to launch and connect to a browser instance running inside the container. Based on the Playwright Docker image for Node.js, it starts an HTTP server that listens for /v1/launch requests and starts a new browser server using BrowserType.launchServer() with a random WebSocket path.

Table of Contents
Requirements
Usage
API
POST /v1/launch
Requirements
Docker installed on your system
Usage
Pull the Docker image from the GitHub repository:
bash
Copy code
docker pull your-github-repo/playwright-browser-server:latest
Run a container from the pulled image:
bash
Copy code
docker run -p 3000:3000 -p 4444:4444 --name your_container_name your-github-repo/playwright-browser-server:latest
This will map the container's HTTP server port (3000) and WebSocket port (4444) to the host's respective ports.

Access the browser server by sending requests to the HTTP server at http://localhost:3000.
API
POST /v1/launch
Launches a new browser server and returns the random WebSocket path to connect to it.

Request
Method: POST
URL: /v1/launch
Response
Status: 200 OK
Content-Type: application/json
Body:
json
Copy code
{
  "wsPath": "random_websocket_path"
}
Example
Request:

http
Copy code
POST /v1/launch HTTP/1.1
Host: localhost:3000
Content-Length: 0
Response:

http
Copy code
HTTP/1.1 200 OK
Content-Type: application/json
Access-Control-Allow-Origin: *

{
  "wsPath": "c29tZV9yYW5kb21fcGF0aA"
}
Usage in a Playwright Client
To connect to the running browser server from another Playwright client, send an HTTP request to http://localhost:3000/v1/launch to receive the random WebSocket path. Use the WebSocket path to create a new Playwright browser instance by connecting to the remote browser server.

Here's an example of how to connect from another Playwright client:

javascript
Copy code
const fetch = require('node-fetch');
const { chromium } = require('playwright');

async function main() {
  const response = await fetch('http://localhost:3000/v1/launch', { method: 'POST' });
  const { wsPath } = await response.json();

  const wsEndpoint = `ws://localhost:4444/${wsPath}`;
  const browser = await chromium.connect({ wsEndpoint });
  const context = await browser.newContext();
  const page = await context.newPage();

  await page.goto('https://example.com');
  console.log(await page.title());

  await browser.close();
}

main();
Remember to install node-fetch in the client-side project using the following command:

bash
Copy code
npm install node-fetch
