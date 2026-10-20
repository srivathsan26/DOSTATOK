import React from 'react';
import { formatINR } from '../utils/formatters';

export function CashFlowBarChart({ income = 0, expenses = 0, savings = 0, periodLabel = 'all time' }) {
  const inc = Number(income) || 0;
  const exp = Number(expenses) || 0;
  const max = Math.max(inc, exp, 1);

  const incHeight = (inc / max) * 160;
  const expHeight = (exp / max) * 160;

  return (
    <div role="img" aria-label={`${periodLabel} recorded totals: income ${formatINR(inc)}, expenses ${formatINR(exp)}. Net ${formatINR(savings)}.`} style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <div style={{ display: 'flex', justifyContent: 'space-around', alignItems: 'flex-end', height: '190px', paddingBottom: '10px', borderBottom: '1px solid var(--color-border)' }}>
        {/* Income Bar */}
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', width: '100px' }}>
          <div style={{ fontSize: '11px', fontWeight: 600, color: 'var(--color-positive)', marginBottom: '6px' }}>
            {formatINR(inc)}
          </div>
          <div
            style={{
              width: '48px',
              height: `${Math.max(incHeight, 8)}px`,
              backgroundColor: 'var(--color-positive)',
              borderRadius: '6px 6px 0 0',
              transition: 'height 0.4s ease',
            }}
          />
          <div style={{ fontSize: '12px', fontWeight: 600, marginTop: '8px', color: 'var(--color-text-main)' }}>
            Income
          </div>
        </div>

        {/* Expenses Bar */}
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', width: '100px' }}>
          <div style={{ fontSize: '11px', fontWeight: 600, color: 'var(--color-negative)', marginBottom: '6px' }}>
            {formatINR(exp)}
          </div>
          <div
            style={{
              width: '48px',
              height: `${Math.max(expHeight, 8)}px`,
              backgroundColor: 'var(--color-negative)',
              borderRadius: '6px 6px 0 0',
              transition: 'height 0.4s ease',
            }}
          />
          <div style={{ fontSize: '12px', fontWeight: 600, marginTop: '8px', color: 'var(--color-text-main)' }}>
            Expenses
          </div>
        </div>
      </div>

      <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '16px', fontSize: '12px' }}>
        <span style={{ color: 'var(--color-text-muted)' }}>Net Cash Flow:</span>
        <span style={{ fontWeight: 700, color: savings >= 0 ? 'var(--color-positive)' : 'var(--color-negative)' }}>
          {savings >= 0 ? '+' : ''}{formatINR(savings)}
        </span>
      </div>
    </div>
  );
}

