import React, { useEffect, useState } from 'react';
import {
  Wallet,
  ShieldCheck,
  TrendingDown,
  ArrowUpRight,
  ArrowDownLeft,
  PiggyBank,
  PlusCircle,
  MinusCircle,
  Landmark
} from 'lucide-react';
import MetricCard from '../components/MetricCard';
import { CashFlowBarChart, CashFlowTrendChart, WealthDonutChart, ExpenseCategoryChart } from '../components/Charts';
import { formatINR, formatDateTime } from '../utils/formatters';
import LoadingSkeleton from '../components/LoadingSkeleton';

function WelcomeSection({ name, now, greeting }) {
  const salutation = name ? greeting : 'Welcome back';
  return (
    <section className="welcome-block" aria-labelledby="welcome-title">
      <div>
        <p className="eyebrow">PERSONAL FINANCE OVERVIEW</p>
        <h1 id="welcome-title">{salutation}{name ? `, ${name.split(/\s+/)[0]}` : ''}</h1>
        <p>Here’s your financial overview for today.</p>
      </div>
      <div className="welcome-date">
        <span className="welcome-date-label">TODAY</span>
        <time dateTime={now.toISOString()}>{now.toLocaleDateString(undefined, { weekday: 'long', day: 'numeric', month: 'long' })}</time>
      </div>
    </section>
  );
}

