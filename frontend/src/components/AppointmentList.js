import React, { useState, useEffect } from 'react';
import { fetchUpcomingAppointments, fetchClients, cancelAppointment } from '../utils/api';
import { formatDateTime } from '../utils/api';

const AppointmentList = () => {
  const [appointments, setAppointments] = useState([]);
  const [clients, setClients] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [cancellingId, setCancellingId] = useState(null);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // Load both appointments and clients
      const [appointmentsData, clientsData] = await Promise.all([
        fetchUpcomingAppointments(),
        fetchClients()
      ]);
      
      setAppointments(appointmentsData);
      setClients(clientsData);
    } catch (err) {
      setError('Failed to load appointments. Please try again.');
      console.error('Error loading appointments:', err);
    } finally {
      setLoading(false);
    }
  };

  const getClientName = (clientId) => {
    const client = clients.find(c => c.id === clientId);
    return client ? client.name : `Client ID: ${clientId}`;
  };

  const getClientEmail = (clientId) => {
    const client = clients.find(c => c.id === clientId);
    return client ? client.email : '';
  };

  const handleCancelAppointment = async (appointmentId) => {
    if (!window.confirm('Are you sure you want to cancel this appointment?')) {
      return;
    }

    try {
      setCancellingId(appointmentId);
      await cancelAppointment(appointmentId);
      
      // Refresh the appointments list
      await loadData();
      
      alert('Appointment cancelled successfully');
    } catch (err) {
      console.error('Error cancelling appointment:', err);
      alert('Failed to cancel appointment. Please try again.');
    } finally {
      setCancellingId(null);
    }
  };

  const getStatusColor = (status) => {
    switch (status?.toLowerCase()) {
      case 'scheduled':
        return '#3b82f6';
      case 'confirmed':
        return '#10b981';
      case 'completed':
        return '#6b7280';
      case 'cancelled':
        return '#ef4444';
      case 'no_show':
        return '#f59e0b';
      default:
        return '#6b7280';
    }
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

  const appointmentCardStyle = {
    border: '1px solid #e5e7eb',
    borderRadius: '6px',
    padding: '1.25rem',
    marginBottom: '1rem',
    transition: 'all 0.2s',
  };

  const appointmentCardHoverStyle = {
    ...appointmentCardStyle,
    borderColor: '#3b82f6',
    boxShadow: '0 2px 4px 0 rgba(0, 0, 0, 0.1)',
  };

  const appointmentHeaderStyle = {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: '0.75rem',
  };

  const clientNameStyle = {
    fontSize: '1.125rem',
    fontWeight: '600',
    color: '#1e293b',
    marginBottom: '0.25rem',
  };

  const appointmentTimeStyle = {
    fontSize: '1rem',
    fontWeight: '500',
    color: '#059669',
    marginBottom: '0.5rem',
  };

  const appointmentInfoStyle = {
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

  const statusBadgeStyle = (status) => ({
    display: 'inline-block',
    padding: '0.25rem 0.75rem',
    borderRadius: '9999px',
    fontSize: '0.75rem',
    fontWeight: '500',
    color: 'white',
    backgroundColor: getStatusColor(status),
    textTransform: 'uppercase',
    letterSpacing: '0.05em',
  });

  const buttonStyle = {
    padding: '0.5rem 1rem',
    borderRadius: '6px',
    fontSize: '0.875rem',
    fontWeight: '500',
    cursor: 'pointer',
    transition: 'all 0.2s',
    border: 'none',
    marginLeft: '0.5rem',
  };

  const cancelButtonStyle = {
    ...buttonStyle,
    backgroundColor: '#ef4444',
    color: 'white',
  };

  const refreshButtonStyle = {
    ...buttonStyle,
    backgroundColor: '#3b82f6',
    color: 'white',
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

  const countStyle = {
    fontSize: '0.875rem',
    color: '#64748b',
    marginBottom: '1rem',
  };

  if (loading) {
    return (
      <div style={containerStyle}>
        <div style={loadingStyle}>
          <div>Loading appointments...</div>
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
            onClick={loadData}
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
        <h2 style={titleStyle}>Upcoming Appointments</h2>
        <button 
          style={refreshButtonStyle}
          onClick={loadData}
          onMouseOver={(e) => e.target.style.backgroundColor = '#2563eb'}
          onMouseOut={(e) => e.target.style.backgroundColor = '#3b82f6'}
        >
          Refresh
        </button>
      </div>

      <div style={countStyle}>
        Total upcoming appointments: {appointments.length}
      </div>

      {appointments.length === 0 ? (
        <div style={emptyStateStyle}>
          <div style={{ fontSize: '1.125rem', marginBottom: '0.5rem' }}>No upcoming appointments</div>
          <div>New appointments will appear here</div>
        </div>
      ) : (
        <div>
          {appointments.map((appointment) => (
            <div
              key={appointment.id}
              style={appointmentCardStyle}
              onMouseEnter={(e) => {
                Object.assign(e.currentTarget.style, appointmentCardHoverStyle);
              }}
              onMouseLeave={(e) => {
                Object.assign(e.currentTarget.style, appointmentCardStyle);
              }}
            >
              <div style={appointmentHeaderStyle}>
                <div>
                  <div style={clientNameStyle}>{getClientName(appointment.clientId)}</div>
                  <div style={appointmentTimeStyle}>
                    {formatDateTime(appointment.time)}
                  </div>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <span style={statusBadgeStyle(appointment.status)}>
                    {appointment.status || 'SCHEDULED'}
                  </span>
                  {appointment.status !== 'CANCELLED' && appointment.status !== 'COMPLETED' && (
                    <button
                      style={cancelButtonStyle}
                      onClick={() => handleCancelAppointment(appointment.id)}
                      disabled={cancellingId === appointment.id}
                      onMouseOver={(e) => e.target.style.backgroundColor = '#dc2626'}
                      onMouseOut={(e) => e.target.style.backgroundColor = '#ef4444'}
                    >
                      {cancellingId === appointment.id ? 'Cancelling...' : 'Cancel'}
                    </button>
                  )}
                </div>
              </div>

              <div style={appointmentInfoStyle}>
                <span style={labelStyle}>Email:</span>
                <span>{getClientEmail(appointment.clientId)}</span>
              </div>
              
              <div style={appointmentInfoStyle}>
                <span style={labelStyle}>Client ID:</span>
                <span style={{ fontFamily: 'monospace', fontSize: '0.8rem' }}>
                  {appointment.clientId}
                </span>
              </div>
              
              <div style={appointmentInfoStyle}>
                <span style={labelStyle}>Appt ID:</span>
                <span style={{ fontFamily: 'monospace', fontSize: '0.8rem' }}>
                  {appointment.id}
                </span>
              </div>

              {appointment.notes && (
                <div style={appointmentInfoStyle}>
                  <span style={labelStyle}>Notes:</span>
                  <span>{appointment.notes}</span>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default AppointmentList;