export function WealthDonutChart({ liquid = 0, investments = 0, physicalAssets = 0 }) {
  const bankBalance = Number(liquid) || 0;
  const l = Math.max(bankBalance, 0);
  const inv = Number(investments) || 0;
  const p = Number(physicalAssets) || 0;
  const total = l + inv + p;

  if (total <= 0) {
    return (
      <div style={{ textAlign: 'center', padding: '32px 0', color: 'var(--color-text-muted)', fontSize: '13px' }}>
        No asset valuation recorded yet.
      </div>
    );
  }

  const pLiquid = ((l / total) * 100).toFixed(1);
  const pInv = ((inv / total) * 100).toFixed(1);
  const pPhysical = ((p / total) * 100).toFixed(1);

  return (
    <div role="img" aria-label={`Current positive asset composition. Net bank balance ${formatINR(bankBalance)}, investments ${formatINR(inv)}, physical assets ${formatINR(p)}.`}>
      {/* Visual Distribution Bar */}
      <div style={{ display: 'flex', height: '14px', borderRadius: '7px', overflow: 'hidden', marginBottom: '20px' }}>
        <div style={{ width: `${pLiquid}%`, backgroundColor: 'var(--color-primary)' }} title={`Liquid: ${pLiquid}%`} />
        <div style={{ width: `${pInv}%`, backgroundColor: 'var(--color-accent-teal)' }} title={`Investments: ${pInv}%`} />
        <div style={{ width: `${pPhysical}%`, backgroundColor: 'var(--color-warning)' }} title={`Physical: ${pPhysical}%`} />
      </div>

      {/* Legend & Breakdown */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', fontSize: '13px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span aria-hidden="true" style={{ width: '10px', height: '10px', borderRadius: '2px', backgroundColor: 'var(--color-primary)' }} />
            <span>Liquid Cash & Bank Reserves</span>
          </div>
          <span style={{ fontWeight: 600 }}>{formatINR(bankBalance)} ({pLiquid}%)</span>
        </div>

        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span aria-hidden="true" style={{ width: '10px', height: '10px', borderRadius: '2px', backgroundColor: 'var(--color-accent-teal)' }} />
            <span>Investments & Mutual Funds</span>
          </div>
          <span style={{ fontWeight: 600 }}>{formatINR(inv)} ({pInv}%)</span>
        </div>

        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span aria-hidden="true" style={{ width: '10px', height: '10px', borderRadius: '2px', backgroundColor: 'var(--color-warning)' }} />
            <span>Real Estate & Physical Assets</span>
          </div>
          <span style={{ fontWeight: 600 }}>{formatINR(p)} ({pPhysical}%)</span>
        </div>
      </div>
      {bankBalance < 0 && <p className="chart-footnote">Negative bank balances are included in net worth and excluded from this asset mix.</p>}
    </div>
  );
}

export function ExpenseCategoryChart({ categories = [] }) {
  const visibleCategories = categories
    .filter((item) => Number(item.total) > 0)
    .slice(0, 5);
  const max = Math.max(...visibleCategories.map((item) => Number(item.total)), 0);

  if (!visibleCategories.length) {
    return <p className="chart-empty-state">Expense categories will appear here after you record expenses.</p>;
  }

  return (
    <div className="category-chart" aria-label="Top expense categories, all time">
      {visibleCategories.map((item) => {
        const total = Number(item.total) || 0;
        return (
          <div className="category-chart-row" key={item.category}>
            <div className="category-chart-heading">
              <span>{item.category}</span>
              <strong>{formatINR(total)}</strong>
            </div>
            <div
              className="category-chart-track"
              role="img"
              aria-label={`${item.category}: ${formatINR(total)}`}
            >
              <span style={{ width: `${(total / max) * 100}%` }} />
            </div>
          </div>
        );
      })}
      {categories.length > visibleCategories.length && (
        <p className="chart-footnote">Showing the five largest categories.</p>
      )}
    </div>
  );
}

export function CashFlowTrendChart({ incomeList = [], expenseList = [], now }) {
  const months = Array.from({ length: 6 }, (_, index) => {
    const date = new Date(now.getFullYear(), now.getMonth() - 5 + index, 1);
    const key = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
    return { key, label: date.toLocaleDateString(undefined, { month: 'short' }), income: 0, expenses: 0 };
  });
  const byMonth = new Map(months.map((month) => [month.key, month]));

  incomeList.forEach((item) => {
    const month = byMonth.get(String(item.incomeDate || '').slice(0, 7));
    if (month) month.income += Number(item.amount || 0);
  });
  expenseList.forEach((item) => {
    const month = byMonth.get(String(item.expenseDate || '').slice(0, 7));
    if (month) month.expenses += Number(item.amount || 0);
  });

  const max = Math.max(...months.flatMap(({ income, expenses }) => [income, expenses]), 1);
  const hasRecords = months.some(({ income, expenses }) => income > 0 || expenses > 0);
  if (!hasRecords) {
    return <p className="chart-empty-state">Record income or expenses to see monthly activity.</p>;
  }

  const description = months.map((month) => `${month.label}: income ${formatINR(month.income)}, expenses ${formatINR(month.expenses)}`).join('; ');
  return (
    <div className="trend-chart" role="img" aria-label={`Income and expense totals for the last six calendar months. ${description}`}>
      <div className="trend-legend" aria-hidden="true">
        <span><i className="trend-income" /> Income</span>
        <span><i className="trend-expense" /> Expenses</span>
      </div>
      <div className="trend-columns" aria-hidden="true">
        {months.map((month) => (
          <div className="trend-month" key={month.key} title={`${month.label}: Income ${formatINR(month.income)}; expenses ${formatINR(month.expenses)}`}>
            <div className="trend-pair">
              <span className="trend-income" style={{ height: `${month.income ? Math.max((month.income / max) * 112, 3) : 0}px` }} />
              <span className="trend-expense" style={{ height: `${month.expenses ? Math.max((month.expenses / max) * 112, 3) : 0}px` }} />
            </div>
            <span className="trend-month-label">{month.label}</span>
          </div>
        ))}
      </div>
    </div>
  );
}

