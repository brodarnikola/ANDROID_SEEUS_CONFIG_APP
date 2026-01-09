const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 5000;
const HOST = '0.0.0.0';

const server = http.createServer((req, res) => {
  res.setHeader('Cache-Control', 'no-cache, no-store, must-revalidate');
  res.setHeader('Content-Type', 'text/html');
  res.writeHead(200);
  
  const html = `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>SeeUs Admin - Android App</title>
  <style>
    * { margin: 0; padding: 0; box-sizing: border-box; }
    body {
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif;
      background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
      min-height: 100vh;
      color: #fff;
      padding: 40px 20px;
    }
    .container {
      max-width: 800px;
      margin: 0 auto;
    }
    .header {
      text-align: center;
      margin-bottom: 40px;
    }
    .header h1 {
      font-size: 2.5rem;
      margin-bottom: 10px;
      color: #4da8da;
    }
    .header p {
      color: #a0a0a0;
      font-size: 1.1rem;
    }
    .card {
      background: rgba(255, 255, 255, 0.05);
      border-radius: 16px;
      padding: 30px;
      margin-bottom: 20px;
      border: 1px solid rgba(255, 255, 255, 0.1);
    }
    .card h2 {
      color: #4da8da;
      margin-bottom: 15px;
      font-size: 1.4rem;
    }
    .card p, .card li {
      color: #c0c0c0;
      line-height: 1.7;
    }
    .card ul {
      margin-left: 20px;
      margin-top: 10px;
    }
    .card li {
      margin-bottom: 8px;
    }
    .warning {
      background: rgba(255, 193, 7, 0.1);
      border: 1px solid rgba(255, 193, 7, 0.3);
    }
    .warning h2 {
      color: #ffc107;
    }
    .code {
      background: rgba(0, 0, 0, 0.3);
      padding: 15px;
      border-radius: 8px;
      margin-top: 15px;
      font-family: 'Monaco', 'Menlo', monospace;
      font-size: 0.9rem;
      overflow-x: auto;
    }
    .badge {
      display: inline-block;
      padding: 4px 12px;
      background: #4da8da;
      color: #fff;
      border-radius: 20px;
      font-size: 0.8rem;
      margin-right: 8px;
      margin-bottom: 8px;
    }
    .badge.kotlin { background: #7F52FF; }
    .badge.android { background: #3DDC84; }
    .badge.firebase { background: #FFCA28; color: #000; }
    .badge.bluetooth { background: #0A84FF; }
  </style>
</head>
<body>
  <div class="container">
    <div class="header">
      <h1>SeeUs Admin</h1>
      <p>Android Configuration Application v1.11.3</p>
    </div>

    <div class="card warning">
      <h2>Native Android App</h2>
      <p>This project is a native Android application and cannot run directly in a web browser. It requires Android Studio and an Android device or emulator to build and run.</p>
    </div>

    <div class="card">
      <h2>Technology Stack</h2>
      <div style="margin-top: 15px;">
        <span class="badge android">Android SDK 33</span>
        <span class="badge kotlin">Kotlin 1.7.22</span>
        <span class="badge firebase">Firebase</span>
        <span class="badge bluetooth">BLE Scanner</span>
        <span class="badge">Google Maps</span>
        <span class="badge">Coroutines</span>
      </div>
    </div>

    <div class="card">
      <h2>Project Structure</h2>
      <ul>
        <li><strong>admin/</strong> - Main Android application module</li>
        <li><strong>core/</strong> - Core library module with shared functionality</li>
      </ul>
      <p style="margin-top: 15px;">Package: <code>hr.sil.android.seeusconfig</code></p>
    </div>

    <div class="card">
      <h2>Key Features</h2>
      <ul>
        <li>Bluetooth Low Energy (BLE) device scanning and communication</li>
        <li>Google Maps integration for location services</li>
        <li>Firebase Cloud Messaging for notifications</li>
        <li>REST API integration</li>
        <li>Data caching</li>
      </ul>
    </div>

    <div class="card">
      <h2>Build Instructions</h2>
      <p>To build and run this project locally:</p>
      <div class="code">
# Clone the repository<br>
git clone [repository-url]<br><br>
# Open in Android Studio<br>
# File -> Open -> Select project folder<br><br>
# Build the project<br>
./gradlew assembleDebug<br><br>
# Install on connected device<br>
./gradlew installDebug
      </div>
    </div>

    <div class="card">
      <h2>Requirements</h2>
      <ul>
        <li>Android Studio Arctic Fox or later</li>
        <li>JDK 17</li>
        <li>Android SDK 33</li>
        <li>Android device or emulator with SDK 26+</li>
        <li>Google Play Services</li>
      </ul>
    </div>
  </div>
</body>
</html>`;
  
  res.end(html);
});

server.listen(PORT, HOST, () => {
  console.log(`Server running at http://${HOST}:${PORT}`);
});
