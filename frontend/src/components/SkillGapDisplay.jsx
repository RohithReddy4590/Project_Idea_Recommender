import React from 'react';

export default function SkillGapDisplay({ skillGaps, learningPath, readinessScore }) {
  const readinessPercent = Math.round((readinessScore || 0) * 100);

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <span style={styles.title}>📊 Skill Gap Analysis</span>
        <div style={styles.readiness}>
          <span style={styles.readinessLabel}>Readiness</span>
          <div style={styles.readinessBar}>
            <div style={{ ...styles.readinessFill, width: `${readinessPercent}%` }} />
          </div>
          <span style={styles.readinessPercent}>{readinessPercent}%</span>
        </div>
      </div>

      {skillGaps && skillGaps.length > 0 ? (
        <>
          <div style={styles.section}>
            <p style={styles.sectionTitle}>🔴 Missing Skills</p>
            <div style={styles.chips}>
              {skillGaps.map(skill => (
                <span key={skill} style={styles.gapChip}>{skill}</span>
              ))}
            </div>
          </div>

          {learningPath && learningPath.length > 0 && (
            <div style={styles.section}>
              <p style={styles.sectionTitle}>📚 Suggested Learning Path</p>
              <ol style={styles.pathList}>
                {learningPath.map((step, i) => (
                  <li key={step} style={styles.pathItem}>
                    <span style={styles.pathNum}>{i + 1}</span>
                    <span style={styles.pathText}>{step}</span>
                  </li>
                ))}
              </ol>
            </div>
          )}
        </>
      ) : (
        <div style={styles.ready}>
          <span>✅ You have all the required skills! You can start this project today.</span>
        </div>
      )}
    </div>
  );
}

const styles = {
  container: { background: '#0f172a', border: '1px solid #1e40af', borderRadius: 12, padding: 16, marginBottom: 14 },
  header: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 14 },
  title: { color: '#60a5fa', fontWeight: 600, fontSize: 14 },
  readiness: { display: 'flex', alignItems: 'center', gap: 8 },
  readinessLabel: { color: '#64748b', fontSize: 12 },
  readinessBar: { width: 80, background: '#1e293b', borderRadius: 4, height: 6 },
  readinessFill: { height: '100%', background: 'linear-gradient(90deg, #f59e0b, #10b981)', borderRadius: 4 },
  readinessPercent: { color: '#10b981', fontSize: 12, fontWeight: 600 },
  section: { marginBottom: 12 },
  sectionTitle: { color: '#94a3b8', fontSize: 13, fontWeight: 600, margin: '0 0 8px' },
  chips: { display: 'flex', flexWrap: 'wrap', gap: 6 },
  gapChip: { background: '#7f1d1d', color: '#fca5a5', borderRadius: 20, padding: '4px 12px', fontSize: 12 },
  pathList: { margin: 0, padding: 0, listStyle: 'none' },
  pathItem: { display: 'flex', alignItems: 'center', gap: 10, marginBottom: 8 },
  pathNum: { background: '#1e40af', color: '#93c5fd', borderRadius: '50%', width: 22, height: 22, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 12, fontWeight: 700, flexShrink: 0 },
  pathText: { color: '#cbd5e1', fontSize: 13 },
  ready: { color: '#4ade80', fontSize: 14, fontWeight: 500, textAlign: 'center', padding: '8px 0' },
};
