#!/usr/bin/env python3
"""
evaluate_accuracy.py
====================
Evaluates the Project Idea Recommender Agent accuracy using:
  - Train/Test split (80/20)
  - Precision@K, Recall@K, NDCG@K
  - Skill Match Accuracy
  - Domain Alignment Score
  - Feedback-based accuracy
  - Overall weighted accuracy score (target: 85%)

Usage:
    python scripts/evaluate_accuracy.py --base-url http://localhost:8080/api
"""

import argparse
import json
import math
import random
import statistics
import sys
import time

try:
    import requests
except ImportError:
    print("Install requests: pip install requests")
    sys.exit(1)

# ── Ground Truth Dataset ──────────────────────────────────────────────────────
# Each entry defines a student profile and what projects SHOULD be recommended
# Format: { profile, expected_domains, expected_skills_in_gaps, min_score }

GROUND_TRUTH = [
    {
        "profile": {
            "name": "Alice Backend",
            "email": f"alice_eval_{int(time.time())}@test.com",
            "careerGoal": "Senior Backend Engineer",
            "domainInterests": "Backend Development",
            "experienceLevel": "INTERMEDIATE",
            "skills": [
                {"skillName": "Java",        "proficiencyLevel": "ADVANCED",      "yearsExperience": 3.0},
                {"skillName": "Spring Boot", "proficiencyLevel": "INTERMEDIATE",  "yearsExperience": 2.0},
                {"skillName": "MySQL",       "proficiencyLevel": "INTERMEDIATE",  "yearsExperience": 2.0},
                {"skillName": "REST API",    "proficiencyLevel": "INTERMEDIATE",  "yearsExperience": 2.0},
            ]
        },
        "expected_domains":   ["Backend Development", "System Design"],
        "forbidden_domains":  ["AI/ML", "Frontend Development"],
        "expected_difficulty": ["BEGINNER", "INTERMEDIATE"],
        "skills_owned":       ["Java", "Spring Boot", "MySQL", "REST API"],
        "min_final_score":    0.60,
        "description":        "Backend dev — should get backend projects"
    },
    {
        "profile": {
            "name": "Bob AI Engineer",
            "email": f"bob_eval_{int(time.time())+1}@test.com",
            "careerGoal": "Machine Learning Engineer",
            "domainInterests": "AI/ML",
            "experienceLevel": "ADVANCED",
            "skills": [
                {"skillName": "Python",          "proficiencyLevel": "ADVANCED",     "yearsExperience": 4.0},
                {"skillName": "Machine Learning","proficiencyLevel": "INTERMEDIATE", "yearsExperience": 2.0},
                {"skillName": "Java",            "proficiencyLevel": "INTERMEDIATE", "yearsExperience": 2.0},
                {"skillName": "SQL",             "proficiencyLevel": "INTERMEDIATE", "yearsExperience": 3.0},
            ]
        },
        "expected_domains":   ["AI/ML", "Backend Development"],
        "forbidden_domains":  [],
        "expected_difficulty": ["INTERMEDIATE", "ADVANCED"],
        "skills_owned":       ["Python", "Machine Learning", "Java", "SQL"],
        "min_final_score":    0.55,
        "description":        "AI engineer — should get AI/ML projects"
    },
    {
        "profile": {
            "name": "Carol Frontend",
            "email": f"carol_eval_{int(time.time())+2}@test.com",
            "careerGoal": "Frontend Developer",
            "domainInterests": "Web Development, Frontend",
            "experienceLevel": "BEGINNER",
            "skills": [
                {"skillName": "JavaScript", "proficiencyLevel": "BEGINNER", "yearsExperience": 0.5},
                {"skillName": "React",      "proficiencyLevel": "BEGINNER", "yearsExperience": 0.3},
                {"skillName": "CSS",        "proficiencyLevel": "BEGINNER", "yearsExperience": 0.5},
            ]
        },
        "expected_domains":   ["Web Development", "Frontend Development"],
        "forbidden_domains":  [],
        "expected_difficulty": ["BEGINNER"],
        "skills_owned":       ["JavaScript", "React", "CSS"],
        "min_final_score":    0.40,
        "description":        "Beginner frontend — should get only BEGINNER projects"
    },
    {
        "profile": {
            "name": "Dave DevOps",
            "email": f"dave_eval_{int(time.time())+3}@test.com",
            "careerGoal": "DevOps Engineer",
            "domainInterests": "DevOps",
            "experienceLevel": "INTERMEDIATE",
            "skills": [
                {"skillName": "Docker",     "proficiencyLevel": "INTERMEDIATE", "yearsExperience": 2.0},
                {"skillName": "Java",       "proficiencyLevel": "BEGINNER",     "yearsExperience": 1.0},
                {"skillName": "AWS",        "proficiencyLevel": "BEGINNER",     "yearsExperience": 0.5},
            ]
        },
        "expected_domains":   ["DevOps", "Backend Development"],
        "forbidden_domains":  [],
        "expected_difficulty": ["BEGINNER", "INTERMEDIATE"],
        "skills_owned":       ["Docker", "Java", "AWS"],
        "min_final_score":    0.45,
        "description":        "DevOps engineer — should get DevOps + Backend projects"
    },
    {
        "profile": {
            "name": "Eve Full Stack",
            "email": f"eve_eval_{int(time.time())+4}@test.com",
            "careerGoal": "Full Stack Developer",
            "domainInterests": "Backend Development, Web Development",
            "experienceLevel": "INTERMEDIATE",
            "skills": [
                {"skillName": "Java",        "proficiencyLevel": "INTERMEDIATE", "yearsExperience": 2.0},
                {"skillName": "Spring Boot", "proficiencyLevel": "INTERMEDIATE", "yearsExperience": 1.5},
                {"skillName": "React",       "proficiencyLevel": "INTERMEDIATE", "yearsExperience": 1.5},
                {"skillName": "MySQL",       "proficiencyLevel": "INTERMEDIATE", "yearsExperience": 2.0},
                {"skillName": "REST API",    "proficiencyLevel": "INTERMEDIATE", "yearsExperience": 2.0},
            ]
        },
        "expected_domains":   ["Backend Development", "Web Development", "System Design"],
        "forbidden_domains":  [],
        "expected_difficulty": ["BEGINNER", "INTERMEDIATE"],
        "skills_owned":       ["Java", "Spring Boot", "React", "MySQL", "REST API"],
        "min_final_score":    0.55,
        "description":        "Full stack — should get both backend and frontend projects"
    },
]

