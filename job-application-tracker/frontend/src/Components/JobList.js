import React from 'react';

const JobList = ({ jobs, onDelete, onUpdate }) => {
  return (
    <div>
      <h2>Job Applications</h2>
      {jobs.length === 0 ? (
        <p>No job applications yet.</p>
      ) : (
        <ul>
          {jobs.map(job => (
            <li key={job.id} style={{ marginBottom: '10px' }}>
              <strong>{job.companyName}</strong> - {job.position} ({job.status})

              {/* Delete Button */}
              <button 
                onClick={() => onDelete(job.id)} 
                style={{ marginLeft: '10px' }}
              >
                Delete
              </button>

              {/* Status Dropdown */}
              <select 
                value={job.status} 
                onChange={(e) => onUpdate(job.id, e.target.value)}
                style={{ marginLeft: '10px' }}
              >
                <option value="Applied">Applied</option>
                <option value="Interview">Interview</option>
                <option value="Offered">Offered</option>
                <option value="Rejected">Rejected</option>
              </select>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

export default JobList;