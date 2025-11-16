<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8" />
  <title>IoT Alert Dashboard</title>
  <style>
    body {
      font-family: Arial, sans-serif;
      background: #0f172a;
      color: #e5e7eb;
      margin: 0;
      padding: 0;
    }

    header {
      padding: 16px 24px;
      background: #111827;
      border-bottom: 1px solid #1f2937;
      display: flex;
      align-items: center;
      justify-content: space-between;
    }

    .title {
      font-size: 20px;
      font-weight: 600;
    }

    .status {
      font-size: 14px;
      padding: 4px 10px;
      border-radius: 999px;
    }

    .status.connected {
      background: #10b98133;
      color: #6ee7b7;
    }

    .status.disconnected {
      background: #ef444433;
      color: #fecaca;
    }

    main {
      padding: 20px 24px 40px;
      display: grid;
      grid-template-columns: 2fr 1fr;
      gap: 24px;
    }

    .card {
      background: #111827;
      border-radius: 12px;
      padding: 16px 18px;
      border: 1px solid #1f2937;
      box-shadow: 0 10px 25px rgba(0, 0, 0, 0.3);
    }

    .card h2 {
      margin: 0 0 12px;
      font-size: 16px;
      font-weight: 600;
      color: #f9fafb;
    }

    .card + .card {
      margin-top: 0;
    }

    .row {
      display: flex;
      align-items: center;
      margin-bottom: 8px;
      font-size: 14px;
    }

    .row span.label {
      width: 110px;
      color: #9ca3af;
    }

    .row span.value {
      font-weight: 500;
    }

    .alert-tag {
      display: inline-block;
      padding: 3px 10px;
      border-radius: 999px;
      font-size: 12px;
      font-weight: 600;
    }

    .alert-0 {
      background: #10b98133;
      color: #6ee7b7;
    }

    .alert-1 {
      background: #fbbf2433;
      color: #fde68a;
    }

    .alert-2 {
      background: #f9731633;
      color: #fed7aa;
    }

    .alert-3 {
      background: #ef444433;
      color: #fecaca;
    }

    .controls {
      display: flex;
      gap: 10px;
      margin-top: 10px;
      flex-wrap: wrap;
    }

    button {
      border: none;
      border-radius: 999px;
      padding: 8px 14px;
      font-size: 13px;
      cursor: pointer;
      background: #1d4ed8;
      color: #e5e7eb;
    }

    button.secondary {
      background: #374151;
    }

    button.danger {
      background: #b91c1c;
    }

    button:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    #sound-status {
      font-size: 13px;
      margin-top: 6px;
      color: #9ca3af;
    }

    #history {
      max-height: 420px;
      overflow-y: auto;
      font-size: 13px;
    }

    .history-item {
      padding: 8px 10px;
      border-radius: 8px;
      border: 1px solid #1f2937;
      margin-bottom: 6px;
      background: #020617;
    }

    .history-item-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 4px;
      font-size: 12px;
      color: #9ca3af;
    }

    .history-item-main {
      font-size: 13px;
    }

    .sensor-name {
      font-weight: 600;
      color: #e5e7eb;
    }

    .timestamp {
      font-size: 11px;
      color: #6b7280;
    }

    code {
      background: #020617;
      padding: 2px 4px;
      border-radius: 4px;
      font-size: 12px;
      border: 1px solid #1f2937;
    }
  </style>
</head>
<body>
<header>
  <div class="title">IoT Alert Dashboard</div>
  <div id="ws-status" class="status disconnected">WebSocket: Disconnected</div>
</header>

<main>
  <!-- TH? ALERT HI?N T?I -->
  <section class="card">
    <h2>C?nh b�o m?i nh?t</h2>
    <div class="row">
      <span class="label">Sensor:</span>
      <span class="value" id="current-sensor">-</span>
    </div>
    <div class="row">
      <span class="label">Gi� tr?:</span>
      <span class="value" id="current-value">-</span>
    </div>
    <div class="row">
      <span class="label">M?c:</span>
      <span class="value">
        <span id="current-level-tag" class="alert-tag alert-0" style="display:none;"></span>
      </span>
    </div>
    <div class="row">
      <span class="label">Th�ng ?i?p:</span>
      <span class="value" id="current-message">Ch?a c� c?nh b�o.</span>
    </div>
    <div class="row">
      <span class="label">Th?i gian:</span>
      <span class="value" id="current-time">-</span>
    </div>

    <div class="controls">
      <button id="btn-enable-sound">? B?t �m thanh</button>
      <button id="btn-test-alert" class="secondary">? G?i test alert (API)</button>
      <button id="btn-clear" class="danger">? X�a l?ch s?</button>
    </div>
    <div id="sound-status">�m thanh: ch?a b?t. Nh?n "B?t �m thanh" m?t l?n.</div>
  </section>

  <!-- L?CH S? ALERT -->
  <section class="card">
    <h2>L?ch s? c?nh b�o</h2>
    <div id="history"></div>
  </section>
</main>

