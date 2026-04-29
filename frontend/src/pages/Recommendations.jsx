import React, { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import ProjectCard from '../components/ProjectCard';
import AgentChat from '../components/AgentChat';

export default function Recommendations() {
  const location = useLocation();
  const navigate = useNavigate();
  const { recommendations = [], profile, sessionId } = location.state || {};

  const [activeTab, setActiveTab] = useState('projects');
  const [filter, setFilter] = useState('ALL');

  if (!profile) {
    return (
      <div style={styles.page}>
        <div style={styles.empty}>
          <p style={{ color: '#94a3b8' }}>No recommendations found. Please create a profile first.</p>
          <button style={styles.backBtn} onClick={() => navigate('/')}>← Back to Dashboard</button>
        </div>
      </div>
    );
  }

  const DIFFICULTY_FILTERS = ['ALL', 'BEGINNER', 'INTERMEDIATE', 'ADVANCED'];
  const filtered = filter === 'ALL'
    ? recommendations
    : recommendations.filter(r => r.project?.difficulty === filter);

  const avgScore = recommendations.length
    ? Math.round(recommendations.reduce((s, r) => s + r.finalScore, 0) / recommendations.length * 100)
    : 0;

  return (
    <div style={styles.page}>
      {/* Top bar */}
      <div style={styles.topBar}>
        <button style={styles.backBtn} onClick={() => navigate('/')}>← New Profile</button>
        <div style={styles.profileBadge}>
          <span style={styles.profileAvatar}>{profile.name?.[0]?.toUpperCase()}</span>
          <span style={styles.profileName}>{profile.name}</span>
        </div>
      </div>

      {/* Summary stats */}
      <div style={styles.statsRow}>
        {[
          { label: 'Projects Found', value: recommendations.length, icon: '📦' },
          { label: 'Avg Match Score', value: `${avgScore}%`, icon: '🎯' },
          { label: 'Experience Level', value: profile.experienceLevel, icon: '⭐' },
          { label: 'Session Active', value: 'Yes', icon: '🤖' },
        ].map(stat => (
          <div key={stat.label} style={styles.statCard}>
            <span style={styles.statIcon}>{stat.icon}</span>
            <div>
              <div style={styles.statValue}>{stat.value}</div>
              <div style={styles.statLabel}>{stat.label}</div>
            </div>
          </div>
        ))}
      </div>

      {/* Tabs */}
      <div style={styles.tabs}>
        {['projects', 'chat'].map(tab => (
          <button key={tab} style={activeTab === tab ? styles.tabActive : styles.tab}
            onClick={() => setActiveTab(tab)}>
            {tab === 'projects' ? `📦 Projects (${recommendations.length})` : '💬 AI Chat'}
          </button>
        ))}
      </div>

      {activeTab === 'projects' && (
        <div>
          {/* Difficulty filter */}
          <div style={styles.filterRow}>
            <span style={styles.filterLabel}>Filter by difficulty:</span>
            {DIFFICULTY_FILTERS.map(d => (
              <button key={d} style={filter === d ? styles.filterActive : styles.filterBtn}
                onClick={() => setFilter(d)}>
                {d}
              </button>
            ))}
          </div>

          {filtered.length === 0 ? (
            <div style={styles.empty}>
              <p style={{ color: '#94a3b8' }}>No projects match this filter.</p>
            </div>
          ) : (
            filtered.map((rec, i) => (
              <ProjectCard key={rec.project?.id || i} rec={rec} rank={i + 1} />
            ))
          )}
        </div>
      )}

      {activeTab === 'chat' && (
        <div>
          <p style={styles.chatHint}>
            💡 Ask the AI agent anything about your recommended projects, how to get started, or which skills to learn next.
          </p>
          <AgentChat studentId={profile.id} sessionId={sessionId} />
        </div>
      )}
    </div>
  );
}

const styles = {
  page: { minHeight: '100vh', background: '#0f172a', padding: '24px 20px', fontFamily: "'Inter', sans-serif", maxWidth: 860, margin: '0 auto' },
  topBar: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 },
  backBtn: { background: '#1e293b', color: '#94a3b8', border: '1px solid #334155', borderRadius: 8, padding: '8px 16px', cursor: 'pointer', fontSize: 13 },
  profileBadge: { display: 'flex', alignItems: 'center', gap: 10 },
  profileAvatar: { background: '#6366f1', color: '#fff', borderRadius: '50%', width: 32, height: 32, display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 700, fontSize: 14 },
  profileName: { color: '#f1f5f9', fontWeight: 600, fontSize: 15 },
  statsRow: { display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 12, marginBottom: 24 },
  statCard: { background: '#1e293b', borderRadius: 12, padding: '14px 16px', border: '1px solid #334155', display: 'flex', alignItems: 'center', gap: 12 },
  statIcon: { fontSize: 22 },
  statValue: { color: '#f1f5f9', fontWeight: 700, fontSize: 18 },
  statLabel: { color: '#64748b', fontSize: 11 },
  tabs: { display: 'flex', gap: 4, marginBottom: 20, background: '#1e293b', padding: 4, borderRadius: 12 },
  tab: { flex: 1, background: 'none', border: 'none', color: '#64748b', padding: '10px 0', borderRadius: 8, cursor: 'pointer', fontWeight: 500, fontSize: 14 },
  tabActive: { flex: 1, background: '#0f172a', border: 'none', color: '#818cf8', padding: '10px 0', borderRadius: 8, cursor: 'pointer', fontWeight: 600, fontSize: 14 },
  filterRow: { display: 'flex', alignItems: 'center', gap: 8, marginBottom: 18, flexWrap: 'wrap' },
  filterLabel: { color: '#64748b', fontSize: 13 },
  filterBtn: { background: '#1e293b', border: '1px solid #334155', borderRadius: 20, color: '#94a3b8', fontSize: 12, padding: '4px 14px', cursor: 'pointer' },
  filterActive: { background: '#312e81', border: '1px solid #6366f1', borderRadius: 20, color: '#818cf8', fontSize: 12, padding: '4px 14px', cursor: 'pointer', fontWeight: 600 },
  empty: { textAlign: 'center', padding: 40 },
  chatHint: { color: '#64748b', fontSize: 13, marginBottom: 14, background: '#1e293b', borderRadius: 10, padding: '10px 16px', border: '1px solid #334155' },
};
