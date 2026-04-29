import React, { useState, useRef, useEffect } from 'react';
import { sendChatMessage } from '../services/api';

export default function AgentChat({ studentId, sessionId }) {
  const [messages, setMessages] = useState([
    { role: 'assistant', text: "Hi! I'm your AI career advisor 🤖 Ask me anything about your recommended projects, skill gaps, or career direction!" }
  ]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const bottomRef = useRef(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSend = async () => {
    const text = input.trim();
    if (!text || loading) return;

    setMessages(prev => [...prev, { role: 'user', text }]);
    setInput('');
    setLoading(true);

    try {
      const data = await sendChatMessage(sessionId, studentId, text);
      setMessages(prev => [...prev, { role: 'assistant', text: data.response }]);
    } catch (err) {
      setMessages(prev => [...prev, {
        role: 'assistant',
        text: '⚠️ Sorry, I had trouble connecting. Please check if the server is running and try again.'
      }]);
    } finally {
      setLoading(false);
    }
  };

  const QUICK_PROMPTS = [
    'Which project should I start first?',
    'What skills should I focus on learning?',
    'Explain the hardest project in simple terms',
    'How do these projects help my career?',
  ];

  return (
    <div style={styles.container}>
      <div style={styles.chatHeader}>
        <div style={styles.statusDot} />
        <span style={styles.chatTitle}>AI Career Advisor</span>
        <span style={styles.sessionTag}>Session active</span>
      </div>

      {/* Messages */}
      <div style={styles.messages}>
        {messages.map((msg, i) => (
          <div key={i} style={msg.role === 'user' ? styles.userBubble : styles.aiBubble}>
            {msg.role === 'assistant' && <span style={styles.aiAvatar}>🤖</span>}
            <p style={msg.role === 'user' ? styles.userText : styles.aiText}>{msg.text}</p>
          </div>
        ))}
        {loading && (
          <div style={styles.aiBubble}>
            <span style={styles.aiAvatar}>🤖</span>
            <div style={styles.typing}>
              <span style={styles.dot} /><span style={styles.dot} /><span style={styles.dot} />
            </div>
          </div>
        )}
        <div ref={bottomRef} />
      </div>

      {/* Quick prompts */}
      <div style={styles.quickPrompts}>
        {QUICK_PROMPTS.map(q => (
          <button key={q} style={styles.quickBtn}
            onClick={() => { setInput(q); }}>
            {q}
          </button>
        ))}
      </div>

      {/* Input */}
      <div style={styles.inputRow}>
        <input style={styles.input} value={input}
          onChange={e => setInput(e.target.value)}
          onKeyDown={e => e.key === 'Enter' && handleSend()}
          placeholder="Ask about projects, skills, career advice..." />
        <button style={loading ? styles.sendBtnDisabled : styles.sendBtn}
          onClick={handleSend} disabled={loading}>
          {loading ? '⏳' : '➤'}
        </button>
      </div>
    </div>
  );
}

const styles = {
  container: { background: '#1e293b', borderRadius: 16, border: '1px solid #334155', overflow: 'hidden', display: 'flex', flexDirection: 'column', height: 520 },
  chatHeader: { background: '#0f172a', padding: '12px 20px', display: 'flex', alignItems: 'center', gap: 10, borderBottom: '1px solid #334155' },
  statusDot: { width: 8, height: 8, borderRadius: '50%', background: '#4ade80', animation: 'pulse 2s infinite' },
  chatTitle: { color: '#f1f5f9', fontWeight: 600, fontSize: 15, flex: 1 },
  sessionTag: { color: '#64748b', fontSize: 11, background: '#1e293b', padding: '2px 8px', borderRadius: 10 },
  messages: { flex: 1, overflowY: 'auto', padding: 16, display: 'flex', flexDirection: 'column', gap: 12 },
  aiBubble: { display: 'flex', gap: 10, alignItems: 'flex-start' },
  userBubble: { display: 'flex', justifyContent: 'flex-end' },
  aiAvatar: { fontSize: 20, flexShrink: 0 },
  aiText: { background: '#0f172a', color: '#cbd5e1', borderRadius: '0 12px 12px 12px', padding: '10px 14px', margin: 0, fontSize: 14, lineHeight: 1.6, maxWidth: '80%' },
  userText: { background: '#312e81', color: '#e0e7ff', borderRadius: '12px 0 12px 12px', padding: '10px 14px', margin: 0, fontSize: 14, lineHeight: 1.6, maxWidth: '80%' },
  typing: { display: 'flex', gap: 4, padding: '12px 14px', background: '#0f172a', borderRadius: '0 12px 12px 12px' },
  dot: { width: 6, height: 6, borderRadius: '50%', background: '#6366f1', display: 'inline-block' },
  quickPrompts: { padding: '0 12px 10px', display: 'flex', gap: 6, flexWrap: 'wrap' },
  quickBtn: { background: '#0f172a', border: '1px solid #334155', borderRadius: 20, color: '#94a3b8', fontSize: 11, padding: '4px 10px', cursor: 'pointer' },
  inputRow: { display: 'flex', gap: 8, padding: '12px 16px', borderTop: '1px solid #334155', background: '#0f172a' },
  input: { flex: 1, background: '#1e293b', border: '1px solid #334155', borderRadius: 10, padding: '10px 14px', color: '#f1f5f9', fontSize: 14, outline: 'none' },
  sendBtn: { background: '#6366f1', color: '#fff', border: 'none', borderRadius: 10, padding: '10px 18px', cursor: 'pointer', fontWeight: 700, fontSize: 18 },
  sendBtnDisabled: { background: '#334155', color: '#64748b', border: 'none', borderRadius: 10, padding: '10px 18px', cursor: 'not-allowed', fontSize: 18 },
};
