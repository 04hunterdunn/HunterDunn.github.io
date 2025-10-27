package com.jobtracker.jobtracker.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "job_applications")
public class JobApplication {

    @NotNull
    private String companyName;

    @NotNull
    private String position;

    @NotNull
    private String status;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public JobApplication() {}

    public JobApplication(String companyName, String position, String status) {
        this.companyName = companyName;
        this.position = position;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
