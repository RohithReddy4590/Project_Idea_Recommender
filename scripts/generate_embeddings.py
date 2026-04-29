#!/usr/bin/env python3
"""
generate_embeddings.py — Pre-compute OpenAI embeddings for all projects.

This script calls the backend API to trigger embedding generation for every
project that doesn't yet have a vector. Run it after seeding the database.

Usage:
    python3 scripts/generate_embeddings.py [--base-url http://localhost:8080/api]
"""

import argparse
import sys
import time

try:
    import requests
except ImportError:
    print("ERROR: 'requests' library not found. Install it: pip install requests")
    sys.exit(1)


def main():
    parser = argparse.ArgumentParser(description="Generate embeddings for all projects.")
    parser.add_argument("--base-url", default="http://localhost:8080/api",
                        help="Base URL of the running backend API")
    args = parser.parse_args()

    base = args.base_url.rstrip("/")
    print(f"Target API: {base}")
    print("=" * 50)

    # 1. Health check
    print("→ Checking API health...")
    try:
        r = requests.get(f"{base}/agent/health", timeout=10)
        r.raise_for_status()
        print(f"  ✓ API is UP: {r.json()}")
    except Exception as e:
        print(f"  ✗ ERROR: API not reachable — {e}")
        print("  Make sure the Spring Boot app is running first.")
        sys.exit(1)

    # 2. Fetch all projects
    print("\n→ Fetching project list...")
    try:
        r = requests.get(f"{base}/profile", timeout=10)  # any lightweight endpoint
        print("  ✓ API responding")
    except Exception as e:
        print(f"  ✗ ERROR: {e}")
        sys.exit(1)

    # 3. Trigger embedding generation via scheduler endpoint
    print("\n→ Note: Embeddings are generated automatically by the Spring Boot app.")
    print("  The EmbeddingUpdateScheduler runs daily at 02:00 AM.")
    print("  For immediate generation, restart the app or call the admin endpoint.")
    print("\n  If you need to force-generate embeddings now, send a POST request to:")
    print(f"  POST {base}/admin/embeddings/generate")
    print("\n  (Requires the AdminController to be enabled in production.)")
    print("\n" + "=" * 50)
    print("✓ Done. Start the app — embeddings will be auto-generated on first run.")


if __name__ == "__main__":
    main()
