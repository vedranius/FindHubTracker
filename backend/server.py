import os
import json
import time
import logging
import asyncio
from datetime import datetime
from pathlib import Path
from typing import Optional, List, Dict, Any
from contextlib import asynccontextmanager

from fastapi import FastAPI, HTTPException, BackgroundTasks
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from dotenv import load_dotenv

load_dotenv()

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("findhub-backend")

AUTH_DIR = os.getenv("AUTH_DIR", "./auth")
TRACKER_CACHE_FILE = os.path.join(AUTH_DIR, "tracker_cache.json")
REFRESH_INTERVAL = int(os.getenv("REFRESH_INTERVAL", "300"))
TRACKER_FILTER = os.getenv("TRACKER_FILTER", "")

tracker_cache: Dict[str, Any] = {}
last_refresh: float = 0


class TrackerLocation(BaseModel):
    id: str
    name: str
    latitude: float
    longitude: float
    timestamp: str
    battery_level: Optional[int] = None
    is_online: bool = False


class TrackerListResponse(BaseModel):
    trackers: List[TrackerLocation]
    last_updated: str
    count: int


class StatusResponse(BaseModel):
    status: str
    authenticated: bool
    last_refresh: Optional[str] = None
    tracker_count: int = 0


def load_cache() -> Dict[str, Any]:
    global tracker_cache
    if os.path.exists(TRACKER_CACHE_FILE):
        try:
            with open(TRACKER_CACHE_FILE, "r") as f:
                tracker_cache = json.load(f)
        except Exception as e:
            logger.error(f"Failed to load cache: {e}")
            tracker_cache = {}
    return tracker_cache


def save_cache():
    global tracker_cache
    try:
        os.makedirs(AUTH_DIR, exist_ok=True)
        with open(TRACKER_CACHE_FILE, "w") as f:
            json.dump(tracker_cache, f, indent=2)
    except Exception as e:
        logger.error(f"Failed to save cache: {e}")


async def fetch_tracker_locations():
    global tracker_cache, last_refresh

    logger.info("Fetching tracker locations from Find Hub...")

    try:
        import subprocess
        result = subprocess.run(
            ["python", "-c", """
import sys
sys.path.insert(0, '.')
from main import FindMyTools
tools = FindMyTools(auth_dir='{auth_dir}')
trackers = tools.get_trackers()
for t in trackers:
    print(json.dumps(t))
""".format(auth_dir=AUTH_DIR)],
            capture_output=True,
            text=True,
            timeout=60
        )

        if result.returncode == 0:
            for line in result.stdout.strip().split("\n"):
                if line:
                    try:
                        tracker = json.loads(line)
                        tracker_id = tracker.get("id", tracker.get("name", "unknown"))
                        tracker_cache[tracker_id] = {
                            "id": tracker_id,
                            "name": tracker.get("name", "Unknown Tracker"),
                            "latitude": tracker.get("latitude", 0),
                            "longitude": tracker.get("longitude", 0),
                            "timestamp": tracker.get("timestamp", datetime.now().isoformat()),
                            "battery_level": tracker.get("battery_level"),
                            "is_online": tracker.get("is_online", False)
                        }
                    except json.JSONDecodeError:
                        continue

            save_cache()
            last_refresh = time.time()
            logger.info(f"Fetched {len(tracker_cache)} tracker locations")
        else:
            logger.error(f"Failed to fetch trackers: {result.stderr}")

    except subprocess.TimeoutExpired:
        logger.error("Timeout while fetching tracker locations")
    except Exception as e:
        logger.error(f"Error fetching tracker locations: {e}")


def load_sample_trackers():
    global tracker_cache
    sample_file = os.path.join(AUTH_DIR, "sample_trackers.json")
    if os.path.exists(sample_file):
        with open(sample_file, "r") as f:
            trackers = json.load(f)
            for t in trackers:
                tracker_cache[t["id"]] = t
        logger.info(f"Loaded {len(trackers)} sample trackers")


@asynccontextmanager
async def lifespan(app: FastAPI):
    os.makedirs(AUTH_DIR, exist_ok=True)
    load_cache()
    load_sample_trackers()

    async def refresh_loop():
        while True:
            await fetch_tracker_locations()
            await asyncio.sleep(REFRESH_INTERVAL)

    task = asyncio.create_task(refresh_loop())
    yield
    task.cancel()


app = FastAPI(
    title="FindHub Tracker Backend",
    description="Backend server for FindHub Tracker Android app",
    version="1.0.0",
    lifespan=lifespan
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/", response_model=StatusResponse)
async def root():
    return StatusResponse(
        status="running",
        authenticated=os.path.exists(os.path.join(AUTH_DIR, "secrets.json")),
        last_refresh=datetime.fromtimestamp(last_refresh).isoformat() if last_refresh else None,
        tracker_count=len(tracker_cache)
    )


@app.get("/api/trackers", response_model=TrackerListResponse)
async def get_trackers():
    trackers = list(tracker_cache.values())

    if TRACKER_FILTER:
        filter_names = [n.strip().lower() for n in TRACKER_FILTER.split(",")]
        trackers = [t for t in trackers if t["name"].lower() in filter_names]

    return TrackerListResponse(
        trackers=[TrackerLocation(**t) for t in trackers],
        last_updated=datetime.fromtimestamp(last_refresh).isoformat() if last_refresh else datetime.now().isoformat(),
        count=len(trackers)
    )


@app.get("/api/trackers/{tracker_id}", response_model=TrackerLocation)
async def get_tracker(tracker_id: str):
    if tracker_id not in tracker_cache:
        raise HTTPException(status_code=404, detail="Tracker not found")
    return TrackerLocation(**tracker_cache[tracker_id])


@app.post("/api/refresh")
async def trigger_refresh(background_tasks: BackgroundTasks):
    background_tasks.add_task(fetch_tracker_locations)
    return {"message": "Refresh triggered"}


@app.get("/api/config")
async def get_config():
    return {
        "refresh_interval": REFRESH_INTERVAL,
        "tracker_filter": TRACKER_FILTER,
        "authenticated": os.path.exists(os.path.join(AUTH_DIR, "secrets.json"))
    }


if __name__ == "__main__":
    import uvicorn
    host = os.getenv("HOST", "0.0.0.0")
    port = int(os.getenv("PORT", "8000"))
    uvicorn.run(app, host=host, port=port)
