import React from 'react';
import { PlusCircle, Building2, Banknote, Trash2 } from 'lucide-react';
import { formatINR, formatDate } from '../utils/formatters';
import LoadingSkeleton from '../components/LoadingSkeleton';

export default function AssetsLiabilitiesPage({
  assets,
  loans,
  loading,
  onOpenAddAsset,
  onOpenAddLoan,
  onOpenRepayLoan,
  onDeleteAsset,
  onDeleteLoan
}) {
  if (loading) {
    return <LoadingSkeleton lines={6} />;
  }

  const totalAssetsVal = assets.reduce((acc, curr) => acc + Number(curr.currentValue || 0), 0);
  const totalOutstandingLoans = loans
    .filter((loan) => loan.status === 'ACTIVE')
    .reduce((acc, curr) => acc + Number(curr.outstandingBalance || 0), 0);

  return (
    <div>
      {/* Overview Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))', gap: '20px', marginBottom: '28px' }}>
        <div className="card" style={{ padding: '20px', marginBottom: 0 }}>
          <div style={{ fontSize: '12px', color: 'var(--color-text-muted)', textTransform: 'uppercase' }}>Physical Capital Assets</div>
          <div style={{ fontSize: '26px', fontWeight: 700, color: 'var(--color-primary)', margin: '4px 0' }}>
            {formatINR(totalAssetsVal)}
          </div>
          <div style={{ fontSize: '11px', color: 'var(--color-text-muted)' }}>{assets.length} Real estate, vehicle, & equipment holdings</div>
        </div>

        <div className="card" style={{ padding: '20px', marginBottom: 0 }}>
          <div style={{ fontSize: '12px', color: 'var(--color-text-muted)', textTransform: 'uppercase' }}>Total Loan Liabilities</div>
          <div style={{ fontSize: '26px', fontWeight: 700, color: 'var(--color-negative)', margin: '4px 0' }}>
            {formatINR(totalOutstandingLoans)}
          </div>
          <div style={{ fontSize: '11px', color: 'var(--color-text-muted)' }}>{loans.filter(l => l.status === 'ACTIVE').length} Active loan facilities outstanding</div>
        </div>

        <div className="card" style={{ padding: '20px', marginBottom: 0 }}>
          <div style={{ fontSize: '12px', color: 'var(--color-text-muted)', textTransform: 'uppercase' }}>Physical Net Equity</div>
          <div style={{ fontSize: '26px', fontWeight: 700, color: totalAssetsVal >= totalOutstandingLoans ? 'var(--color-positive)' : 'var(--color-negative)', margin: '4px 0' }}>
            {formatINR(totalAssetsVal - totalOutstandingLoans)}
          </div>
          <div style={{ fontSize: '11px', color: 'var(--color-text-muted)' }}>Physical Assets minus total debt</div>
        </div>
      </div>

      {/* Section 1: Physical Capital Assets */}
      <div className="card" style={{ marginBottom: '28px' }}>
        <div className="card-header">
          <span className="card-title">
            <Building2 size={18} /> Physical Capital Assets Register
          </span>
          <button className="btn btn-primary btn-sm" onClick={onOpenAddAsset}>
            <PlusCircle size={14} /> Add Physical Asset
          </button>
        </div>

        <div className="card-body" style={{ padding: 0 }}>
          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Asset Description</th>
                  <th>Category</th>
                  <th>Acquisition Date</th>
                  <th style={{ textAlign: 'right' }}>Purchase Cost</th>
                  <th style={{ textAlign: 'right' }}>Current Valuation</th>
                  <th style={{ textAlign: 'right' }}>Appreciation</th>
                  <th style={{ textAlign: 'center' }}>Action</th>
                </tr>
              </thead>
              <tbody>
                {assets.length === 0 ? (
                  <tr>
                    <td colSpan="7" style={{ textAlign: 'center', padding: '32px', color: 'var(--color-text-muted)' }}>
                      No physical assets recorded. Add property or vehicles to track net equity.
                    </td>
                  </tr>
                ) : (
                  assets.map((asset) => {
                    const diff = Number(asset.currentValue) - Number(asset.purchaseValue);
                    const pct = Number(asset.purchaseValue) > 0 ? ((diff / Number(asset.purchaseValue)) * 100).toFixed(1) : 0;
                    return (
                      <tr key={asset.id}>
                        <td>
                          <strong>{asset.name}</strong>
                          {asset.notes && <div style={{ fontSize: '11px', color: 'var(--color-text-muted)' }}>{asset.notes}</div>}
                        </td>
                        <td>
                          <span className="badge badge-savings">{asset.assetCategory}</span>
                        </td>
                        <td>{formatDate(asset.acquisitionDate)}</td>
                        <td style={{ textAlign: 'right', fontWeight: 600 }}>{formatINR(asset.purchaseValue)}</td>
                        <td style={{ textAlign: 'right', fontWeight: 600 }}>{formatINR(asset.currentValue)}</td>
                        <td style={{ textAlign: 'right', fontWeight: 600, color: diff >= 0 ? 'var(--color-positive)' : 'var(--color-negative)' }}>
                          {diff >= 0 ? '+' : ''}{formatINR(diff)} ({pct}%)
                        </td>
                        <td style={{ textAlign: 'center' }}>
                          <button
                            className="btn btn-danger btn-sm"
                            onClick={() => onDeleteAsset(asset.id, asset.name)}
                            title="Delete Asset"
                            aria-label={`Delete asset ${asset.name}`}
                          >
                            <Trash2 size={13} />
                          </button>
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

      {/* Section 2: Liabilities & Borrowings */}
      <div className="card">
        <div className="card-header">
          <span className="card-title">
            <Banknote size={18} /> Liabilities & Loan Facilities
          </span>
          <button className="btn btn-primary btn-sm" onClick={onOpenAddLoan}>
            <PlusCircle size={14} /> Add Loan Facility
          </button>
        </div>

        <div className="card-body" style={{ padding: 0 }}>
          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Loan Name & Lender</th>
                  <th>Principal</th>
                  <th>Interest Rate</th>
                  <th>Tenure</th>
                  <th style={{ textAlign: 'right' }}>Monthly EMI</th>
                  <th style={{ textAlign: 'right' }}>Outstanding Balance</th>
                  <th>Status</th>
                  <th style={{ textAlign: 'center' }}>Action</th>
                </tr>
              </thead>
              <tbody>
                {loans.length === 0 ? (
                  <tr>
                    <td colSpan="8" style={{ textAlign: 'center', padding: '32px', color: 'var(--color-text-muted)' }}>
                      No liabilities recorded. (Zero debt position).
                    </td>
                  </tr>
                ) : (
                  loans.map((loan) => (
                    <tr key={loan.id}>
                      <td>
                        <strong>{loan.loanName}</strong>
                        <div style={{ fontSize: '11px', color: 'var(--color-text-muted)' }}>{loan.lender} • Started {formatDate(loan.startDate)}</div>
                      </td>
                      <td style={{ fontWeight: 600 }}>{formatINR(loan.principalAmount)}</td>
                      <td>{loan.interestRate}% p.a.</td>
                      <td>{loan.tenureMonths} Months</td>
                      <td style={{ textAlign: 'right', fontWeight: 600, color: 'var(--color-negative)' }}>
                        {formatINR(loan.monthlyEmi)}
                      </td>
                      <td style={{ textAlign: 'right', fontWeight: 700, color: Number(loan.outstandingBalance) > 0 ? 'var(--color-text-main)' : 'var(--color-positive)' }}>
                        {formatINR(loan.outstandingBalance)}
                      </td>
                      <td>
                        <span className={`badge ${loan.status === 'ACTIVE' ? 'badge-negative' : 'badge-positive'}`}>
                          {loan.status}
                        </span>
                      </td>
                      <td style={{ textAlign: 'center' }}>
                        <div style={{ display: 'inline-flex', gap: '6px' }}>
                          {loan.status === 'ACTIVE' && (
                            <button
                              className="btn btn-primary btn-sm"
                              onClick={() => onOpenRepayLoan(loan)}
                              aria-label={`Record a repayment for ${loan.loanName}`}
                            >
                              Repay EMI
                            </button>
                          )}
                          <button
                            className="btn btn-danger btn-sm"
                            onClick={() => onDeleteLoan(loan.id, loan.loanName)}
                            title="Delete Loan"
                            aria-label={`Delete loan ${loan.loanName}`}
                          >
                            <Trash2 size={13} />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}

