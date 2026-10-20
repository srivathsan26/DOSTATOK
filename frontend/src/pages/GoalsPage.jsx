import React from 'react';
import { PlusCircle, Target, TrendingUp, Trash2, Calendar } from 'lucide-react';
import { formatINR, formatDate } from '../utils/formatters';
import LoadingSkeleton from '../components/LoadingSkeleton';

export default function GoalsPage({
  goals,
  loading,
  onOpenCreateGoal,
  onOpenContributeGoal,
  onDeleteGoal
}) {
  if (loading) {
    return <LoadingSkeleton lines={6} />;
  }

  const totalTarget = goals.reduce((acc, curr) => acc + Number(curr.targetAmount || 0), 0);
  const totalSaved = goals.reduce((acc, curr) => acc + Number(curr.currentAmount || 0), 0);
  const completedCount = goals.filter((g) => g.status === 'COMPLETED').length;

  return (
    <div>
      {/* Top Goals Metrics */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '20px', marginBottom: '28px' }}>
        <div className="card" style={{ padding: '20px', marginBottom: 0 }}>
          <div style={{ fontSize: '12px', color: 'var(--color-text-muted)', textTransform: 'uppercase' }}>Target Cumulative Savings</div>
          <div style={{ fontSize: '26px', fontWeight: 700, color: 'var(--color-text-main)', margin: '4px 0' }}>
            {formatINR(totalTarget)}
          </div>
          <div style={{ fontSize: '11px', color: 'var(--color-text-muted)' }}>Across {goals.length} defined life milestones</div>
        </div>

        <div className="card" style={{ padding: '20px', marginBottom: 0 }}>
          <div style={{ fontSize: '12px', color: 'var(--color-text-muted)', textTransform: 'uppercase' }}>Total Funds Accumulated</div>
          <div style={{ fontSize: '26px', fontWeight: 700, color: 'var(--color-primary)', margin: '4px 0' }}>
            {formatINR(totalSaved)}
          </div>
          <div style={{ fontSize: '11px', color: 'var(--color-text-muted)' }}>
            Overall progress: {totalTarget > 0 ? ((totalSaved / totalTarget) * 100).toFixed(1) : 0}%
          </div>
        </div>

        <div className="card" style={{ padding: '20px', marginBottom: 0 }}>
          <div style={{ fontSize: '12px', color: 'var(--color-text-muted)', textTransform: 'uppercase' }}>Milestones Achieved</div>
          <div style={{ fontSize: '26px', fontWeight: 700, color: 'var(--color-positive)', margin: '4px 0' }}>
            {completedCount} / {goals.length}
          </div>
          <div style={{ fontSize: '11px', color: 'var(--color-text-muted)' }}>Completed target plans</div>
        </div>
      </div>

      {/* Goal Cards */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h3 style={{ fontSize: '17px', fontWeight: 700 }}>Active Financial Milestones</h3>
        <button className="btn btn-primary btn-sm" onClick={onOpenCreateGoal}>
          <PlusCircle size={14} /> Create New Goal
        </button>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(min(100%, 340px), 1fr))', gap: '20px' }}>
        {goals.length === 0 ? (
          <div className="card" style={{ gridColumn: '1 / -1', padding: '48px', textAlign: 'center' }}>
            <Target size={36} style={{ color: 'var(--color-text-subtle)', marginBottom: '12px' }} />
            <h4>No financial goals established yet</h4>
            <p style={{ color: 'var(--color-text-muted)', fontSize: '13px', marginTop: '4px' }}>
              Create a savings target for an emergency reserve, vacation, or equipment purchase.
            </p>
          </div>
        ) : (
          goals.map((goal) => {
            const target = Number(goal.targetAmount) || 1;
            const current = Number(goal.currentAmount) || 0;
            const pct = Math.min(Math.round((current / target) * 100), 100);
            const remaining = Math.max(target - current, 0);
            const isCompleted = goal.status === 'COMPLETED' || current >= target;

            return (
              <div key={goal.id} className="card" style={{ marginBottom: 0 }}>
                <div className="card-header" style={{ justifyContent: 'space-between' }}>
                  <div>
                    <strong style={{ fontSize: '15px' }}>{goal.title}</strong>
                    <div style={{ fontSize: '12px', color: 'var(--color-text-muted)' }}>{goal.category}</div>
                  </div>
                  <span className={`badge ${isCompleted ? 'badge-positive' : 'badge-savings'}`}>
                    {isCompleted ? 'COMPLETED' : 'IN PROGRESS'}
                  </span>
                </div>

                <div className="card-body">
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                    <span style={{ fontSize: '13px', color: 'var(--color-text-muted)' }}>Saved: <strong>{formatINR(current)}</strong></span>
                    <span style={{ fontSize: '13px', fontWeight: 600 }}>Target: {formatINR(target)}</span>
                  </div>

                  <div className="progress-bar-bg">
                    <div
                      className="progress-bar-fill"
                      style={{
                        width: `${pct}%`,
                        backgroundColor: isCompleted ? 'var(--color-positive)' : 'var(--color-primary)'
                      }}
                    />
                  </div>

                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', color: 'var(--color-text-muted)', marginTop: '8px' }}>
                    <span>{pct}% complete</span>
                    <span>Remaining: <strong>{formatINR(remaining)}</strong></span>
                  </div>

                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '12px', color: 'var(--color-text-muted)', marginTop: '12px' }}>
                    <Calendar size={13} />
                    <span>Target Date: {formatDate(goal.targetDate)}</span>
                  </div>

                  <div style={{ display: 'flex', gap: '8px', marginTop: '18px' }}>
                    {!isCompleted && (
                      <button
                        className="btn btn-primary btn-sm"
                        style={{ flex: 1 }}
                        onClick={() => onOpenContributeGoal(goal)}
                      >
                        <TrendingUp size={14} /> Contribute Funds
                      </button>
                    )}
                    <button
                      className="btn btn-danger btn-sm"
                      onClick={() => onDeleteGoal(goal.id, goal.title)}
                      title="Delete Goal"
                        aria-label={`Delete goal ${goal.title}`}
                    >
                      <Trash2 size={14} />
                    </button>
                  </div>
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}

