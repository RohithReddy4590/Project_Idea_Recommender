#!/usr/bin/env python3
"""
evaluate_recommendations.py — Evaluate recommendation quality.

Creates test student profiles, calls the recommendation API, and measures:
  - Number of recommendations returned
  - Average match score
  - Skill gap coverage
  - Response time

Usage:
    python3 scripts/evaluate_recommendations.py [--base-url http://localhost:8080/api]
"""

import argparse
import json
import sys
import time
import statistics

try:
    import requests
except ImportError:
    print("ERROR: Install requests: pip install requests")
    sys.exit(1)

# ── Test profiles ─────────────────────────────────────────────────────────────
TEST_PROFILES = [
    {
        "name": "Alice Backend Dev",
        "email": f"alice_eval_{int(time.time())}@test.com",
        "careerGoal": "Senior Software Engineer",
        "domainInterests": "Backend Development",
        "experienceLevel": "INTERMEDIATE",
        "skills": [
            {"skillName": "Java", "proficiencyLevel": "ADVANCED", "yearsExperience": 3.0},
            {"skillName": "Spring Boot", "proficiencyLevel": "INTERMEDIATE", "yearsExperience": 2.0},
            {"skillName": "MySQL", "proficiencyLevel": "INTERMEDIATE", "yearsExperience": 2.0},
            {"skillName": "REST API", "proficiencyLevel": "INTERMEDIATE", "yearsExperience": 2.0},
        ]
    },
    {
        "name": "Bob AI Engineer",
        "email": f"bob_eval_{int(time.time())}@test.com",
        "careerGoal": "Machine Learning Engineer",
        "domainInterests": "AI/ML, Backend Development",
        "experienceLevel": "ADVANCED",
        "skills": [
            {"skillName": "Python", "proficiencyLevel": "ADVANCED", "yearsExperience": 4.0},
            {"skillName": "Machine Learning", "proficiencyLevel": "INTERMEDIATE", "yearsExperience": 2.0},
            {"skillName": "Java", "proficiencyLevel": "INTERMEDIATE", "yearsExperience": 2.0},
            {"skillName": "SQL", "proficiencyLevel": "INTERMEDIATE", "yearsExperience": 3.0},
        ]
    },
    {
        "name": "Carol Beginner",
        "email": f"carol_eval_{int(time.time())}@test.com",
        "careerGoal": "Junior Frontend Developer",
        "domainInterests": "Web Development, Frontend",
        "experienceLevel": "BEGINNER",
        "skills": [
            {"skillName": "JavaScript", "proficiencyLevel": "BEGINNER", "yearsExperience": 0.5},
            {"skillName": "CSS", "proficiencyLevel": "BEGINNER", "yearsExperience": 0.5},
            {"skillName": "React", "proficiencyLevel": "BEGINNER", "yearsExperience": 0.3},
        ]
    },
]

def evaluate(base_url: str):
    results = []
    print(f"\nEvaluating recommendations at: {base_url}")
    print("=" * 60)

    for profile_data in TEST_PROFILES:
        print(f"\n→ Testing profile: {profile_data['name']}")

        # Create profile
        try:
            r = requests.post(f"{base_url}/profile/create", json=profile_data, timeout=30)
            r.raise_for_status()
            profile = r.json()
            student_id = profile["id"]
            print(f"  ✓ Profile created (id={student_id})")
        except Exception as e:
            print(f"  ✗ Profile creation failed: {e}")
            continue

        # Get recommendations
        session_id = f"eval-session-{student_id}-{int(time.time())}"
        start = time.time()
        try:
            r = requests.post(f"{base_url}/recommendations",
                              json={"studentId": student_id, "sessionId": session_id, "maxResults": 10},
                              timeout=120)
            r.raise_for_status()
            recs = r.json()
            elapsed = round(time.time() - start, 2)
        except Exception as e:
            print(f"  ✗ Recommendation failed: {e}")
            continue

        count = len(recs)
        if count == 0:
            print(f"  ⚠ No recommendations returned")
            continue

        scores = [rec.get("finalScore", 0) for rec in recs]
        skill_scores = [rec.get("skillMatchScore", 0) for rec in recs]
        avg_score = round(statistics.mean(scores) * 100, 1)
        avg_skill = round(statistics.mean(skill_scores) * 100, 1)
        has_explanations = sum(1 for r in recs if r.get("explanation"))
        has_gaps = sum(1 for r in recs if r.get("skillGaps"))

        print(f"  ✓ {count} recommendations in {elapsed}s")
        print(f"    Avg final score:  {avg_score}%")
        print(f"    Avg skill match:  {avg_skill}%")
        print(f"    With explanation: {has_explanations}/{count}")
        print(f"    With skill gaps:  {has_gaps}/{count}")
        print(f"    Top project: {recs[0]['project']['title']} ({round(scores[0]*100)}%)")

        results.append({
            "profile": profile_data["name"],
            "count": count,
            "avg_score": avg_score,
            "avg_skill": avg_skill,
            "response_time_s": elapsed,
        })

    print("\n" + "=" * 60)
    print("EVALUATION SUMMARY")
    print("=" * 60)
    if results:
        for r in results:
            print(f"  {r['profile']:30s} | {r['count']:2d} recs | "
                  f"Avg:{r['avg_score']:5.1f}% | Skill:{r['avg_skill']:5.1f}% | "
                  f"Time:{r['response_time_s']}s")
    else:
        print("  No results to report.")
    print("=" * 60)

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--base-url", default="http://localhost:8080/api")
    args = parser.parse_args()
    evaluate(args.base_url)

if __name__ == "__main__":
    main()
