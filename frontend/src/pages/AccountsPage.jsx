import React, { useState } from 'react';
import {
  PlusCircle,
  ArrowRightLeft,
  ArrowDownLeft,
  ArrowUpRight,
  Landmark,
  Trash2,
  Shield,
  CreditCard,
  Search
} from 'lucide-react';
import { formatINR, formatDateTime } from '../utils/formatters';
import LoadingSkeleton from '../components/LoadingSkeleton';

export default function AccountsPage({
  accounts,
  transactions,
  loading,
  onOpenCreateAccount,
  onOpenDeposit,
  onOpenWithdraw,
  onOpenTransfer,
  onDeleteAccount
}) {
  const [selectedAccountId, setSelectedAccountId] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');

  if (loading) {
    return <LoadingSkeleton lines={6} />;
  }

  // Filter transactions
  const filteredTransactions = transactions.filter((tx) => {
    const matchesAccount = selectedAccountId === 'ALL' || tx.accountId === Number(selectedAccountId);
    const matchesQuery = !searchQuery ||
      (tx.description && tx.description.toLowerCase().includes(searchQuery.toLowerCase())) ||
      (tx.category && tx.category.toLowerCase().includes(searchQuery.toLowerCase())) ||
      (tx.accountName && tx.accountName.toLowerCase().includes(searchQuery.toLowerCase()));
    return matchesAccount && matchesQuery;
  });

  return (
    <div>
      {/* Top Action Bar */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px', flexWrap: 'wrap', gap: '12px' }}>
        <div>
          <h2 style={{ fontSize: '18px', fontWeight: 700 }}>Bank Accounts Portfolio</h2>
          <p style={{ fontSize: '13px', color: 'var(--color-text-muted)' }}>
            Manage your account balances and review the latest transaction activity.
          </p>
        </div>
        <div style={{ display: 'flex', gap: '12px' }}>
          <button className="btn btn-secondary" onClick={onOpenTransfer} disabled={accounts.length < 2} title={accounts.length < 2 ? 'Open a second account to make a transfer.' : undefined}>
            <ArrowRightLeft size={16} /> Inter-Account Transfer
          </button>
          <button className="btn btn-primary" onClick={onOpenCreateAccount}>
            <PlusCircle size={16} /> Open New Account
          </button>
        </div>
      </div>

      {/* Account Cards Grid */}
      {accounts.length === 0 && (
        <section className="card account-empty-state" aria-labelledby="account-empty-title">
            <Landmark aria-hidden="true" />
          <div>
            <h3 id="account-empty-title">No bank accounts yet</h3>
            <p>Add an account to record balances, deposits, and transfers.</p>
          </div>
          <button className="btn btn-primary" onClick={onOpenCreateAccount}>
            <PlusCircle size={16} aria-hidden="true" /> Open an account
          </button>
        </section>
      )}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '20px', marginBottom: '32px' }}>
        {accounts.map((acc) => {
          const isSavings = acc.accountType === 'SAVINGS';
          return (
            <div key={acc.id} className="card" style={{ marginBottom: 0 }}>
              <div className="card-header" style={{ justifyContent: 'space-between' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <div style={{
                    width: '32px', height: '32px', borderRadius: '6px',
                    backgroundColor: isSavings ? 'var(--color-primary-light)' : '#ede9fe',
                    color: isSavings ? 'var(--color-primary)' : '#6d28d9',
                    display: 'flex', alignItems: 'center', justifyContent: 'center'
                  }}>
                    {isSavings ? <Shield size={18} /> : <CreditCard size={18} />}
                  </div>
                  <div>
                    <strong style={{ fontSize: '14px' }}>{acc.accountName}</strong>
                    <div style={{ fontSize: '11px', color: 'var(--color-text-muted)' }}>{acc.bankName} • {acc.accountNumber}</div>
                  </div>
                </div>
                <span className={`badge ${isSavings ? 'badge-savings' : 'badge-current'}`}>
                  {acc.accountType}
                </span>
              </div>

              <div className="card-body">
                <div style={{ marginBottom: '16px' }}>
                  <div style={{ fontSize: '11px', color: 'var(--color-text-muted)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
                    Available Balance
                  </div>
                  <div style={{ fontSize: '26px', fontWeight: 700, color: 'var(--color-text-main)' }}>
                    {formatINR(acc.balance)}
                  </div>
                </div>

                {/* Polymorphic Policy Metadata */}
                <div style={{
                  padding: '10px 14px',
                  backgroundColor: 'var(--color-surface-hover)',
                  borderRadius: 'var(--radius-sm)',
                  fontSize: '12px',
                  color: 'var(--color-text-muted)',
                  marginBottom: '18px'
                }}>
                  {isSavings ? (
                    <div>
                      <div>Minimum Balance Rule: <strong>{formatINR(acc.minimumBalance)}</strong></div>
                      <div>Interest Rate: <strong>{acc.interestRate || '3.50'}% p.a.</strong></div>
                    </div>
                  ) : (
                    <div>
                      <div>Overdraft Facility: <strong>{formatINR(acc.overdraftLimit)}</strong></div>
                      <div>Effective Purchasing Power: <strong>{formatINR(Number(acc.balance) + Number(acc.overdraftLimit || 0))}</strong></div>
                    </div>
                  )}
                </div>

                {/* Operations on this account */}
                <div style={{ display: 'flex', gap: '8px' }}>
                  <button
                    className="btn btn-primary btn-sm"
                    style={{ flex: 1 }}
                    onClick={() => onOpenDeposit(acc.id)}
                  >
                    <ArrowDownLeft size={14} /> Deposit
                  </button>
                  <button
                    className="btn btn-secondary btn-sm"
                    style={{ flex: 1 }}
                    onClick={() => onOpenWithdraw(acc.id)}
                  >
                    <ArrowUpRight size={14} /> Withdraw
                  </button>
                  <button
                    className="btn btn-danger btn-sm"
                    title="Delete Account (Must have zero balance)"
                    aria-label={`Delete ${acc.accountName}`}
                    onClick={() => onDeleteAccount(acc.id, acc.accountName, acc.balance)}
                  >
                    <Trash2 size={14} />
                  </button>
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* Transaction Statement Ledger */}
      <div className="card">
        <div className="card-header" style={{ flexWrap: 'wrap', gap: '12px' }}>
          <span className="card-title">Transaction ledger · latest 100</span>
          <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
            {/* Account Filter */}
            <select
              aria-label="Filter transactions by account"
              className="form-select"
              style={{ width: 'auto', padding: '6px 12px', fontSize: '13px' }}
              value={selectedAccountId}
              onChange={(e) => setSelectedAccountId(e.target.value)}
            >
              <option value="ALL">All Accounts</option>
              {accounts.map((a) => (
                <option key={a.id} value={a.id}>{a.accountName} ({a.accountNumber})</option>
              ))}
            </select>

            {/* Search Input */}
            <div style={{ position: 'relative' }}>
              <label className="visually-hidden" htmlFor="ledger-search">Search transaction ledger</label>
              <input
                id="ledger-search"
                type="text"
                placeholder="Search description/category..."
                className="form-input"
                style={{ padding: '6px 10px 6px 30px', fontSize: '13px', width: '220px' }}
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
              <Search size={14} style={{ position: 'absolute', left: '10px', top: '10px', color: 'var(--color-text-subtle)' }} />
            </div>
          </div>
        </div>

        <div className="card-body" style={{ padding: 0 }}>
          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Timestamp</th>
                  <th>Account</th>
                  <th>Type</th>
                  <th>Category</th>
                  <th>Description</th>
                  <th style={{ textAlign: 'right' }}>Amount</th>
                  <th style={{ textAlign: 'right' }}>Balance After</th>
                </tr>
              </thead>
              <tbody>
                {filteredTransactions.length === 0 ? (
                  <tr>
                    <td colSpan="7" style={{ textAlign: 'center', padding: '32px', color: 'var(--color-text-muted)' }}>
                      No transactions match the selected filter.
                    </td>
                  </tr>
                ) : (
                  filteredTransactions.map((tx) => {
                    const isCredit = tx.transactionType === 'DEPOSIT' || tx.transactionType === 'TRANSFER_IN';
                    return (
                      <tr key={tx.id}>
                        <td>{formatDateTime(tx.transactionDate || tx.createdAt)}</td>
                        <td>
                          <strong>{tx.accountName || 'Account'}</strong>
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

