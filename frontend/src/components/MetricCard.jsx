import React from 'react';

export default function MetricCard({ label, value, subtext, icon: Icon, color = 'emerald' }) {
  const colorMap = {
    emerald: { bg: 'var(--color-primary-light)', text: 'var(--color-primary)' },
    teal: { bg: 'var(--color-accent-teal-bg)', text: 'var(--color-accent-teal)' },
    positive: { bg: 'var(--color-positive-bg)', text: 'var(--color-positive)' },
    negative: { bg: 'var(--color-negative-bg)', text: 'var(--color-negative)' },
    warning: { bg: 'var(--color-warning-bg)', text: 'var(--color-warning)' },
  };

  const currentTheme = colorMap[color] || colorMap.emerald;

  return (
    <div className="metric-card">
      <div className="metric-card-top">
        <span className="metric-card-label">{label}</span>
        {Icon && (
          <div
            className="metric-card-icon-wrap"
            style={{ backgroundColor: currentTheme.bg, color: currentTheme.text }}
          >
            <Icon size={18} />
          </div>
        )}
      </div>
      <div className="metric-card-value">{value}</div>
      {subtext && <div className="metric-card-subtext">{subtext}</div>}
    </div>
  );
}

