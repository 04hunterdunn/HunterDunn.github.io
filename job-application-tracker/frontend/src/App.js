import React, { useState, useEffect } from 'react';
import axios from 'axios';
import Greeting from './Components/Welcome.js';
import JobList from './Components/JobList.js';
import JobForm from './Components/JobForm';

function App() {
    const [jobs, setJobs] = useState([]);

    // Fetch jobs from backend on page load
    useEffect(() => {
        axios.get('http://localhost:8080/api/jobs')
            .then(response => setJobs(response.data))
            .catch(error => console.error('Error fetching jobs:', error));
    }, []);

    // Add a new job
    const handleJobAdded = (newJob) => {
        setJobs([...jobs, newJob]);
    };

    // Delete a job
    const handleDelete = (id) => {
        axios.delete(`http://localhost:8080/api/jobs/${id}`)
            .then(() => setJobs(jobs.filter(job => job.id !== id)))
            .catch(error => console.error("Error deleting job:", error));
    };

    // Update a job's status
    const handleUpdate = (id, newStatus) => {
        const jobToUpdate = jobs.find(job => job.id === id);
        if (!jobToUpdate) return;

        const updatedJob = { ...jobToUpdate, status: newStatus };

        axios.put(`http://localhost:8080/api/jobs/${id}`, updatedJob)
            .then(response => {
                setJobs(jobs.map(job => job.id === id ? response.data : job));
            })
            .catch(error => console.error("Error updating job:", error));
    };

    return (
        <div>
            <Greeting name="Hunter"/>
            <JobForm onJobAdded={handleJobAdded} />
            <JobList 
                jobs={jobs} 
                setJobs={setJobs} 
                onDelete={handleDelete} 
                onUpdate={handleUpdate} 
            />
        </div>
    );
}

export default App;