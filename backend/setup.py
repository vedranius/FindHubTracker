import os
import sys
import json
import shutil
from pathlib import Path


def create_env_file():
    env_file = Path(".env")
    if env_file.exists():
        print("✓ .env file already exists")
        return

    with open(env_file, "w") as f:
        f.write("# FindHub Tracker Backend Configuration\n")
        f.write("HOST=0.0.0.0\n")
        f.write("PORT=8000\n")
    print("✓ Created .env file")


def create_auth_dir():
    auth_dir = Path("auth")
    auth_dir.mkdir(exist_ok=True)
    print("✓ Created auth directory")


def create_sample_trackers():
    sample_file = Path("auth/sample_trackers.json")
    if sample_file.exists():
        print("✓ Sample trackers file exists")
        return

    sample_trackers = [
        {
            "id": "sample-tracker-1",
            "name": "Moji kljucevi",
            "latitude": 45.8150,
            "longitude": 15.9819,
            "timestamp": "2026-01-01T12:00:00",
            "battery_level": 85,
            "is_online": True
        },
        {
            "id": "sample-tracker-2",
            "name": "Torba",
            "latitude": 45.8160,
            "longitude": 15.9830,
            "timestamp": "2026-01-01T11:30:00",
            "battery_level": 60,
            "is_online": True
        }
    ]

    with open(sample_file, "w", encoding="utf-8") as f:
        json.dump(sample_trackers, f, indent=2, ensure_ascii=False)
    print("✓ Created sample trackers file (for testing)")


def main():
    print("=" * 50)
    print("FindHub Tracker Backend - Setup")
    print("=" * 50)
    print()

    create_env_file()
    create_auth_dir()
    create_sample_trackers()

    print()
    print("=" * 50)
    print("Setup complete!")
    print()
    print("To start server:")
    print("  python server.py")
    print()
    print("Then open http://localhost:8000/docs")
    print("=" * 50)


if __name__ == "__main__":
    main()