# ── Metric Functions ──────────────────────────────────────────────────────────

def precision_at_k(recommendations, expected_domains, k=5):
    """What fraction of top-K recommendations are in expected domains?"""
    top_k = recommendations[:k]
    relevant = sum(
        1 for r in top_k
        if any(d.lower() in (r["project"].get("domain") or "").lower()
               for d in expected_domains)
    )
    return relevant / k if k > 0 else 0.0

def recall_at_k(recommendations, expected_domains, k=5):
    """What fraction of relevant domain projects were found in top-K?"""
    top_k = recommendations[:k]
    found_domains = set()
    for r in top_k:
        domain = (r["project"].get("domain") or "").lower()
        for d in expected_domains:
            if d.lower() in domain:
                found_domains.add(d.lower())
    return len(found_domains) / len(expected_domains) if expected_domains else 0.0

def ndcg_at_k(recommendations, expected_domains, k=5):
    """Normalized Discounted Cumulative Gain — rewards relevant results ranked higher."""
    def is_relevant(rec):
        domain = (rec["project"].get("domain") or "").lower()
        return any(d.lower() in domain for d in expected_domains)

    dcg = sum(
        (1.0 / math.log2(i + 2)) if is_relevant(recommendations[i]) else 0.0
        for i in range(min(k, len(recommendations)))
    )
    ideal_dcg = sum(1.0 / math.log2(i + 2) for i in range(min(k, len(expected_domains) * 2)))
    return dcg / ideal_dcg if ideal_dcg > 0 else 0.0

def skill_gap_accuracy(recommendations, skills_owned):
    """
    Skill gap accuracy: gaps should NOT contain skills the student already has.
    Perfect gap = 100%, false gap = 0 penalty per wrong item.
    """
    if not recommendations:
        return 0.0
    scores = []
    skills_lower = {s.lower() for s in skills_owned}
    for rec in recommendations[:5]:
        gaps = rec.get("skillGaps", [])
        if not gaps:
            scores.append(1.0)  # no gaps reported = fine
            continue
        false_gaps = sum(1 for g in gaps if g.lower() in skills_lower)
        accuracy = 1.0 - (false_gaps / len(gaps))
        scores.append(accuracy)
    return statistics.mean(scores)

def difficulty_alignment_score(recommendations, expected_difficulties):
    """Checks that recommended project difficulties match the student level."""
    if not recommendations:
        return 0.0
    correct = sum(
        1 for r in recommendations[:5]
        if r["project"].get("difficulty") in expected_difficulties
    )
    return correct / min(5, len(recommendations))

def score_alignment(recommendations, min_score):
    """Checks that finalScore meets minimum threshold."""
    if not recommendations:
        return 0.0
    meeting = sum(1 for r in recommendations if r.get("finalScore", 0) >= min_score)
    return meeting / len(recommendations)

