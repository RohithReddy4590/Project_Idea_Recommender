import json
import requests

# This script can be used to manually push seed data to the API
# though the backend seeds automatically on startup.

API_URL = "http://localhost:8080/api/projects"

def seed():
    with open('../backend/src/main/resources/seed_projects.json', 'r') as f:
        projects = json.load(f)
    
    for project in projects:
        try:
            response = requests.post(API_URL, json=project)
            if response.status_code == 200:
                print(f"Seeded: {project['title']}")
            else:
                print(f"Failed to seed: {project['title']} - {response.status_code}")
        except Exception as e:
            print(f"Error seeding {project['title']}: {e}")

if __name__ == "__main__":
    print("Starting manual seeding...")
    # seed()
