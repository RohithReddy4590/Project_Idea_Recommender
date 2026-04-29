import React, { useState } from 'react';
import ProfileForm from '../components/ProfileForm';
import { getRecommendations } from '../services/api';
import { useNavigate } from 'react-router-dom';

export default function Dashboard() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleProfileCreated = async (profile) => {
    setError('');
    setLoading(true);
    try {
      const sessionId = `session-${profile.id}-${Date.now()}`;
      const recommendations = await getRecommendations({
        studentId: profile.id,
        sessionId,
        maxResults: 10,
      });
      navigate('/recommendations', { state: { recommendations, profile, sessionId } });
    } catch (err) {
      setError('Profile created! But failed to fetch recommendations: ' + (err.response?.data?.message || err.message));
      setLoading(false);
    }
  };

  return (
    <div style={styles.page}>
      {/* Hero */}
      <div style={styles.hero}>
        <div style={styles.heroIcon}>🤖</div>
        <h1 style={styles.heroTitle}>Project Idea Recommender</h1>
        <p style={styles.heroSub}>
          An AI agent that analyzes your skills and recommends the perfect portfolio projects
          to land your dream job — with personalized explanations and learning paths.
        </p>
        <div style={styles.features}>
          {['🎯 Skill-based filtering', '🔍 Semantic AI matching', '🧠 LLM-powered ranking', '📊 Skill gap analysis'].map(f => (
            <span key={f} style={styles.featureChip}>{f}</span>
          ))}
        </div>
      </div>

      {/* Form */}
      {loading ? (
        <div style={styles.loadingCard}>
          <div style={styles.spinner} />
          <h3 style={styles.loadingTitle}>AI Agent is analyzing your profile...</h3>
          <p style={styles.loadingText}>Running profile analysis → semantic search → LLM ranking → generating explanations</p>
          <div style={styles.pipeline}>
            {['Profile Analysis', 'Rule Filtering', 'Semantic Search', 'LLM Ranking', 'Explanations'].map((step, i) => (
              <div key={step} style={styles.pipelineStep}>
                <div style={styles.pipelineDot} />
                <span style={styles.pipelineText}>{step}</span>
                {i < 4 && <span style={styles.pipelineArrow}>→</span>}
              </div>
            ))}
          </div>
        </div>
      ) : (
        <div style={styles.formWrap}>
          {error && <div style={styles.error}>{error}</div>}
          <ProfileForm onProfileCreated={handleProfileCreated} />
        </div>
      )}
    </div>
  );
}

const styles = {
  page: { minHeight: '100vh', background: '#0f172a', padding: '40px 20px', fontFamily: "'Inter', sans-serif" },
  hero: { textAlign: 'center', maxWidth: 680, margin: '0 auto 40px' },
  heroIcon: { fontSize: 56, marginBottom: 12 },
  heroTitle: { color: '#f1f5f9', fontSize: 36, fontWeight: 800, margin: '0 0 12px', background: 'linear-gradient(135deg, #818cf8, #c4b5fd)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' },
  heroSub: { color: '#94a3b8', fontSize: 16, lineHeight: 1.7, margin: '0 0 24px' },
  features: { display: 'flex', flexWrap: 'wrap', justifyContent: 'center', gap: 10 },
  featureChip: { background: '#1e293b', color: '#94a3b8', border: '1px solid #334155', borderRadius: 20, padding: '6px 16px', fontSize: 13 },
  formWrap: { maxWidth: 720, margin: '0 auto' },
  error: { background: '#7f1d1d', color: '#fca5a5', padding: '12px 18px', borderRadius: 10, marginBottom: 16, fontSize: 14 },
  loadingCard: { maxWidth: 600, margin: '0 auto', background: '#1e293b', borderRadius: 16, padding: 40, textAlign: 'center', border: '1px solid #334155' },
  spinner: { width: 48, height: 48, border: '4px solid #334155', borderTop: '4px solid #6366f1', borderRadius: '50%', margin: '0 auto 20px', animation: 'spin 1s linear infinite' },
  loadingTitle: { color: '#f1f5f9', fontSize: 20, fontWeight: 700, margin: '0 0 8px' },
  loadingText: { color: '#94a3b8', fontSize: 14, margin: '0 0 24px' },
  pipeline: { display: 'flex', justifyContent: 'center', flexWrap: 'wrap', gap: 4 },
  pipelineStep: { display: 'flex', alignItems: 'center', gap: 4 },
  pipelineDot: { width: 8, height: 8, background: '#6366f1', borderRadius: '50%' },
  pipelineText: { color: '#64748b', fontSize: 11 },
  pipelineArrow: { color: '#334155', fontSize: 12 },
};
