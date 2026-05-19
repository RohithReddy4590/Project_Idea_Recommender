import React, { useState } from 'react';
import { Sparkles, Brain, Target, User } from 'lucide-react';

const ProfileForm = ({ onSubmit, loading }) => {
  const [formData, setFormData] = useState({
    skills: '',
    interests: '',
    goal: '',
    experienceLevel: 'BEGINNER',
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    const skillsArray = formData.skills.split(',').map((s) => s.trim()).filter((s) => s !== '');
    const interestsArray = formData.interests.split(',').map((s) => s.trim()).filter((s) => s !== '');
    onSubmit({ ...formData, skills: skillsArray, interests: interestsArray });
  };

  return (
    <form onSubmit={handleSubmit} className="glass p-8 rounded-3xl space-y-6 max-w-2xl mx-auto animate-fade-in">
      <div className="flex items-center gap-3 mb-8">
        <div className="p-3 bg-primary-100 dark:bg-primary-900/30 rounded-2xl text-primary-600">
          <Brain size={24} />
        </div>
        <div>
          <h2 className="text-2xl font-bold">Build Your Profile</h2>
          <p className="text-slate-500 text-sm">Tell us about your skills and goals</p>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="space-y-2">
          <label className="text-sm font-medium flex items-center gap-2">
            <Sparkles size={16} /> Skills (comma separated)
          </label>
          <input
            type="text"
            name="skills"
            placeholder="Java, React, SQL..."
            value={formData.skills}
            onChange={handleChange}
            className="input-field"
            required
          />
        </div>

        <div className="space-y-2">
          <label className="text-sm font-medium flex items-center gap-2">
            <Target size={16} /> Interests
          </label>
          <input
            type="text"
            name="interests"
            placeholder="Backend, AI, Web..."
            value={formData.interests}
            onChange={handleChange}
            className="input-field"
            required
          />
        </div>
      </div>

      <div className="space-y-2">
        <label className="text-sm font-medium flex items-center gap-2">
          <Target size={16} /> Career Goal
        </label>
        <input
          type="text"
          name="goal"
          placeholder="e.g. Software Engineer at Google"
          value={formData.goal}
          onChange={handleChange}
          className="input-field"
          required
        />
      </div>

      <div className="space-y-2">
        <label className="text-sm font-medium flex items-center gap-2">
          <User size={16} /> Experience Level
        </label>
        <select
          name="experienceLevel"
          value={formData.experienceLevel}
          onChange={handleChange}
          className="input-field"
        >
          <option value="BEGINNER">Beginner</option>
          <option value="INTERMEDIATE">Intermediate</option>
          <option value="SENIOR">Senior</option>
        </select>
      </div>

      <button
        type="submit"
        disabled={loading}
        className="btn-primary w-full flex items-center justify-center gap-2 mt-4"
      >
        {loading ? (
          <span className="flex items-center gap-2">
            <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
            Analyzing...
          </span>
        ) : (
          <>
            <Sparkles size={20} />
            Generate Recommendations
          </>
        )}
      </button>
    </form>
  );
};

export default ProfileForm;
