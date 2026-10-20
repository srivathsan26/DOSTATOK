import React from 'react';

export default function LoadingSkeleton({ lines = 4 }) {
  return (
    <div style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '16px' }}>
      {Array.from({ length: lines }).map((_, i) => (
        <div
          key={i}
          style={{
            height: i === 0 ? '32px' : '20px',
            width: i === 0 ? '40%' : `${85 - i * 10}%`,
            backgroundColor: 'var(--color-surface-hover)',
            borderRadius: '4px',
            animation: 'pulse 1.5s cubic-bezier(0.4, 0, 0.6, 1) infinite',
          }}
        />
      ))}
      <style>{`
        @keyframes pulse {
          0%, 100% { opacity: 1; }
          50% { opacity: .4; }
        }
      `}</style>
    </div>
  );
}

