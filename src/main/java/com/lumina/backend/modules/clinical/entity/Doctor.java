package com.lumina.backend.modules.clinical.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lumina.backend.modules.appointment.entity.Appointment;
import com.lumina.backend.modules.auth.entity.User;
import com.lumina.backend.modules.operations.entity.Branch;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "doctor")
public class Doctor {

    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    @JsonIgnore
    private User user;

    private String title;
    private String specialty;

    @Column(unique = true)
    private String diplomaNo;

    @Column(columnDefinition = "TEXT")
    private String biography;

    private Integer experience;
     @Builder.Default
    private Integer patientCount = 0;
    @Builder.Default
    private Double rating = 0.0;
    @Builder.Default
    private Integer reviewCount = 0;
    private Double consultationFee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<Appointment> appointments = new ArrayList<>();

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DoctorSchedule> schedules = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "doctor_sub_specialties", joinColumns = @JoinColumn(name = "doctor_id"))
    @Column(name = "sub_specialties")
    @Builder.Default
    private List<String> subSpecialties = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "doctor_professional_experiences", joinColumns = @JoinColumn(name = "doctor_id"))
    @Column(name = "professional_experiences")
    @Builder.Default
    private List<String> professionalExperiences = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "doctor_educations", joinColumns = @JoinColumn(name = "doctor_id"))
    @Column(name = "educations")
    @Builder.Default
    private List<String> educations = new ArrayList<>();
     @ElementCollection
    @CollectionTable(name = "doctor_certificates", joinColumns = @JoinColumn(name = "doctor_id"))
    @Column(name = "certificate")
    @Builder.Default
    private List<String> certificates = new ArrayList<>();
     @ElementCollection
    @CollectionTable(name = "doctor_languages", joinColumns = @JoinColumn(name = "doctor_id"))
    @Column(name = "language")
    @Builder.Default
    private List<String> languages = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "doctor_insurances", joinColumns = @JoinColumn(name = "doctor_id"))
    @Column(name = "insurance")
    @Builder.Default
    private List<String> acceptedInsurances = new ArrayList<>();

    public String getFullName() { return user != null ? user.getFullName() : ""; }
    public String getPhoneNumber() { return user != null ? user.getPhoneNumber() : ""; }
    public String getEmail() { return user != null ? user.getEmail() : ""; }
    public String getNationalId() { return user != null ? user.getNationalId() : ""; }
    public String getImageUrl() { return user != null ? user.getImageUrl() : null; }
    public String getGender() { return user != null ? user.getGender() : null; }
    public String getBirthDate() { return user != null && user.getBirthDate() != null ? user.getBirthDate().toString() : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Doctor doctor = (Doctor) o;
        return id != null && Objects.equals(id, doctor.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}