import http.server
import json
import os
import time
from urllib.parse import urlparse, parse_qs
from datetime import datetime

AUTH_DIR = "./auth"
TRACKER_CACHE_FILE = os.path.join(AUTH_DIR, "tracker_cache.json")

tracker_cache = {}
last_refresh = 0


def load_cache():
    global tracker_cache
    if os.path.exists(TRACKER_CACHE_FILE):
        try:
            with open(TRACKER_CACHE_FILE, "r", encoding="utf-8") as f:
                tracker_cache = json.load(f)
        except Exception:
            tracker_cache = {}


def save_cache():
    os.makedirs(AUTH_DIR, exist_ok=True)
    with open(TRACKER_CACHE_FILE, "w", encoding="utf-8") as f:
        json.dump(tracker_cache, f, indent=2, ensure_ascii=False)


def load_sample_trackers():
    global tracker_cache
    sample_file = os.path.join(AUTH_DIR, "sample_trackers.json")
    if os.path.exists(sample_file):
        try:
            with open(sample_file, "r", encoding="utf-8") as f:
                trackers = json.load(f)
                for t in trackers:
                    tracker_cache[t["id"]] = t
        except Exception:
            pass


class FindHubHandler(http.server.BaseHTTPRequestHandler):

    def do_GET(self):
        parsed = urlparse(self.path)
        path = parsed.path

        if path == "/":
            self.send_json({
                "status": "running",
                "authenticated": os.path.exists(os.path.join(AUTH_DIR, "secrets.json")),
                "last_refresh": datetime.fromtimestamp(last_refresh).isoformat() if last_refresh else None,
                "tracker_count": len(tracker_cache)
            })

        elif path == "/api/trackers":
            trackers = list(tracker_cache.values())
            self.send_json({
                "trackers": trackers,
                "last_updated": datetime.fromtimestamp(last_refresh).isoformat() if last_refresh else datetime.now().isoformat(),
                "count": len(trackers)
            })

        elif path.startswith("/api/trackers/"):
            tracker_id = path.split("/")[-1]
            if tracker_id in tracker_cache:
                self.send_json(tracker_cache[tracker_id])
            else:
                self.send_error(404, "Tracker not found")

        elif path == "/api/config":
            self.send_json({
                "refresh_interval": 300,
                "authenticated": os.path.exists(os.path.join(AUTH_DIR, "secrets.json"))
            })

        elif path == "/docs":
            self.send_html(self.get_docs_html())

        else:
            self.send_error(404, "Not found")

    def do_POST(self):
        parsed = urlparse(self.path)
        path = parsed.path

        if path == "/api/refresh":
            self.send_json({"message": "Refresh triggered"})
        else:
            self.send_error(404, "Not found")

    def send_json(self, data):
        response = json.dumps(data, ensure_ascii=False).encode("utf-8")
        self.send_response(200)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(response)))
        self.send_header("Access-Control-Allow-Origin", "*")
        self.end_headers()
        self.wfile.write(response)

    def send_html(self, html):
        response = html.encode("utf-8")
        self.send_response(200)
        self.send_header("Content-Type", "text/html; charset=utf-8")
        self.send_header("Content-Length", str(len(response)))
        self.end_headers()
        self.wfile.write(response)

    def log_message(self, format, *args):
        print(f"[{datetime.now().strftime('%H:%M:%S')}] {args[0]}")

    def get_docs_html(self):
        return """<!DOCTYPE html>
<html>
<head>
    <title>FindHub Tracker API</title>
    <style>
        body { font-family: Arial, sans-serif; max-width: 800px; margin: 40px auto; padding: 20px; }
        h1 { color: #333; }
        .endpoint { background: #f5f5f5; padding: 15px; margin: 10px 0; border-radius: 5px; }
        .method { background: #4CAF50; color: white; padding: 3px 8px; border-radius: 3px; font-weight: bold; }
        .method-post { background: #2196F3; }
        code { background: #eee; padding: 2px 6px; border-radius: 3px; }
    </style>
</head>
<body>
    <h1>FindHub Tracker API</h1>
    <p>Backend server za praćenje Google Find Hub trackera.</p>

    <div class="endpoint">
        <span class="method">GET</span> <code>/</code>
        <p>Status servera</p>
    </div>

    <div class="endpoint">
        <span class="method">GET</span> <code>/api/trackers</code>
        <p>Lista svih trackera</p>
    </div>

    <div class="endpoint">
        <span class="method">GET</span> <code>/api/trackers/{id}</code>
        <p>Detalji jednog trackera</p>
    </div>

    <div class="endpoint">
        <span class="method method-post">POST</span> <code>/api/refresh</code>
        <p>Ručno osvježavanje lokacija</p>
    </div>

    <div class="endpoint">
        <span class="method">GET</span> <code>/api/config</code>
        <p>Konfiguracija servera</p>
    </div>
</body>
</html>"""


def main():
    os.makedirs(AUTH_DIR, exist_ok=True)
    load_cache()
    load_sample_trackers()

    host = "0.0.0.0"
    port = 8000

    server = http.server.HTTPServer((host, port), FindHubHandler)
    print(f"FindHub Tracker Backend running on http://{host}:{port}")
    print(f"API docs: http://localhost:{port}/docs")
    print(f"Loaded {len(tracker_cache)} trackers")
    print("Press Ctrl+C to stop")

    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\nStopping server...")
        server.server_close()


if __name__ == "__main__":
    main()
