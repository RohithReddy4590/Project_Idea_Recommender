import React, { useState, useEffect } from 'react';
import { Moon, Sun, Sparkles, LayoutDashboard, Database, Target, BrainCircuit } from 'lucide-react';
import ProfileForm from './components/ProfileForm/ProfileForm';
import ProjectCard from './components/ProjectCard/ProjectCard';
import { recommendationService } from './services/api';

function App() {
  const [darkMode, setDarkMode] = useState(true);
  const [loading, setLoading] = useState(false);
  const [recommendations, setRecommendations] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (darkMode) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }, [darkMode]);

  const handleGenerate = async (profile) => {
    setLoading(true);
    setError(null);
    try {
      const data = await recommendationService.getRecommendations(profile);
      setRecommendations(data.recommendations);
      
      // Scroll to results
      setTimeout(() => {
        document.getElementById('results')?.scrollIntoView({ behavior: 'smooth' });
      }, 100);
    } catch (err) {
      setError('Failed to fetch recommendations. Please check your connection or try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950 text-slate-900 dark:text-slate-100 transition-colors duration-500 selection:bg-primary-500/30">
      {/* Background Decorative Elements */}
      <div className="fixed inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-[-10%] left-[-10%] w-[40%] h-[40%] bg-primary-500/10 rounded-full blur-[120px]" />
        <div className="absolute bottom-[-10%] right-[-10%] w-[40%] h-[40%] bg-indigo-500/10 rounded-full blur-[120px]" />
      </div>

      <nav className="sticky top-0 z-50 glass border-b border-slate-200 dark:border-slate-800">
        <div className="max-w-7xl mx-auto px-4 h-16 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="w-10 h-10 bg-primary-600 rounded-xl flex items-center justify-center text-white shadow-lg shadow-primary-600/20">
              <BrainCircuit size={24} />
            </div>
            <span className="text-xl font-black tracking-tighter">Idea<span className="text-primary-600">Engine</span></span>
          </div>
          
          <div className="flex items-center gap-6">
            <button 
              onClick={() => setDarkMode(!darkMode)}
              className="p-2.5 rounded-xl hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
            >
              {darkMode ? <Sun size={20} /> : <Moon size={20} />}
            </button>
            <button className="hidden md:flex btn-primary !py-2 !px-4 text-sm gap-2">
              <Sparkles size={16} /> Sign In
            </button>
          </div>
        </div>
      </nav>

      <main className="max-w-7xl mx-auto px-4 py-12 relative">
        <header className="text-center mb-16 space-y-4 animate-slide-up">
          <h1 className="text-5xl md:text-7xl font-black tracking-tight leading-tight">
            Level up your career with <br />
            <span className="gradient-text">AI Project Recommendations</span>
          </h1>
          <p className="text-lg text-slate-500 dark:text-slate-400 max-w-2xl mx-auto font-medium">
            Personalized portfolio ideas powered by hybrid LLM analysis and skill gap detection.
          </p>
        </header>

        <section className="mb-24">
          <ProfileForm onSubmit={handleGenerate} loading={loading} />
        </section>

        {error && (
          <div className="max-w-2xl mx-auto mb-12 p-4 bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 rounded-2xl text-center">
            {error}
          </div>
        )}

        {recommendations.length > 0 && (
          <section id="results" className="space-y-8 animate-fade-in">
            <div className="flex items-center justify-between">
              <div className="space-y-1">
                <h2 className="text-3xl font-black">Top Recommendations</h2>
                <p className="text-slate-500 dark:text-slate-400">Ranked based on your unique profile and goals</p>
              </div>
              <div className="flex items-center gap-2 px-4 py-2 glass rounded-xl text-sm font-medium">
                <LayoutDashboard size={16} /> {recommendations.length} results
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
              {recommendations.map((rec, index) => (
                <ProjectCard key={index} recommendation={rec} />
              ))}
            </div>
          </section>
        )}

        {recommendations.length === 0 && !loading && !error && (
          <div className="text-center py-24 space-y-6 opacity-40">
            <Database size={64} className="mx-auto text-slate-300" />
            <p className="text-xl font-medium">Your future projects will appear here</p>
          </div>
        )}
      </main>

      <footer className="py-12 border-t border-slate-200 dark:border-slate-800 text-center text-slate-400 text-sm">
        <p>&copy; 2026 IdeaEngine AI. All rights reserved.</p>
      </footer>
    </div>
  );
}

export default App;
