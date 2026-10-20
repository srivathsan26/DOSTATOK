import React, { useState } from 'react';
import { PlusCircle, ArrowDownLeft, ArrowUpRight, Trash2 } from 'lucide-react';
import { formatINR, formatDate } from '../utils/formatters';
import LoadingSkeleton from '../components/LoadingSkeleton';

export default function CashflowPage({
  incomeList,
  expenseList,
  loading,
  onOpenAddIncome,
  onOpenAddExpense,
  onDeleteIncome,
  onDeleteExpense
}) {
  const [activeTab, setActiveTab] = useState('income');
  const [categoryFilter, setCategoryFilter] = useState('ALL');

  if (loading) {
    return <LoadingSkeleton lines={6} />;
  }

  const totalIncome = incomeList.reduce((acc, curr) => acc + Number(curr.amount || 0), 0);
  const totalExpense = expenseList.reduce((acc, curr) => acc + Number(curr.amount || 0), 0);
  const netSurplus = totalIncome - totalExpense;

  const filteredIncome = incomeList.filter(
    (item) => categoryFilter === 'ALL' || item.category === categoryFilter
  );
  const filteredExpenses = expenseList.filter(
    (item) => categoryFilter === 'ALL' || item.category === categoryFilter
  );

  const categories = activeTab === 'income'
    ? Array.from(new Set(incomeList.map((i) => i.category)))
    : Array.from(new Set(expenseList.map((e) => e.category)));

  return (
    <div>
      {/* Financial Inflow/Outflow Overview Banner */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '20px', marginBottom: '24px' }}>
        <div className="card" style={{ padding: '20px', marginBottom: 0 }}>
          <div style={{ fontSize: '12px', color: 'var(--color-text-muted)', textTransform: 'uppercase' }}>Total Inflow (Income)</div>
          <div style={{ fontSize: '24px', fontWeight: 700, color: 'var(--color-positive)', margin: '4px 0' }}>
            +{formatINR(totalIncome)}
          </div>
          <div style={{ fontSize: '11px', color: 'var(--color-text-muted)' }}>{incomeList.length} Entries recorded</div>
        </div>

        <div className="card" style={{ padding: '20px', marginBottom: 0 }}>
          <div style={{ fontSize: '12px', color: 'var(--color-text-muted)', textTransform: 'uppercase' }}>Total Outflow (Expenses)</div>
          <div style={{ fontSize: '24px', fontWeight: 700, color: 'var(--color-negative)', margin: '4px 0' }}>
            -{formatINR(totalExpense)}
          </div>
          <div style={{ fontSize: '11px', color: 'var(--color-text-muted)' }}>{expenseList.length} Entries recorded</div>
        </div>

        <div className="card" style={{ padding: '20px', marginBottom: 0 }}>
          <div style={{ fontSize: '12px', color: 'var(--color-text-muted)', textTransform: 'uppercase' }}>Net Cash Retention</div>
          <div style={{ fontSize: '24px', fontWeight: 700, color: netSurplus >= 0 ? 'var(--color-positive)' : 'var(--color-negative)', margin: '4px 0' }}>
            {netSurplus >= 0 ? '+' : ''}{formatINR(netSurplus)}
          </div>
          <div style={{ fontSize: '11px', color: 'var(--color-text-muted)' }}>Surplus retention rate: {totalIncome > 0 ? ((netSurplus / totalIncome) * 100).toFixed(1) : 0}%</div>
        </div>
      </div>

      {/* Tabs & Filter Bar */}
      <div className="card">
        <div className="card-header" style={{ flexWrap: 'wrap', gap: '12px' }}>
          <div className="tab-list" style={{ marginBottom: 0, borderBottom: 'none' }}>
            <button
              className={`tab-btn ${activeTab === 'income' ? 'active' : ''}`}
              onClick={() => { setActiveTab('income'); setCategoryFilter('ALL'); }}
            >
              <ArrowDownLeft size={16} style={{ display: 'inline', marginRight: '6px', verticalAlign: 'text-bottom' }} />
              Income Stream ({incomeList.length})
            </button>
            <button
              className={`tab-btn ${activeTab === 'expense' ? 'active' : ''}`}
              onClick={() => { setActiveTab('expense'); setCategoryFilter('ALL'); }}
            >
              <ArrowUpRight size={16} style={{ display: 'inline', marginRight: '6px', verticalAlign: 'text-bottom' }} />
              Expenditures ({expenseList.length})
            </button>
          </div>

          <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
            <select
              aria-label="Filter records by category"
              className="form-select"
              style={{ width: 'auto', padding: '6px 12px', fontSize: '13px' }}
              value={categoryFilter}
              onChange={(e) => setCategoryFilter(e.target.value)}
            >
              <option value="ALL">All Categories</option>
              {categories.map((cat) => (
                <option key={cat} value={cat}>{cat}</option>
              ))}
            </select>

            {activeTab === 'income' ? (
              <button className="btn btn-primary btn-sm" onClick={onOpenAddIncome}>
                <PlusCircle size={14} /> Add Income
              </button>
            ) : (
              <button className="btn btn-primary btn-sm" onClick={onOpenAddExpense}>
                <PlusCircle size={14} /> Add Expense
              </button>
            )}
          </div>
        </div>

        <div className="card-body" style={{ padding: 0 }}>
          <div className="table-container">
            {activeTab === 'income' ? (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Date</th>
                    <th>Title</th>
                    <th>Category</th>
                    <th>Source</th>
                    <th>Bank Account</th>
                    <th>Notes</th>
                    <th style={{ textAlign: 'right' }}>Amount</th>
                    <th style={{ textAlign: 'center' }}>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredIncome.length === 0 ? (
                    <tr>
                      <td colSpan="8" style={{ textAlign: 'center', padding: '32px', color: 'var(--color-text-muted)' }}>
                        No income records found. Click "+ Add Income" to record a salary or dividend payout.
                      </td>
                    </tr>
                  ) : (
                    filteredIncome.map((inc) => (
                      <tr key={inc.id}>
                        <td>{formatDate(inc.incomeDate)}</td>
                        <td><strong>{inc.title}</strong></td>
                        <td><span className="badge badge-positive">{inc.category}</span></td>
                        <td>{inc.source}</td>
                        <td>{inc.accountName || '—'}</td>
                        <td style={{ color: 'var(--color-text-muted)' }}>{inc.notes || '—'}</td>
                        <td style={{ textAlign: 'right', fontWeight: 600, color: 'var(--color-positive)' }}>
                          +{formatINR(inc.amount)}
                        </td>
                        <td style={{ textAlign: 'center' }}>
                          <button
                            className="btn btn-danger btn-sm"
                            onClick={() => onDeleteIncome(inc.id, inc.title)}
                            title="Delete Record"
                            aria-label={`Delete income ${inc.title}`}
                          >
                            <Trash2 size={13} />
                          </button>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Date</th>
                    <th>Title</th>
                    <th>Category</th>
                    <th>Payment Method</th>
                    <th>Bank Account</th>
                    <th>Notes</th>
                    <th style={{ textAlign: 'right' }}>Amount</th>
                    <th style={{ textAlign: 'center' }}>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredExpenses.length === 0 ? (
                    <tr>
                      <td colSpan="8" style={{ textAlign: 'center', padding: '32px', color: 'var(--color-text-muted)' }}>
                        No expense records found. Click "+ Add Expense" to record a payment.
                      </td>
                    </tr>
                  ) : (
                    filteredExpenses.map((exp) => (
                      <tr key={exp.id}>
                        <td>{formatDate(exp.expenseDate)}</td>
                        <td><strong>{exp.title}</strong></td>
                        <td><span className="badge badge-negative">{exp.category}</span></td>
                        <td>{exp.paymentMethod}</td>
                        <td>{exp.accountName || '—'}</td>
                        <td style={{ color: 'var(--color-text-muted)' }}>{exp.notes || '—'}</td>
                        <td style={{ textAlign: 'right', fontWeight: 600, color: 'var(--color-negative)' }}>
                          -{formatINR(exp.amount)}
                        </td>
                        <td style={{ textAlign: 'center' }}>
                          <button
                            className="btn btn-danger btn-sm"
                            onClick={() => onDeleteExpense(exp.id, exp.title)}
                            title="Delete Record"
                            aria-label={`Delete expense ${exp.title}`}
                          >
                            <Trash2 size={13} />
                          </button>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

