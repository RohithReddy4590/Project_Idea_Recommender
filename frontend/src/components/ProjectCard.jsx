import React, { useState } from 'react';
import { submitFeedback } from '../services/api';
import SkillGapDisplay from './SkillGapDisplay';

const DIFFICULTY_COLORS = {
  BEGINNER: { bg: '#14532d', text: '#4ade80' },
  INTERMEDIATE: { bg: '#1e3a5f', text: '#60a5fa' },
  ADVANCED: { bg: '#4c1d95', text: '#c4b5fd' },
};

export default function ProjectCard({ rec, rank }) {
  const [showGap, setShowGap] = useState(false);
  const [feedback, setFeedback] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const { project, finalScore, skillMatchScore, explanation, skillGaps, learningPath, readinessScore } = rec;
  const diff = DIFFICULTY_COLORS[project.difficulty] || DIFFICULTY_COLORS.BEGINNER;
  const scorePercent = Math.round(finalScore * 100);
  const matchPercent = Math.round(skillMatchScore * 100);

  const handleFeedback = async (value) => {
    if (submitting || feedback) return;
    setSubmitting(true);
    try {
      // historyId would come from the backend; using projectId as placeholder
      await submitFeedback(project.id, value);
      setFeedback(value);
    } catch (e) { /* silent */ }
    finally { setSubmitting(false); }
  };

  return (
    <div style={styles.card}>
      {/* Header */}
      <div style={styles.header}>
        <div style={styles.rankBadge}>#{rank}</div>
        <div style={{ flex: 1 }}>
          <div style={styles.titleRow}>
            <h3 style={styles.title}>{project.title}</h3>
            <span style={{ ...styles.diffBadge, background: diff.bg, color: diff.text }}>
              {project.difficulty}
            </span>
          </div>
          <div style={styles.meta}>
            <span style={styles.domain}>📁 {project.domain}</span>
            {project.estimatedHours && (
              <span style={styles.hours}>⏱ ~{project.estimatedHours}h</span>
            )}
          </div>
        </div>
        {/* Score ring */}
        <div style={styles.scoreCircle}>
          <span style={styles.scoreNum}>{scorePercent}%</span>
          <span style={styles.scoreLabel}>match</span>
        </div>
      </div>

      {/* Description */}
      <p style={styles.desc}>{project.description}</p>

      {/* Tech stack */}
      {project.techStack && (
        <div style={styles.techRow}>
          {project.techStack.split(',').map(t => (
            <span key={t.trim()} style={styles.tech}>{t.trim()}</span>
          ))}
        </div>
      )}

      {/* Score bars */}
      <div style={styles.scoreBar}>
        <div style={styles.barLabel}>
          <span style={styles.barText}>Skill Match</span>
          <span style={styles.barValue}>{matchPercent}%</span>
        </div>
        <div style={styles.barBg}>
          <div style={{ ...styles.barFill, width: `${matchPercent}%`, background: '#6366f1' }} />
        </div>
      </div>

      {/* AI Explanation */}
      {explanation && (
        <div style={styles.explanation}>
          <span style={styles.aiLabel}>🤖 AI Insight</span>
          <p style={styles.explanationText}>{explanation}</p>
        </div>
      )}

      {/* Skill gaps toggle */}
      {skillGaps && skillGaps.length > 0 && (
        <button style={styles.gapToggle} onClick={() => setShowGap(!showGap)}>
          {showGap ? '▲ Hide' : '▼ Show'} Skill Gap ({skillGaps.length} skills to learn)
        </button>
      )}
      {showGap && (
        <SkillGapDisplay skillGaps={skillGaps} learningPath={learningPath} readinessScore={readinessScore} />
      )}

      {/* Feedback */}
      <div style={styles.feedbackRow}>
        <span style={styles.feedbackLabel}>Was this helpful?</span>
        {['LIKED', 'DISLIKED'].map(v => (
          <button key={v}
            style={feedback === v ? styles.feedbackActive : styles.feedbackBtn}
            onClick={() => handleFeedback(v)} disabled={!!feedback}>
            {v === 'LIKED' ? '👍' : '👎'}
          </button>
        ))}
        {feedback && <span style={styles.feedbackThanks}>Thanks for the feedback!</span>}
      </div>
    </div>
  );
}

const styles = {
  card: { background: '#1e293b', borderRadius: 16, padding: 24, border: '1px solid #334155', marginBottom: 20, transition: 'border-color 0.2s' },
  header: { display: 'flex', alignItems: 'flex-start', gap: 16, marginBottom: 14 },
  rankBadge: { background: '#312e81', color: '#a5b4fc', borderRadius: 10, width: 36, height: 36, display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 700, fontSize: 14, flexShrink: 0 },
  titleRow: { display: 'flex', alignItems: 'center', gap: 10, flexWrap: 'wrap' },
  title: { color: '#f1f5f9', margin: 0, fontSize: 18, fontWeight: 700 },
  diffBadge: { borderRadius: 20, padding: '3px 12px', fontSize: 12, fontWeight: 600 },
  meta: { display: 'flex', gap: 16, marginTop: 4 },
  domain: { color: '#94a3b8', fontSize: 13 },
  hours: { color: '#94a3b8', fontSize: 13 },
  scoreCircle: { background: '#0f172a', border: '2px solid #6366f1', borderRadius: '50%', width: 56, height: 56, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', flexShrink: 0 },
  scoreNum: { color: '#818cf8', fontWeight: 700, fontSize: 14, lineHeight: 1 },
  scoreLabel: { color: '#64748b', fontSize: 10, lineHeight: 1 },
  desc: { color: '#94a3b8', fontSize: 14, lineHeight: 1.6, marginBottom: 14 },
  techRow: { display: 'flex', flexWrap: 'wrap', gap: 6, marginBottom: 16 },
  tech: { background: '#0f172a', color: '#64748b', border: '1px solid #334155', borderRadius: 6, padding: '3px 10px', fontSize: 12 },
  scoreBar: { marginBottom: 16 },
  barLabel: { display: 'flex', justifyContent: 'space-between', marginBottom: 4 },
  barText: { color: '#94a3b8', fontSize: 12 },
  barValue: { color: '#f1f5f9', fontSize: 12, fontWeight: 600 },
  barBg: { background: '#0f172a', borderRadius: 4, height: 6, overflow: 'hidden' },
  barFill: { height: '100%', borderRadius: 4, transition: 'width 0.8s ease' },
  explanation: { background: '#0f172a', borderRadius: 10, padding: '12px 16px', marginBottom: 14, border: '1px solid #1d4ed8' },
  aiLabel: { color: '#60a5fa', fontSize: 12, fontWeight: 600 },
  explanationText: { color: '#cbd5e1', fontSize: 13, lineHeight: 1.6, margin: '6px 0 0' },
  gapToggle: { background: 'none', border: '1px solid #334155', borderRadius: 8, color: '#94a3b8', fontSize: 13, cursor: 'pointer', padding: '7px 14px', marginBottom: 10, width: '100%' },
  feedbackRow: { display: 'flex', alignItems: 'center', gap: 10, marginTop: 14, borderTop: '1px solid #334155', paddingTop: 14 },
  feedbackLabel: { color: '#64748b', fontSize: 13 },
  feedbackBtn: { background: '#0f172a', border: '1px solid #334155', borderRadius: 8, padding: '5px 12px', cursor: 'pointer', fontSize: 16 },
  feedbackActive: { background: '#312e81', border: '1px solid #6366f1', borderRadius: 8, padding: '5px 12px', cursor: 'default', fontSize: 16 },
  feedbackThanks: { color: '#4ade80', fontSize: 12 },
};
