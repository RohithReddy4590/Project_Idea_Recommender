import React from 'react';
import { motion } from 'framer-motion';
import { Code2, BookOpen, Layers, ExternalLink } from 'lucide-react';

const ProjectCard = ({ recommendation }) => {
  const { project, score, reason, skillGap } = recommendation;

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      whileHover={{ y: -5 }}
      className="glass p-6 rounded-3xl group relative overflow-hidden transition-all duration-500"
    >
      <div className="absolute top-0 right-0 p-4">
        <div className="bg-primary-500/10 dark:bg-primary-500/20 text-primary-600 dark:text-primary-400 px-3 py-1 rounded-full text-sm font-bold border border-primary-500/20">
          {score}% Match
        </div>
      </div>

      <div className="space-y-4">
        <div className="flex items-center gap-2 text-primary-600 dark:text-primary-400">
          <Layers size={18} />
          <span className="text-xs font-bold uppercase tracking-wider">{project.domain}</span>
        </div>

        <h3 className="text-xl font-bold group-hover:text-primary-600 transition-colors">
          {project.title}
        </h3>

        <p className="text-slate-500 dark:text-slate-400 text-sm line-clamp-2">
          {project.description}
        </p>

        <div className="flex flex-wrap gap-2">
          {project.techStack.slice(0, 3).map((tech, i) => (
            <span key={i} className="px-2 py-1 bg-slate-100 dark:bg-slate-800 text-[10px] font-semibold rounded-md">
              {tech}
            </span>
          ))}
          {project.techStack.length > 3 && (
            <span className="px-2 py-1 bg-slate-100 dark:bg-slate-800 text-[10px] font-semibold rounded-md">
              +{project.techStack.length - 3} more
            </span>
          )}
        </div>

        <div className="p-4 bg-slate-50 dark:bg-slate-800/50 rounded-2xl border border-slate-100 dark:border-slate-800">
          <p className="text-xs italic text-slate-600 dark:text-slate-300">
            "{reason}"
          </p>
        </div>

        {skillGap.length > 0 && (
          <div className="space-y-2">
            <span className="text-xs font-bold text-red-500 flex items-center gap-1">
              <Code2 size={14} /> Skill Gaps
            </span>
            <div className="flex flex-wrap gap-1">
              {skillGap.map((skill, i) => (
                <span key={i} className="px-2 py-0.5 bg-red-500/10 text-red-600 dark:text-red-400 text-[10px] font-bold rounded-full border border-red-500/20">
                  {skill}
                </span>
              ))}
            </div>
          </div>
        )}

        <button className="w-full flex items-center justify-center gap-2 py-2 rounded-xl border border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-800 transition-all text-sm font-medium">
          View Details <ExternalLink size={14} />
        </button>
      </div>
    </motion.div>
  );
};

export default ProjectCard;
