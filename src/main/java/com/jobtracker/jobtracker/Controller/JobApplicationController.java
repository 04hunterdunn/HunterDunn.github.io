package com.jobtracker.jobtracker.Controller;

import com.jobtracker.jobtracker.model.JobApplication;
import com.jobtracker.jobtracker.Repository.JobApplicationRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "http://localhost:3000")
public class JobApplicationController {
    private final JobApplicationRepository repo;

    public JobApplicationController(JobApplicationRepository repo) {
        this.repo = repo;
    }

    // Get all job applications
    @GetMapping
    public List<JobApplication> getAllJobs() {
        return repo.findAll();
    }

    // Create a new job application
    @PostMapping
    public JobApplication createJob(@Valid @RequestBody JobApplication job) {
        return repo.save(job);
    }

    @PutMapping("/{id}")
    public JobApplication updateJob(@PathVariable Long id, @RequestBody JobApplication updatedJob) {
        return repo.findById(id)
                .map(job -> {
                    job.setCompanyName(updatedJob.getCompanyName());
                    job.setPosition(updatedJob.getPosition());
                    job.setStatus(updatedJob.getStatus());
                    return repo.save(job);
                })
                .orElseThrow(() -> new RuntimeException("Job not found with id " + id));
    }

    // Delete a job by id
    @DeleteMapping("/{id}")
    public void deleteJob(@PathVariable Long id) {
        repo.deleteById(id);
    }
}
