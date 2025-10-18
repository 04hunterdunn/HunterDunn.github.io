package com.jobtracker.jobtracker.Repository;

import com.jobtracker.jobtracker.model.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long>{

}
