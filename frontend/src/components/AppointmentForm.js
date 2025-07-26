import React, { useState, useEffect } from 'react';
import { createAppointment, fetchClients } from '../utils/api';

const AppointmentForm = ({ onAppointmentCreated }) => {
  const [clients, setClients] = useState([]);
  const [formData, setFormData] = useState({
    clientId: '',
    time: '',
    notes: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [loadingClients, setLoadingClients] = useState(true);

  useEffect(() => {
    loadClients();
  }, []);

  const loadClients = async () => {
    try {
      setLoadingClients(true);
      const data = await fetchClients();
      setClients(data);
      if (data.length > 0) {
        setFormData(prev => ({ ...prev, clientId: data[0].id }));
      }
    } catch (err) {
      setError('Failed to load clients. Please refresh the page.');
      console.error('Error loading clients:', err);
    } finally {
      setLoadingClients(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    
    // Clear messages when user starts typing
    if (error) setError(null);
    if (success) setSuccess(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!formData.clientId || !formData.time) {
      setError('Please fill in all required fields.');
      return;
    }

    // Validate that the appointment time is in the future
    const appointmentTime = new Date(formData.time);
    const now = new Date();
    if (appointmentTime <= now) {
      setError('Appointment time must be in the future.');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      setSuccess(null);

      const appointmentData = {
        clientId: formData.clientId,
        time: appointmentTime.toISOString().slice(0, 19),
        notes: formData.notes.trim() || null
      };
      
      const createdAppointment = await createAppointment(appointmentData);
      
      setSuccess('Appointment scheduled successfully!');
      
      // Reset form
      setFormData({
        clientId: clients.length > 0 ? clients[0].id : '',
        time: '',
        notes: ''
      });

      // Notify parent component if callback provided
      if (onAppointmentCreated) {
        onAppointmentCreated(createdAppointment);
      }

    } catch (err) {
      if (err.message.includes('already booked')) {
        setError('This time slot is already booked. Please choose a different time.');
      } else if (err.message.includes('400')) {
        setError('Invalid appointment data. Please check your inputs.');
      } else {
        setError('Failed to schedule appointment. Please try again.');
      }
      console.error('Error creating appointment:', err);
    } finally {
      setLoading(false);
    }
  };

  const getMinDateTime = () => {
    const now = new Date();
    now.setMinutes(now.getMinutes() + 30); // Minimum 30 minutes from now
    return now.toISOString().slice(0, 16);
  };

  const containerStyle = {
    backgroundColor: '#ffffff',
    borderRadius: '8px',
    boxShadow: '0 1px 3px 0 rgba(0, 0, 0, 0.1)',
    padding: '1.5rem',
    margin: '1rem',
  };

  const titleStyle = {
    fontSize: '1.5rem',
    fontWeight: '600',
    color: '#1e293b',
    marginBottom: '1.5rem',
  };

  const formStyle = {
    display: 'flex',
    flexDirection: 'column',
    gap: '1rem',
  };

  const formGroupStyle = {
    display: 'flex',
    flexDirection: 'column',
    gap: '0.5rem',
  };

  const labelStyle = {
    fontSize: '0.875rem',
    fontWeight: '600',
    color: '#374151',
  };

  const inputStyle = {
    padding: '0.75rem',
    border: '1px solid #d1d5db',
    borderRadius: '6px',
    fontSize: '0.875rem',
    outline: 'none',
    transition: 'border-color 0.2s',
  };

  const selectStyle = {
    ...inputStyle,
    backgroundColor: '#ffffff',
  };

  const textareaStyle = {
    ...inputStyle,
    minHeight: '80px',
    resize: 'vertical',
  };

  const buttonStyle = {
    backgroundColor: '#10b981',
    color: 'white',
    border: 'none',
    borderRadius: '6px',
    padding: '0.75rem 1.5rem',
    fontSize: '0.875rem',
    fontWeight: '600',
    cursor: 'pointer',
    transition: 'background-color 0.2s',
    opacity: loading ? 0.6 : 1,
    marginTop: '0.5rem',
  };

  const errorStyle = {
    padding: '0.75rem',
    backgroundColor: '#fef2f2',
    border: '1px solid #fecaca',
    borderRadius: '6px',
    color: '#dc2626',
    fontSize: '0.875rem',
  };

  const successStyle = {
    padding: '0.75rem',
    backgroundColor: '#f0fdf4',
    border: '1px solid #bbf7d0',
    borderRadius: '6px',
    color: '#166534',
    fontSize: '0.875rem',
  };

  const loadingStyle = {
    textAlign: 'center',
    padding: '2rem',
    color: '#64748b',
  };

  const requiredStyle = {
    color: '#dc2626',
  };

  if (loadingClients) {
    return (
      <div style={containerStyle}>
        <div style={loadingStyle}>
          <div>Loading form...</div>
        </div>
      </div>
    );
  }

  if (clients.length === 0) {
    return (
      <div style={containerStyle}>
        <h2 style={titleStyle}>Schedule New Appointment</h2>
        <div style={errorStyle}>
          No clients available. Please add clients first before scheduling appointments.
        </div>
      </div>
    );
  }

  return (
    <div style={containerStyle}>
      <h2 style={titleStyle}>Schedule New Appointment</h2>
      
      {error && <div style={errorStyle}>{error}</div>}
      {success && <div style={successStyle}>{success}</div>}
      
      <form onSubmit={handleSubmit} style={formStyle}>
        <div style={formGroupStyle}>
          <label htmlFor="clientId" style={labelStyle}>
            Client <span style={requiredStyle}>*</span>
          </label>
          <select
            id="clientId"
            name="clientId"
            value={formData.clientId}
            onChange={handleInputChange}
            style={selectStyle}
            required
            onFocus={(e) => e.target.style.borderColor = '#3b82f6'}
            onBlur={(e) => e.target.style.borderColor = '#d1d5db'}
          >
            <option value="">Select a client</option>
            {clients.map((client) => (
              <option key={client.id} value={client.id}>
                {client.name} - {client.email}
              </option>
            ))}
          </select>
        </div>

        <div style={formGroupStyle}>
          <label htmlFor="time" style={labelStyle}>
            Appointment Date & Time <span style={requiredStyle}>*</span>
          </label>
          <input
            type="datetime-local"
            id="time"
            name="time"
            value={formData.time}
            onChange={handleInputChange}
            min={getMinDateTime()}
            style={inputStyle}
            required
            onFocus={(e) => e.target.style.borderColor = '#3b82f6'}
            onBlur={(e) => e.target.style.borderColor = '#d1d5db'}
          />
          <div style={{ fontSize: '0.75rem', color: '#64748b' }}>
            Appointment must be scheduled at least 30 minutes in advance
          </div>
        </div>

        <div style={formGroupStyle}>
          <label htmlFor="notes" style={labelStyle}>
            Notes (Optional)
          </label>
          <textarea
            id="notes"
            name="notes"
            value={formData.notes}
            onChange={handleInputChange}
            placeholder="Add any additional notes for this appointment..."
            style={textareaStyle}
            onFocus={(e) => e.target.style.borderColor = '#3b82f6'}
            onBlur={(e) => e.target.style.borderColor = '#d1d5db'}
          />
        </div>

        <button
          type="submit"
          disabled={loading}
          style={buttonStyle}
          onMouseOver={(e) => !e.target.disabled && (e.target.style.backgroundColor = '#059669')}
          onMouseOut={(e) => !e.target.disabled && (e.target.style.backgroundColor = '#10b981')}
        >
          {loading ? 'Scheduling...' : 'Schedule Appointment'}
        </button>
      </form>
    </div>
  );
};

export default AppointmentForm;
