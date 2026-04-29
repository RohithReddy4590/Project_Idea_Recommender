import React, { useState } from 'react';
import { createProfile } from '../services/api';

const SKILL_SUGGESTIONS = [
  'Java','Spring Boot','Python','JavaScript','TypeScript','React','MySQL',
  'Docker','Kubernetes','REST API','SQL','Redis','AWS','Machine Learning',
  'OpenAI API','Git','CSS','Node.js','PostgreSQL','MongoDB'
];

export default function ProfileForm({ onProfileCreated }) {
  const [form, setForm] = useState({
    name: '', email: '', careerGoal: '', domainInterests: '',
    experienceLevel: 'INTERMEDIATE', skills: []
  });
  const [skillInput, setSkillInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e) =>
    setForm((f) => ({ ...f, [e.target.name]: e.target.value }));

  const addSkill = (name) => {
    if (!name.trim()) return;
    if (form.skills.some((s) => s.skillName.toLowerCase() === name.toLowerCase())) return;
    setForm((f) => ({
      ...f,
      skills: [...f.skills, { skillName: name.trim(), proficiencyLevel: 'INTERMEDIATE', yearsExperience: 1 }]
    }));
    setSkillInput('');
  };

  const removeSkill = (name) =>
    setForm((f) => ({ ...f, skills: f.skills.filter((s) => s.skillName !== name) }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const profile = await createProfile(form);
      onProfileCreated(profile);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create profile. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.card}>
      <h2 style={styles.title}>🎓 Create Your Profile</h2>
      <p style={styles.subtitle}>Tell us about yourself so the AI agent can recommend the perfect projects.</p>

      {error && <div style={styles.error}>{error}</div>}

      <form onSubmit={handleSubmit}>
        <div style={styles.grid}>
          <div style={styles.field}>
            <label style={styles.label}>Full Name *</label>
            <input style={styles.input} name="name" value={form.name}
              onChange={handleChange} placeholder="e.g. Alice Johnson" required />
          </div>
          <div style={styles.field}>
            <label style={styles.label}>Email *</label>
            <input style={styles.input} name="email" type="email" value={form.email}
              onChange={handleChange} placeholder="alice@example.com" required />
          </div>
        </div>

        <div style={styles.field}>
          <label style={styles.label}>Career Goal</label>
          <input style={styles.input} name="careerGoal" value={form.careerGoal}
            onChange={handleChange} placeholder="e.g. Backend Software Engineer at a product company" />
        </div>

        <div style={styles.grid}>
          <div style={styles.field}>
            <label style={styles.label}>Domain Interests</label>
            <input style={styles.input} name="domainInterests" value={form.domainInterests}
              onChange={handleChange} placeholder="e.g. Backend, AI/ML, DevOps" />
          </div>
          <div style={styles.field}>
            <label style={styles.label}>Experience Level</label>
            <select style={styles.input} name="experienceLevel" value={form.experienceLevel} onChange={handleChange}>
              <option value="BEGINNER">Beginner (0–1 year)</option>
              <option value="INTERMEDIATE">Intermediate (1–3 years)</option>
              <option value="ADVANCED">Advanced (3+ years)</option>
            </select>
          </div>
        </div>

        <div style={styles.field}>
          <label style={styles.label}>Your Skills</label>
          <div style={styles.skillInputRow}>
            <input style={{ ...styles.input, flex: 1 }} value={skillInput}
              onChange={(e) => setSkillInput(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && (e.preventDefault(), addSkill(skillInput))}
              placeholder="Type a skill and press Enter or click Add" />
            <button type="button" style={styles.addBtn} onClick={() => addSkill(skillInput)}>Add</button>
          </div>

          {/* Suggestions */}
          <div style={styles.suggestions}>
            {SKILL_SUGGESTIONS.filter(s =>
              !form.skills.some(sk => sk.skillName === s)
            ).map(s => (
              <button key={s} type="button" style={styles.suggestionChip} onClick={() => addSkill(s)}>{s}</button>
            ))}
          </div>

          {/* Selected skills */}
          <div style={styles.skillChips}>
            {form.skills.map((s) => (
              <span key={s.skillName} style={styles.skillChip}>
                {s.skillName}
                <button type="button" style={styles.removeBtn} onClick={() => removeSkill(s.skillName)}>✕</button>
              </span>
            ))}
          </div>
        </div>

        <button type="submit" style={loading ? styles.btnDisabled : styles.btn} disabled={loading}>
          {loading ? '⏳ Creating Profile...' : '🚀 Create Profile & Get Recommendations'}
        </button>
      </form>
    </div>
  );
}

const styles = {
  card: { background: '#1e293b', borderRadius: 16, padding: 32, border: '1px solid #334155' },
  title: { color: '#f1f5f9', fontSize: 24, fontWeight: 700, margin: '0 0 8px' },
  subtitle: { color: '#94a3b8', marginBottom: 24, fontSize: 14 },
  error: { background: '#7f1d1d', color: '#fca5a5', padding: '10px 16px', borderRadius: 8, marginBottom: 16, fontSize: 14 },
  grid: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16, marginBottom: 16 },
  field: { marginBottom: 16 },
  label: { display: 'block', color: '#94a3b8', fontSize: 13, fontWeight: 500, marginBottom: 6 },
  input: { width: '100%', background: '#0f172a', border: '1px solid #334155', borderRadius: 8, padding: '10px 14px', color: '#f1f5f9', fontSize: 14, outline: 'none', boxSizing: 'border-box' },
  skillInputRow: { display: 'flex', gap: 8 },
  addBtn: { background: '#6366f1', color: '#fff', border: 'none', borderRadius: 8, padding: '10px 18px', cursor: 'pointer', fontWeight: 600, fontSize: 13 },
  suggestions: { display: 'flex', flexWrap: 'wrap', gap: 6, marginTop: 10 },
  suggestionChip: { background: '#1e3a5f', color: '#60a5fa', border: '1px solid #1d4ed8', borderRadius: 20, padding: '4px 12px', fontSize: 12, cursor: 'pointer' },
  skillChips: { display: 'flex', flexWrap: 'wrap', gap: 8, marginTop: 12 },
  skillChip: { background: '#312e81', color: '#a5b4fc', borderRadius: 20, padding: '5px 14px', fontSize: 13, display: 'flex', alignItems: 'center', gap: 6 },
  removeBtn: { background: 'none', border: 'none', color: '#a5b4fc', cursor: 'pointer', padding: 0, fontSize: 12, lineHeight: 1 },
  btn: { width: '100%', background: 'linear-gradient(135deg, #6366f1, #8b5cf6)', color: '#fff', border: 'none', borderRadius: 10, padding: '14px 0', fontSize: 16, fontWeight: 700, cursor: 'pointer', marginTop: 8 },
  btnDisabled: { width: '100%', background: '#334155', color: '#64748b', border: 'none', borderRadius: 10, padding: '14px 0', fontSize: 16, fontWeight: 700, cursor: 'not-allowed', marginTop: 8 },
};
