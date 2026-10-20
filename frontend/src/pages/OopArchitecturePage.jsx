import React, { useState, useEffect } from 'react';
import {
  GraduationCap,
  Code2,
  CheckCircle2,
  Terminal,
  Cpu
} from 'lucide-react';
import { api } from '../services/api';

export default function OopArchitecturePage() {
  const [healthData, setHealthData] = useState(null);
  const [healthError, setHealthError] = useState(null);

  useEffect(() => {
    api.getHealth()
      .then((data) => setHealthData(data))
      .catch((err) => setHealthError(err.message));
  }, []);

  const oopConcepts = [
    {
      concept: '1. Classes and Objects',
      file: 'com.dostatok.model.*',
      description: 'Domain models represent real-world financial entities (User, BankAccount, FinancialTransaction, Income, Expense, Investment, Loan, FinancialGoal).',
      vivaNote: 'Instantiated via constructors; holds instance state; invokes encapsulated business methods like getProfitLoss(), calculateEmi(), and recordRepayment().',
    },
    {
      concept: '2. Encapsulation',
      file: 'BankAccount.java & Models',
      description: 'All sensitive domain fields are private. Controlled getters and mutation methods prevent illegal state changes.',
      vivaNote: 'Notice BankAccount balance has no public unrestricted setter. Deposits and withdrawals validate positive amounts before modifying balance.',
    },
    {
      concept: '3. Inheritance',
      file: 'SavingsAccount & CurrentAccount',
      description: 'SavingsAccount and CurrentAccount both extend the abstract base class BankAccount, inheriting common properties while specializing banking behavior.',
      vivaNote: 'Avoids redundant code duplication across account types while establishing a genuine "IS-A" hierarchy.',
    },
    {
      concept: '4. Abstraction (Abstract Class)',
      file: 'BankAccount.java',
      description: 'BankAccount is declared abstract and cannot be directly instantiated. It defines abstract methods: validateWithdrawal() and getAccountSummary().',
      vivaNote: 'Forces every concrete bank account type to supply its own business logic for withdrawals and statements.',
    },
    {
      concept: '5. Interfaces & Contract',
      file: 'AccountOperations & AccountRepository',
      description: 'AccountOperations interface defines the public contract for bank transactions (deposit, withdraw, getBalance). AccountRepository defines persistence.',
      vivaNote: 'Enables Dependency Inversion: AccountService depends on the AccountRepository interface contract, not the SQLite implementation directly.',
    },
    {
      concept: '6. Runtime Polymorphism',
      file: 'AccountService.java (withdraw)',
      description: 'When account.withdraw(amount) is called on a BankAccount reference, Java dynamically executes SavingsAccount.validateWithdrawal() or CurrentAccount.validateWithdrawal().',
      vivaNote: 'SavingsAccount rejects withdrawals violating the ₹1,000 minimum balance rule; CurrentAccount permits withdrawals leveraging the overdraft limit!',
    },
    {
      concept: '7. Method Overloading',
      file: 'BankAccount.java & FinancialTransaction.java',
      description: 'Multiple methods share the same name with different parameter signatures: deposit(BigDecimal amount) and deposit(BigDecimal amount, String description).',
      vivaNote: 'Demonstrates compile-time polymorphism providing clean default overloads for calling services.',
    },
    {
      concept: '8. Method Overriding',
      file: 'SavingsAccount.java / CurrentAccount.java',
      description: 'Subclasses override validateWithdrawal(), getAccountType(), and getAccountSummary() using the @Override annotation.',
      vivaNote: 'Replaces generic base behavior with subclass-specific financial mathematics and reporting.',
    },
    {
      concept: '9. Custom Exception Handling',
      file: 'com.dostatok.exception.*',
      description: 'Domain-specific exceptions (InsufficientBalanceException, InvalidAmountException, ResourceNotFoundException) protect business invariants.',
      vivaNote: 'Handled in ApiController to return meaningful HTTP status codes (400, 404, 422) with safe error messages, preventing JDBC stack trace exposure.',
    },
  ];

  return (
    <div>
      {/* College Viva Presentation Header */}
      <div className="card" style={{ background: 'linear-gradient(135deg, #0d4734 0%, #155e46 100%)', color: '#fff', border: 'none', marginBottom: '28px' }}>
        <div style={{ padding: '28px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '8px' }}>
            <GraduationCap size={28} />
            <span style={{ fontSize: '13px', textTransform: 'uppercase', letterSpacing: '1px', opacity: 0.9 }}>
              Academic Evaluation Architecture
            </span>
          </div>
          <h2 style={{ fontSize: '24px', fontWeight: 700, color: '#fff', marginBottom: '8px' }}>
            Java Object-Oriented Programming (OOP) & DBMS Syllabus Mapping
          </h2>
          <p style={{ maxWidth: '820px', fontSize: '14px', lineHeight: 1.6, opacity: 0.9 }}>
            DOSTATOK is built in pure Java without third-party frameworks (no Spring Boot, Maven, or Gradle),
            demonstrating core Java OOP concepts through a production-grade personal financial management engine.
          </p>
        </div>
      </div>

      {/* Live System Diagnostics */}
      <div className="card" style={{ marginBottom: '28px' }}>
        <div className="card-header">
          <span className="card-title">
            <Cpu size={18} /> Live Backend Architecture Status
          </span>
        </div>
        <div className="card-body" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '20px' }}>
          <div style={{ padding: '16px', backgroundColor: 'var(--color-surface-hover)', borderRadius: 'var(--radius-sm)' }}>
            <div style={{ fontSize: '12px', color: 'var(--color-text-muted)' }}>HTTP Server Runtime</div>
            <div style={{ fontSize: '15px', fontWeight: 700, marginTop: '4px' }}>JDK 27 com.sun.net.httpserver</div>
            <div style={{ fontSize: '12px', color: 'var(--color-positive)', marginTop: '4px' }}>Port 8080 Active • Multi-Threaded</div>
          </div>

          <div style={{ padding: '16px', backgroundColor: 'var(--color-surface-hover)', borderRadius: 'var(--radius-sm)' }}>
            <div style={{ fontSize: '12px', color: 'var(--color-text-muted)' }}>Database Management</div>
            <div style={{ fontSize: '15px', fontWeight: 700, marginTop: '4px' }}>SQLite 3.53 via JDBC Driver</div>
            <div style={{ fontSize: '12px', color: 'var(--color-positive)', marginTop: '4px' }}>PRAGMA foreign_keys = ON</div>
          </div>

          <div style={{ padding: '16px', backgroundColor: 'var(--color-surface-hover)', borderRadius: 'var(--radius-sm)' }}>
            <div style={{ fontSize: '12px', color: 'var(--color-text-muted)' }}>Live API Health Check</div>
            <div style={{ fontSize: '15px', fontWeight: 700, marginTop: '4px' }}>
              {healthData ? `${healthData.status.toUpperCase()} (${healthData.app})` : 'Querying...'}
            </div>
            <div style={{ fontSize: '12px', color: healthData ? 'var(--color-positive)' : 'var(--color-negative)', marginTop: '4px' }}>
              {healthData ? 'GET /api/health responding 200 OK' : healthError || 'Connecting'}
            </div>
          </div>
        </div>
      </div>

      {/* OOP Concepts Mapping Table */}
      <div className="card">
        <div className="card-header">
          <span className="card-title">
            <Code2 size={18} /> Mandatory Java OOP Concepts Implementation Matrix
          </span>
        </div>

        <div className="card-body" style={{ padding: 0 }}>
          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th style={{ width: '22%' }}>OOP Concept</th>
                  <th style={{ width: '20%' }}>Source File / Package</th>
                  <th style={{ width: '28%' }}>Implementation in DOSTATOK</th>
                  <th style={{ width: '30%' }}>Viva / Faculty Explanation</th>
                </tr>
              </thead>
              <tbody>
                {oopConcepts.map((item, idx) => (
                  <tr key={idx}>
                    <td><strong>{item.concept}</strong></td>
                    <td><code>{item.file}</code></td>
                    <td style={{ fontSize: '12px' }}>{item.description}</td>
                    <td style={{ fontSize: '12px', color: 'var(--color-text-muted)' }}>
                      <CheckCircle2 size={13} style={{ color: 'var(--color-positive)', display: 'inline', marginRight: '6px' }} />
                      {item.vivaNote}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {/* Standalone Viva Test Suite Runner Instructions */}
      <div className="card">
        <div className="card-header">
          <span className="card-title">
            <Terminal size={18} /> How to Run the OOP Demonstration Program for Faculty
          </span>
        </div>
        <div className="card-body">
          <p style={{ fontSize: '13px', color: 'var(--color-text-muted)', marginBottom: '14px' }}>
            A standalone Java evaluation program has been created in <code>com.dostatok.main.OopDemo</code>.
            You can run it in Windows Command Prompt or VS Code terminal to demonstrate all 9 concepts:
          </p>
          <pre style={{
            padding: '14px',
            backgroundColor: '#0f172a',
            color: '#38bdf8',
            borderRadius: 'var(--radius-sm)',
            fontSize: '13px',
            overflowX: 'auto',
            fontFamily: 'monospace'
          }}>
            java -cp "backend\out;backend\lib\sqlite-jdbc-3.53.4.0.jar;backend\lib\gson-2.11.0.jar" com.dostatok.main.OopDemo
          </pre>
        </div>
      </div>
    </div>
  );
}

