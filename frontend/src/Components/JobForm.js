import React, { useState } from 'react';
import axios from 'axios';

function JobForm({ onJobAdded }) {
  const [companyName, setCompanyName] = useState('');
  const [position, setPosition] = useState('');
  const [status, setStatus] = useState('Applied'); // default value

  const handleSubmit = (e) => {
    e.preventDefault();

    const newJob = { companyName, position, status };

    axios.post('http://localhost:8080/api/jobs', newJob)
      .then(response => {
        onJobAdded(response.data); // notify parent component
        setCompanyName('');
        setPosition('');
        setStatus('Applied'); // reset to default
      })
      .catch(error => console.error('Error adding job:', error));
  };

  return (
    <form onSubmit={handleSubmit} style={{ marginBottom: '20px' }}>
      <h2>Add Job Application</h2>
      <div style={{ marginBottom: '10px' }}>
        <label>Company Name: </label>
        <input
          type="text"
          value={companyName}
          onChange={(e) => setCompanyName(e.target.value)}
          required
        />
      </div>
      <div style={{ marginBottom: '10px' }}>
        <label>Position: </label>
        <input
          type="text"
          value={position}
          onChange={(e) => setPosition(e.target.value)}
          required
        />
      </div>
      <div style={{ marginBottom: '10px' }}>
        <label>Status: </label>
        <select
          value={status}
          onChange={(e) => setStatus(e.target.value)}
          required
        >
          <option value="Applied">Applied</option>
          <option value="Interview">Interview</option>
          <option value="Offered">Offered</option>
          <option value="Rejected">Rejected</option>
        </select>
      </div>
      <button type="submit">Add Job</button>
    </form>
  );
}

export default JobForm;