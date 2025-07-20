import React, { useState, useEffect } from 'react';
import { fetchClients, searchClients } from '../utils/api';

const ClientList = () => {
  const [clients, setClients] = useState([]);
  const [filteredClients, setFilteredClients] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    loadClients();
  }, []);

  useEffect(() => {
    if (searchTerm.trim() === '') {
      setFilteredClients(clients);
    } else {
      const filtered = clients.filter(client =>
        client.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        client.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
        client.phone.includes(searchTerm)
      );
      setFilteredClients(filtered);
    }
  }, [searchTerm, clients]);

  const loadClients = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await fetchClients();
      setClients(data);
      setFilteredClients(data);
    } catch (err) {
      setError('Failed to load clients. Please try again.');
      console.error('Error loading clients:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    setSearchTerm(e.target.value);
  };

  const containerStyle = {
    backgroundColor: '#ffffff',
    borderRadius: '8px',
    boxShadow: '0 1px 3px 0 rgba(0, 0, 0, 0.1)',
    padding: '1.5rem',
    margin: '1rem',
  };

  const headerStyle = {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '1.5rem',
  };

  const titleStyle = {
    fontSize: '1.5rem',
    fontWeight: '600',
    color: '#1e293b',
    margin: 0,
  };

  const searchInputStyle = {
    padding: '0.5rem 1rem',
    border: '1px solid #d1d5db',
    borderRadius: '6px',
    fontSize: '0.875rem',
    width: '300px',
    outline: 'none',
    transition: 'border-color 0.2s',
  };

  const clientCardStyle = {
    border: '1px solid #e5e7eb',
    borderRadius: '6px',
    padding: '1rem',
    marginBottom: '0.75rem',
    transition: 'all 0.2s',
    cursor: 'pointer',
  };

  const clientCardHoverStyle = {
    ...clientCardStyle,
    borderColor: '#3b82f6',
    boxShadow: '0 2px 4px 0 rgba(0, 0, 0, 0.1)',
  };

  const clientNameStyle = {
    fontSize: '1.125rem',
    fontWeight: '600',
    color: '#1e293b',
    marginBottom: '0.5rem',
  };

  const clientInfoStyle = {
    fontSize: '0.875rem',
    color: '#64748b',
    marginBottom: '0.25rem',
    display: 'flex',
    alignItems: 'center',
    gap: '0.5rem',
  };

  const labelStyle = {
    fontWeight: '500',
    color: '#374151',
    minWidth: '60px',
  };

  const loadingStyle = {
    textAlign: 'center',
    padding: '2rem',
    color: '#64748b',
  };

  const errorStyle = {
    textAlign: 'center',
    padding: '2rem',
    color: '#dc2626',
    backgroundColor: '#fef2f2',
    borderRadius: '6px',
    border: '1px solid #fecaca',
  };

  const emptyStateStyle = {
    textAlign: 'center',
    padding: '3rem',
    color: '#64748b',
  };

  const refreshButtonStyle = {
    backgroundColor: '#3b82f6',
    color: 'white',
    border: 'none',
    borderRadius: '6px',
    padding: '0.5rem 1rem',
    fontSize: '0.875rem',
    fontWeight: '500',
    cursor: 'pointer',
    transition: 'background-color 0.2s',
  };

  const countStyle = {
    fontSize: '0.875rem',
    color: '#64748b',
    marginBottom: '1rem',
  };

  if (loading) {
    return (
      <div style={containerStyle}>
        <div style={loadingStyle}>
          <div>Loading clients...</div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div style={containerStyle}>
        <div style={errorStyle}>
          <div>{error}</div>
          <button 
            style={{...refreshButtonStyle, marginTop: '1rem'}} 
            onClick={loadClients}
            onMouseOver={(e) => e.target.style.backgroundColor = '#2563eb'}
            onMouseOut={(e) => e.target.style.backgroundColor = '#3b82f6'}
          >
            Try Again
          </button>
        </div>
      </div>
    );
  }

  return (
    <div style={containerStyle}>
      <div style={headerStyle}>
        <h2 style={titleStyle}>Clients</h2>
        <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
          <input
            type="text"
            placeholder="Search clients..."
            value={searchTerm}
            onChange={handleSearch}
            style={searchInputStyle}
            onFocus={(e) => e.target.style.borderColor = '#3b82f6'}
            onBlur={(e) => e.target.style.borderColor = '#d1d5db'}
          />
          <button 
            style={refreshButtonStyle}
            onClick={loadClients}
            onMouseOver={(e) => e.target.style.backgroundColor = '#2563eb'}
            onMouseOut={(e) => e.target.style.backgroundColor = '#3b82f6'}
          >
            Refresh
          </button>
        </div>
      </div>

      <div style={countStyle}>
        {searchTerm ? (
          <>Showing {filteredClients.length} of {clients.length} clients</>
        ) : (
          <>Total clients: {clients.length}</>
        )}
      </div>

      {filteredClients.length === 0 ? (
        <div style={emptyStateStyle}>
          {searchTerm ? (
            <>
              <div style={{ fontSize: '1.125rem', marginBottom: '0.5rem' }}>No clients found</div>
              <div>Try adjusting your search terms</div>
            </>
          ) : (
            <>
              <div style={{ fontSize: '1.125rem', marginBottom: '0.5rem' }}>No clients available</div>
              <div>Clients will appear here once they are added to the system</div>
            </>
          )}
        </div>
      ) : (
        <div>
          {filteredClients.map((client) => (
            <div
              key={client.id}
              style={clientCardStyle}
              onMouseEnter={(e) => {
                Object.assign(e.currentTarget.style, clientCardHoverStyle);
              }}
              onMouseLeave={(e) => {
                Object.assign(e.currentTarget.style, clientCardStyle);
              }}
            >
              <div style={clientNameStyle}>{client.name}</div>
              <div style={clientInfoStyle}>
                <span style={labelStyle}>Email:</span>
                <span>{client.email}</span>
              </div>
              <div style={clientInfoStyle}>
                <span style={labelStyle}>Phone:</span>
                <span>{client.phone}</span>
              </div>
              <div style={clientInfoStyle}>
                <span style={labelStyle}>ID:</span>
                <span style={{ fontFamily: 'monospace', fontSize: '0.8rem' }}>{client.id}</span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default ClientList;