<script>
  const WS_URL = "ws://127.0.0.1:9100";
  const TEST_ALERT_URL = "http://127.0.0.1:8000/test-alert";

  let ws = null;
  let soundEnabled = false;

  const wsStatusEl = document.getElementById("ws-status");
  const currentSensorEl = document.getElementById("current-sensor");
  const currentValueEl = document.getElementById("current-value");
  const currentLevelTagEl = document.getElementById("current-level-tag");
  const currentMessageEl = document.getElementById("current-message");
  const currentTimeEl = document.getElementById("current-time");
  const historyEl = document.getElementById("history");
  const soundStatusEl = document.getElementById("sound-status");

  const btnEnableSound = document.getElementById("btn-enable-sound");
  const btnTestAlert = document.getElementById("btn-test-alert");
  const btnClear = document.getElementById("btn-clear");

  function formatTime(ts) {
    try {
      // N?u server g?i timestamp (seconds) -> convert
      if (typeof ts === "number") {
        const d = new Date(ts * 1000);
        return d.toLocaleString();
      }
      // N?u l� string ISO
      const d = new Date(ts);
      if (!isNaN(d.getTime())) return d.toLocaleString();
    } catch (e) {}
    return new Date().toLocaleString();
  }

  function setWsStatus(connected) {
    if (connected) {
      wsStatusEl.textContent = "WebSocket: Connected";
      wsStatusEl.classList.remove("disconnected");
      wsStatusEl.classList.add("connected");
    } else {
      wsStatusEl.textContent = "WebSocket: Disconnected";
      wsStatusEl.classList.remove("connected");
      wsStatusEl.classList.add("disconnected");
    }
  }

  function appendHistory(data) {
    const item = document.createElement("div");
    item.className = "history-item";

    const header = document.createElement("div");
    header.className = "history-item-header";

    const left = document.createElement("div");
    left.innerHTML = `<span class="sensor-name">${'${'}data.sensor || "-"}</span> &mdash; <code>${'${'}data.level || "-"}</code>`;

    const right = document.createElement("div");
    right.className = "timestamp";
    right.textContent = formatTime(data.timestamp);

    header.appendChild(left);
    header.appendChild(right);

    const main = document.createElement("div");
    main.className = "history-item-main";
    main.textContent = (data.message || "") + " (value: " + (data.value ?? "-") + ")";

    item.appendChild(header);
    item.appendChild(main);

    historyEl.prepend(item); // m?i nh?t l�n ??u
  }

  async function playAudioFromUrl(url) {
    if (!soundEnabled) return;
    if (!url) return;

    try {
      const audio = new Audio(url);
      await audio.play();
      console.log("Audio playing:", url);
    } catch (err) {
      console.error("Cannot play audio:", err);
    }
  }

  function handleAlertMessage(data) {
    // C?p nh?t th? "C?nh b�o m?i nh?t"
    currentSensorEl.textContent = data.sensor || "-";
    currentValueEl.textContent = data.value ?? "-";
    currentMessageEl.textContent = data.message || "";
    currentTimeEl.textContent = formatTime(data.timestamp);

    const alertLevel = typeof data.alert === "number" ? data.alert : parseInt(data.alert || 0, 10);
    const levelName = data.level || "Unknown";

    currentLevelTagEl.style.display = "inline-block";
    currentLevelTagEl.textContent = `${'${'}levelName} (L${'${'}alertLevel})`;
    currentLevelTagEl.className = `alert-tag alert-${'${'}alertLevel}`;

    appendHistory(data);
    playAudioFromUrl(data.audio_url);
  }

  function initWebSocket() {
    ws = new WebSocket(WS_URL);

    ws.onopen = () => {
      console.log("WebSocket connected");
      setWsStatus(true);
    };

    ws.onclose = () => {
      console.log("WebSocket disconnected");
      setWsStatus(false);
      // Th? reconnect sau 3s
      setTimeout(initWebSocket, 3000);
    };

    ws.onerror = (err) => {
      console.error("WebSocket error:", err);
    };

    ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        console.log("WS message:", data);

        if (data.type === "connected") {
          // message kh?i t?o t? server
          return;
        }

        // Coi t?t c? message kh�c l� alert
        handleAlertMessage(data);
      } catch (e) {
        console.error("Invalid WS data:", event.data);
      }
    };
  }

  // N�t b?t �m thanh
  btnEnableSound.addEventListener("click", async () => {
    try {
      // t?o 1 �m thanh "silent" r?t ng?n ?? browser cho ph�p autoplay
      const testAudio = new Audio("data:audio/wav;base64,UklGRiQAAABXQVZFZm10IBAAAAABAAEAIlYAAB9AAACABAAZGF0YQAAAAA=");
      await testAudio.play().catch(() => {});
    } catch (e) {}

    soundEnabled = true;
    soundStatusEl.textContent = "�m thanh: ?� B?T. Khi c� c?nh b�o, audio s? t? ph�t.";
    btnEnableSound.disabled = true;
  });

  // N�t g?i /test-alert
  btnTestAlert.addEventListener("click", async () => {
    try {
      await fetch(TEST_ALERT_URL);
      alert("?� g?i /test-alert. N?u h? th?ng OK s? nh?n ???c alert qua WebSocket.");
    } catch (e) {
      console.error(e);
      alert("Kh�ng g?i ???c /test-alert. Ki?m tra server FastAPI.");
    }
  });

  // N�t clear l?ch s?
  btnClear.addEventListener("click", () => {
    historyEl.innerHTML = "";
  });

  // Kh?i ??ng WebSocket
  initWebSocket();
</script>
</body>
</html>