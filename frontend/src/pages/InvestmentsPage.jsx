import React from 'react';
import { PlusCircle, Trash2 } from 'lucide-react';
import { formatINR, formatDate } from '../utils/formatters';
import LoadingSkeleton from '../components/LoadingSkeleton';

export default function InvestmentsPage({
  investments,
  loading,
  onOpenAddInvestment,
  onDeleteInvestment
}) {
  if (loading) {
    return <LoadingSkeleton lines={6} />;
  }

  const totalInvested = investments.reduce((acc, curr) => acc + Number(curr.investedAmount || 0), 0);
  const totalValuation = investments.reduce((acc, curr) => acc + Number(curr.currentValue || 0), 0);
  const totalGain = totalValuation - totalInvested;
  const totalRoi = totalInvested > 0 ? ((totalGain / totalInvested) * 100).toFixed(2) : 0;

  return (
    <div>
      {/* Portfolio Header Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '20px', marginBottom: '24px' }}>
        <div className="card" style={{ padding: '20px', marginBottom: 0 }}>
          <div style={{ fontSize: '12px', color: 'var(--color-text-muted)', textTransform: 'uppercase' }}>Portfolio Valuation</div>
          <div style={{ fontSize: '26px', fontWeight: 700, color: 'var(--color-text-main)', margin: '4px 0' }}>
            {formatINR(totalValuation)}
          </div>
          <div style={{ fontSize: '11px', color: 'var(--color-text-muted)' }}>Estimated current market value</div>
        </div>

        <div className="card" style={{ padding: '20px', marginBottom: 0 }}>
          <div style={{ fontSize: '12px', color: 'var(--color-text-muted)', textTransform: 'uppercase' }}>Principal Invested</div>
          <div style={{ fontSize: '26px', fontWeight: 700, color: 'var(--color-text-main)', margin: '4px 0' }}>
            {formatINR(totalInvested)}
          </div>
          <div style={{ fontSize: '11px', color: 'var(--color-text-muted)' }}>Cost basis across {investments.length} holdings</div>
        </div>

        <div className="card" style={{ padding: '20px', marginBottom: 0 }}>
          <div style={{ fontSize: '12px', color: 'var(--color-text-muted)', textTransform: 'uppercase' }}>Total Unrealized Return</div>
          <div style={{ fontSize: '26px', fontWeight: 700, color: totalGain >= 0 ? 'var(--color-positive)' : 'var(--color-negative)', margin: '4px 0' }}>
            {totalGain >= 0 ? '+' : ''}{formatINR(totalGain)}
          </div>
          <div style={{ fontSize: '12px', fontWeight: 600, color: totalGain >= 0 ? 'var(--color-positive)' : 'var(--color-negative)' }}>
            ROI: {totalGain >= 0 ? '+' : ''}{totalRoi}%
          </div>
        </div>
      </div>

      {/* Holdings Register */}
      <div className="card">
        <div className="card-header">
          <span className="card-title">Investment Holdings Register</span>
          <button className="btn btn-primary btn-sm" onClick={onOpenAddInvestment}>
            <PlusCircle size={14} /> Add Investment
          </button>
        </div>

        <div className="card-body" style={{ padding: 0 }}>
          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Holding Name</th>
                  <th>Asset Type</th>
                  <th>Purchase Date</th>
                  <th style={{ textAlign: 'right' }}>Invested Amount</th>
                  <th style={{ textAlign: 'right' }}>Current Valuation</th>
                  <th style={{ textAlign: 'right' }}>Profit / Loss</th>
                  <th style={{ textAlign: 'right' }}>ROI (%)</th>
                  <th style={{ textAlign: 'center' }}>Action</th>
                </tr>
              </thead>
              <tbody>
                {investments.length === 0 ? (
                  <tr>
                    <td colSpan="8" style={{ textAlign: 'center', padding: '32px', color: 'var(--color-text-muted)' }}>
                      No investments tracked yet. Click "+ Add Investment" to record mutual funds, bluechip equities, or gold bonds.
                    </td>
                  </tr>
                ) : (
                  investments.map((inv) => {
                    const diff = Number(inv.currentValue) - Number(inv.investedAmount);
                    const roi = Number(inv.investedAmount) > 0 ? ((diff / Number(inv.investedAmount)) * 100).toFixed(2) : '0.00';
                    const isProfit = diff >= 0;

                    return (
                      <tr key={inv.id}>
                        <td>
                          <strong>{inv.name}</strong>
                          {inv.notes && <div style={{ fontSize: '11px', color: 'var(--color-text-muted)' }}>{inv.notes}</div>}
                        </td>
                        <td>
                          <span className="badge badge-savings">{inv.investmentType}</span>
                        </td>
                        <td>{formatDate(inv.purchaseDate)}</td>
                        <td style={{ textAlign: 'right', fontWeight: 600 }}>{formatINR(inv.investedAmount)}</td>
                        <td style={{ textAlign: 'right', fontWeight: 600 }}>{formatINR(inv.currentValue)}</td>
                        <td style={{ textAlign: 'right', fontWeight: 600, color: isProfit ? 'var(--color-positive)' : 'var(--color-negative)' }}>
                          {isProfit ? '+' : ''}{formatINR(diff)}
                        </td>
                        <td style={{ textAlign: 'right', fontWeight: 600, color: isProfit ? 'var(--color-positive)' : 'var(--color-negative)' }}>
                          {isProfit ? '+' : ''}{roi}%
                        </td>
                        <td style={{ textAlign: 'center' }}>
                          <button
                            className="btn btn-danger btn-sm"
                            onClick={() => onDeleteInvestment(inv.id, inv.name)}
                            title="Delete Holding"
                            aria-label={`Delete investment ${inv.name}`}
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
    </div>
  );
}