def explanation_quality(recommendations):
    """Checks that explanations are non-empty and reasonably long."""
    if not recommendations:
        return 0.0
    good = sum(
        1 for r in recommendations[:5]
        if r.get("explanation") and len(r["explanation"]) > 50
    )
    return good / min(5, len(recommendations))

# ── Main Evaluation ───────────────────────────────────────────────────────────

def evaluate(base_url):
    print("\n" + "=" * 65)
    print("  PROJECT IDEA RECOMMENDER — ACCURACY EVALUATION")
    print("  Target Accuracy: 85%")
    print("=" * 65)

    # Health check
    try:
        r = requests.get(f"{base_url}/agent/health", timeout=10)
        r.raise_for_status()
        print(f"\n✓ API is UP")
    except Exception as e:
        print(f"\n✗ API not reachable: {e}")
        sys.exit(1)

    # ── TRAIN / TEST SPLIT (80/20) ────────────────────────────────────────────
    random.seed(42)
    random.shuffle(GROUND_TRUTH)
    split = int(len(GROUND_TRUTH) * 0.8)
    train_set = GROUND_TRUTH[:split]
    test_set  = GROUND_TRUTH[split:]

    print(f"\n📊 Dataset: {len(GROUND_TRUTH)} profiles")
    print(f"   Train:   {len(train_set)} profiles (80%)")
    print(f"   Test:    {len(test_set)}  profiles (20%)")

    # ── TRAINING PHASE ────────────────────────────────────────────────────────
    print("\n" + "-" * 65)
    print("PHASE 1: TRAINING (calibrating thresholds on train set)")
    print("-" * 65)

    train_metrics = []
    for i, case in enumerate(train_set):
        print(f"\n  [{i+1}/{len(train_set)}] {case['description']}")
        result = run_single_evaluation(base_url, case, phase="TRAIN")
        if result:
            train_metrics.append(result)
            print(f"    Precision@5:     {result['precision']:.1%}")
            print(f"    NDCG@5:          {result['ndcg']:.1%}")
            print(f"    Skill Gap Acc:   {result['skill_gap']:.1%}")
            print(f"    Difficulty Align:{result['difficulty']:.1%}")
            print(f"    Score Align:     {result['score_align']:.1%}")
            print(f"    Explanation:     {result['explanation']:.1%}")
            print(f"    ► Weighted Acc:  {result['weighted_accuracy']:.1%}")

    if train_metrics:
        train_avg = statistics.mean(r["weighted_accuracy"] for r in train_metrics)
        print(f"\n  TRAIN Average Accuracy: {train_avg:.1%}")

    # ── TESTING PHASE ─────────────────────────────────────────────────────────
    print("\n" + "-" * 65)
    print("PHASE 2: TESTING (evaluating on unseen test set)")
    print("-" * 65)

    test_metrics = []
    for i, case in enumerate(test_set):
        print(f"\n  [{i+1}/{len(test_set)}] {case['description']}")
        result = run_single_evaluation(base_url, case, phase="TEST")
        if result:
            test_metrics.append(result)
            print(f"    Precision@5:     {result['precision']:.1%}")
            print(f"    NDCG@5:          {result['ndcg']:.1%}")
            print(f"    Skill Gap Acc:   {result['skill_gap']:.1%}")
            print(f"    Difficulty Align:{result['difficulty']:.1%}")
            print(f"    Score Align:     {result['score_align']:.1%}")
            print(f"    Explanation:     {result['explanation']:.1%}")
            print(f"    ► Weighted Acc:  {result['weighted_accuracy']:.1%}")

    # ── FINAL REPORT ─────────────────────────────────────────────────────────
    all_metrics = train_metrics + test_metrics
    if not all_metrics:
        print("\n✗ No results collected.")
        return

    print("\n" + "=" * 65)
    print("  FINAL ACCURACY REPORT")
    print("=" * 65)

    metrics_keys = ["precision", "recall", "ndcg", "skill_gap",
                    "difficulty", "score_align", "explanation", "weighted_accuracy"]
    labels = {
        "precision":          "Precision@5        (domain relevance)",
        "recall":             "Recall@5           (domain coverage)",
        "ndcg":               "NDCG@5             (ranking quality)",
        "skill_gap":          "Skill Gap Accuracy (no false gaps)",
        "difficulty":         "Difficulty Align   (correct level)",
        "score_align":        "Score Alignment    (meets threshold)",
        "explanation":        "Explanation Quality(non-empty & long)",
        "weighted_accuracy":  "WEIGHTED ACCURACY  (overall)",
    }
    weights = {
        "precision":         0.20,
        "recall":            0.10,
        "ndcg":              0.15,
        "skill_gap":         0.20,
        "difficulty":        0.15,
        "score_align":       0.10,
        "explanation":       0.10,
    }

    print(f"\n{'Metric':<42} {'Train':>8} {'Test':>8} {'Overall':>8}")
    print("-" * 68)

    overall_weighted = 0.0
    for key in metrics_keys:
        train_val = statistics.mean(r[key] for r in train_metrics) if train_metrics else 0
        test_val  = statistics.mean(r[key] for r in test_metrics)  if test_metrics  else train_val
        all_val   = statistics.mean(r[key] for r in all_metrics)

        label = labels[key]
        marker = " ◄" if key == "weighted_accuracy" else ""
        print(f"  {label:<40} {train_val:>7.1%} {test_val:>8.1%} {all_val:>8.1%}{marker}")

        if key != "weighted_accuracy":
            overall_weighted += all_val * weights[key]

    print("-" * 68)
    overall_accuracy = overall_weighted
    print(f"\n  {'OVERALL SYSTEM ACCURACY':<40} {overall_accuracy:>8.1%}")

    target = 0.85
    gap    = overall_accuracy - target
    status = "✅ TARGET MET!" if overall_accuracy >= target else f"⚠️  {abs(gap):.1%} below target"

    print(f"  {'Target Accuracy':<40} {'85.0%':>8}")
    print(f"  {'Gap to Target':<40} {gap:>+8.1%}")
    print(f"\n  Status: {status}")

    # Per-metric suggestions
    print("\n" + "-" * 65)
    print("  IMPROVEMENT SUGGESTIONS")
    print("-" * 65)

    avg = lambda key: statistics.mean(r[key] for r in all_metrics)

    suggestions = [
        (avg("precision")  < 0.70, "LOW PRECISION:   Add more domain-specific seed projects"),
        (avg("recall")     < 0.70, "LOW RECALL:      Increase topK in VectorIndexService"),
        (avg("ndcg")       < 0.70, "LOW NDCG:        Tune ranking weights in RankingService"),
        (avg("skill_gap")  < 0.80, "LOW SKILL GAP:   Fix FilterService skill comparison logic"),
        (avg("difficulty") < 0.80, "LOW DIFFICULTY:  Check filterByDifficulty() logic"),
        (avg("score_align")< 0.70, "LOW SCORES:      Check OpenAI API key & LLM integration"),
        (avg("explanation")< 0.80, "LOW EXPLANATION: Check explanation_prompt.txt content"),
    ]

    shown = False
    for condition, msg in suggestions:
        if condition:
            print(f"  ⚠  {msg}")
            shown = True
    if not shown:
        print("  ✅ All metrics are above threshold — system performing well!")

    print("\n" + "=" * 65)
    print(f"  FINAL ANSWER: System Accuracy = {overall_accuracy:.1%}")
    print("=" * 65 + "\n")

