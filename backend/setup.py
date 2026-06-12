"""
FindHub Tracker Backend - Setup Script

This script helps you set up the backend server.
Run: python setup.py
"""

import os
import sys
import json
import shutil
from pathlib import Path


def check_python_version():
    if sys.version_info < (3, 8):
        print("ERROR: Python 3.8 or higher is required")
        sys.exit(1)
    print(f"✓ Python {sys.version_info.major}.{sys.version_info.minor}")


def create_env_file():
    env_file = Path(".env")
    env_example = Path(".env.example")

    if env_file.exists():
        print("✓ .env file already exists")
        return

    if env_example.exists():
        shutil.copy(env_example, env_file)
        print("✓ Created .env file from template")
        print("  → Edit .env to configure your server")
    else:
        print("✗ .env.example not found")


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
            "name": "Moji ključevi",
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

    with open(sample_file, "w") as f:
        json.dump(sample_trackers, f, indent=2)
    print("✓ Created sample trackers file (for testing)")


def install_requirements():
    print("\nInstalling requirements...")
    os.system(f"{sys.executable} -m pip install -r requirements.txt -q")
    print("✓ Requirements installed")


def main():
    print("=" * 50)
    print("FindHub Tracker Backend - Setup")
    print("=" * 50)
    print()

    check_python_version()
    create_env_file()
    create_auth_dir()
    create_sample_trackers()
    install_requirements()

    print()
    print("=" * 50)
    print("Setup complete!")
    print()
    print("Next steps:")
    print("1. Edit .env file with your settings")
    print("2. Run: python server.py")
    print("3. Open http://localhost:8000/docs for API documentation")
    print("4. Test with sample data or connect Google Find Hub")
    print("=" * 50)


if __name__ == "__main__":
    main()