export default function DashboardPage({
  profile,
  summary,
  loadError,
  onRetry,
  hasAccounts = false,
  incomeList = [],
  expenseList = [],
  goals = [],
  loading,
  onOpenDeposit,
  onOpenWithdraw,
  onOpenAddIncome,
  onOpenAddExpense,
  onNavigate
}) {
  const [now, setNow] = useState(() => new Date());
  const [period, setPeriod] = useState('month');
  useEffect(() => {
    const timer = window.setInterval(() => setNow(new Date()), 60_000);
    return () => window.clearInterval(timer);
  }, []);

  const hour = now.getHours();
  const greeting = hour >= 5 && hour < 12
    ? 'Good morning'
    : hour >= 12 && hour < 17
      ? 'Good afternoon'
      : hour >= 17 && hour < 21
        ? 'Good evening'
        : 'Good night';

  const dateKey = (value) => String(value || '').slice(0, 10);
  const todayKey = dateKey(`${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`);
  const thirtyDaysAgo = new Date(now.getFullYear(), now.getMonth(), now.getDate() - 29);
  const thirtyDaysKey = dateKey(`${thirtyDaysAgo.getFullYear()}-${String(thirtyDaysAgo.getMonth() + 1).padStart(2, '0')}-${String(thirtyDaysAgo.getDate()).padStart(2, '0')}`);
  const inSelectedPeriod = (value) => {
    const key = dateKey(value);
    if (!key) return false;
    if (period === 'all') return true;
    if (period === 'month') return key.slice(0, 7) === todayKey.slice(0, 7);
    return key >= thirtyDaysKey && key <= todayKey;
  };
  const periodIncome = incomeList
    .filter((item) => inSelectedPeriod(item.incomeDate))
    .reduce((total, item) => total + Number(item.amount || 0), 0);
  const periodExpenses = expenseList
    .filter((item) => inSelectedPeriod(item.expenseDate))
    .reduce((total, item) => total + Number(item.amount || 0), 0);
  const periodSavings = periodIncome - periodExpenses;
  const activeGoals = goals.filter((goal) => goal.status === 'IN_PROGRESS').length;

  if (loading) {
    return <div className="dashboard-page"><WelcomeSection name={profile?.name} now={now} greeting={greeting} /><LoadingSkeleton lines={6} /></div>;
  }

  if (!summary) {
    return (
      <div className="dashboard-page">
      <WelcomeSection name={profile?.name} now={now} greeting={greeting} />
      <section className="card dashboard-error" role="alert">
        <h1>We couldn’t load your overview</h1>
        <p>{loadError || 'Your financial data is temporarily unavailable.'}</p>
        <button className="btn btn-primary" type="button" onClick={onRetry}>Try again</button>
      </section>
      </div>
    );
  }

  return (
    <div className="dashboard-page">
      <WelcomeSection name={profile?.name} now={now} greeting={greeting} />

      {/* Top Financial Position Metrics */}
      <div className="metrics-grid">
        <MetricCard
          label="Consolidated Net Worth"
          value={formatINR(summary.netWorth)}
          subtext="Total Assets − Total Liabilities"
          icon={ShieldCheck}
          color="emerald"
        />
        <MetricCard
          label="Total Gross Assets"
          value={formatINR(summary.totalGrossAssets)}
          subtext={`${summary.accountCount || 0} Accounts + Investments + Assets`}
          icon={Wallet}
          color="teal"
        />
        <MetricCard
          label="Total Liabilities"
          value={formatINR(summary.totalLiabilities)}
          subtext="Outstanding Borrowings & Loans"
          icon={TrendingDown}
          color="negative"
        />
        <MetricCard
          label="Liquid Bank Balances"
          value={formatINR(summary.totalLiquidCash)}
          subtext="Instantly Available Reserves"
          icon={Landmark}
          color="emerald"
        />
      </div>

      {/* Cash Flow Highlights */}
      <div className="metrics-grid">
        <MetricCard
          label="Income Recorded · All time"
          value={formatINR(summary.totalIncome)}
          subtext="All recorded inflow sources"
          icon={ArrowDownLeft}
          color="positive"
        />
        <MetricCard
          label="Expenses Recorded · All time"
          value={formatINR(summary.totalExpenses)}
          subtext="All recorded expenditures"
          icon={ArrowUpRight}
          color="negative"
        />
        <MetricCard
          label="Net Savings Cashflow"
          value={formatINR(summary.netSavings)}
          subtext={summary.netSavings >= 0 ? 'Surplus wealth retained' : 'Deficit burn rate'}
          icon={PiggyBank}
          color={summary.netSavings >= 0 ? 'positive' : 'negative'}
        />
        <MetricCard
          label="Active Financial Goals"
          value={activeGoals}
          subtext="Milestones in progress"
          icon={PiggyBank}
          color="warning"
        />
      </div>

      {/* Quick Action Operations */}
      <div className="card" style={{ marginBottom: '24px' }}>
        <div className="card-header">
          <span className="card-title">Quick Financial Operations</span>
        </div>
        <div className="card-body" style={{ display: 'flex', flexWrap: 'wrap', gap: '12px' }}>
          <button className="btn btn-primary" onClick={onOpenDeposit} disabled={!hasAccounts} title={!hasAccounts ? 'Open a bank account first.' : undefined}>
            <PlusCircle size={16} /> Record Deposit
          </button>
          <button className="btn btn-secondary" onClick={onOpenWithdraw} disabled={!hasAccounts} title={!hasAccounts ? 'Open a bank account first.' : undefined}>
            <MinusCircle size={16} /> Record Withdrawal
          </button>
          <button className="btn btn-secondary" onClick={onOpenAddIncome}>
            <ArrowDownLeft size={16} /> Record Income
          </button>
          <button className="btn btn-secondary" onClick={onOpenAddExpense}>
            <ArrowUpRight size={16} /> Record Expense
          </button>
          <button className="btn btn-secondary" onClick={() => onNavigate('accounts')}>
            <Landmark size={16} /> Manage Accounts
          </button>
        </div>
      </div>

      {/* Visual Analytics Grid */}
      <div className="dashboard-chart-grid">
        {/* Income vs Expense Chart */}
        <div className="card" style={{ marginBottom: 0 }}>
          <div className="card-header">
            <span className="card-title">Cash flow · {period === 'month' ? 'This month' : period === '30days' ? 'Last 30 days' : 'All time'}</span>
            <label className="visually-hidden" htmlFor="dashboard-period">Cash flow date range</label>
            <select id="dashboard-period" className="form-select dashboard-period" value={period} onChange={(event) => setPeriod(event.target.value)}>
              <option value="month">This month</option>
              <option value="30days">Last 30 days</option>
              <option value="all">All time</option>
            </select>
          </div>
          <div className="card-body">
            <CashFlowBarChart
              income={periodIncome}
              expenses={periodExpenses}
              savings={periodSavings}
              periodLabel={period === 'month' ? 'This month' : period === '30days' ? 'Last 30 days' : 'All time'}
            />
          </div>
        </div>

        {/* Wealth Composition */}
        <div className="card" style={{ marginBottom: 0 }}>
          <div className="card-header">
            <span className="card-title">Current asset mix</span>
          </div>
          <div className="card-body">
            <WealthDonutChart
              liquid={summary.totalLiquidCash}
              investments={summary.totalInvestmentsValue}
              physicalAssets={summary.totalAssetsValue}
            />
          </div>
        </div>

        <div className="card" style={{ marginBottom: 0 }}>
          <div className="card-header">
            <span className="card-title">Largest expense categories</span>
          </div>
          <div className="card-body">
            <ExpenseCategoryChart categories={summary.expenseCategories || []} />
          </div>
        </div>

        <div className="card" style={{ marginBottom: 0 }}>
          <div className="card-header">
            <span className="card-title">Monthly cash flow · last 6 months</span>
          </div>
          <div className="card-body">
            <CashFlowTrendChart incomeList={incomeList} expenseList={expenseList} now={now} />
          </div>
        </div>
      </div>

      {/* Recent Transactions Table */}
      <div className="card">
        <div className="card-header">
          <span className="card-title">Recent Ledger Transactions</span>
          <button className="btn btn-secondary btn-sm" onClick={() => onNavigate('accounts')}>
            View All
          </button>
        </div>
        <div className="card-body" style={{ padding: 0 }}>
          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Account</th>
                  <th>Type</th>
                  <th>Category</th>
                  <th>Description</th>
                  <th style={{ textAlign: 'right' }}>Amount</th>
                  <th style={{ textAlign: 'right' }}>Balance After</th>
                </tr>
              </thead>
              <tbody>
                {(!summary.recentTransactions || summary.recentTransactions.length === 0) ? (
                  <tr>
                    <td colSpan="7" style={{ textAlign: 'center', padding: '24px', color: 'var(--color-text-muted)' }}>
                      No transactions recorded yet. Use the Quick Operations above to perform your first deposit!
                    </td>
                  </tr>
                ) : (
                  summary.recentTransactions.map((tx) => {
                    const isCredit = tx.transactionType === 'DEPOSIT' || tx.transactionType === 'TRANSFER_IN';
                    return (
                      <tr key={tx.id}>
                        <td>{formatDateTime(tx.transactionDate || tx.createdAt)}</td>
                        <td>
                          <strong>{tx.accountName || 'Bank Account'}</strong>
                          <div style={{ fontSize: '11px', color: 'var(--color-text-muted)' }}>{tx.accountNumber}</div>
                        </td>
                        <td>
                          <span className={`badge ${isCredit ? 'badge-positive' : 'badge-negative'}`}>
                            {tx.transactionType}
                          </span>
                        </td>
                        <td>{tx.category || 'General'}</td>
                        <td style={{ color: 'var(--color-text-muted)' }}>{tx.description || '—'}</td>
                        <td style={{ textAlign: 'right', fontWeight: 600, color: isCredit ? 'var(--color-positive)' : 'var(--color-negative)' }}>
                          {isCredit ? '+' : '-'}{formatINR(tx.amount)}
                        </td>
                        <td style={{ textAlign: 'right', fontWeight: 600 }}>
                          {formatINR(tx.balanceAfter)}
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}