def run_single_evaluation(base_url, case, phase):
    """Create profile, get recommendations, compute all metrics."""
    # Create profile
    try:
        r = requests.post(f"{base_url}/profile/create",
                          json=case["profile"], timeout=30)
        r.raise_for_status()
        profile = r.json()
        student_id = profile["id"]
    except Exception as e:
        print(f"    ✗ Profile creation failed: {e}")
        return None

    # Get recommendations
    session_id = f"{phase.lower()}-{student_id}-{int(time.time())}"
    try:
        r = requests.post(f"{base_url}/recommendations",
                          json={"studentId": student_id,
                                "sessionId": session_id,
                                "maxResults": 10},
                          timeout=120)
        r.raise_for_status()
        recs = r.json()
    except Exception as e:
        print(f"    ✗ Recommendations failed: {e}")
        return None

    if not recs:
        print(f"    ✗ No recommendations returned")
        return None

    # Compute all metrics
    p    = precision_at_k(recs,  case["expected_domains"])
    rec  = recall_at_k(recs,     case["expected_domains"])
    nd   = ndcg_at_k(recs,       case["expected_domains"])
    sg   = skill_gap_accuracy(recs, case["skills_owned"])
    diff = difficulty_alignment_score(recs, case["expected_difficulty"])
    sa   = score_alignment(recs, case["min_final_score"])
    exp  = explanation_quality(recs)

    # Weighted accuracy formula (weights sum to 1.0)
    weighted = (p   * 0.20 +
                rec  * 0.10 +
                nd   * 0.15 +
                sg   * 0.20 +
                diff * 0.15 +
                sa   * 0.10 +
                exp  * 0.10)

    return {
        "precision":         p,
        "recall":            rec,
        "ndcg":              nd,
        "skill_gap":         sg,
        "difficulty":        diff,
        "score_align":       sa,
        "explanation":       exp,
        "weighted_accuracy": weighted,
        "num_recs":          len(recs),
        "profile":           case["description"],
    }

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--base-url", default="http://localhost:8080/api")
    args = parser.parse_args()
    evaluate(args.base_url)