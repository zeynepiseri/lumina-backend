package com.lumina.backend.modules.patient.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lumina.backend.modules.auth.entity.User;
import com.lumina.backend.modules.medicalrecord.entity.MedicalResult;
import com.lumina.backend.modules.medicalrecord.entity.Prescription;
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
@Table(name = "patient")
public class Patient {

    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    @JsonIgnore
    private User user;

    private Double height;
    private Double weight;
    private String bloodType;

    @ElementCollection
    @CollectionTable(name = "patient_allergies", joinColumns = @JoinColumn(name = "patient_id"))
    @Column(name = "allergy")
    @Builder.Default
    private List<String> allergies = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "patient_chronic_diseases", joinColumns = @JoinColumn(name = "patient_id"))
    @Column(name = "disease")
    @Builder.Default
    private List<String> chronicDiseases = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "patient_medications", joinColumns = @JoinColumn(name = "patient_id"))
    @Column(name = "medication")
    @Builder.Default
    private List<String> medications = new ArrayList<>();

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<Prescription> prescriptions = new ArrayList<>();

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<MedicalResult> medicalResults = new ArrayList<>();

    public String getFullName() { return user != null ? user.getFullName() : ""; }
    public String getNationalId() { return user != null ? user.getNationalId() : ""; }
    public String getGender() { return user != null ? user.getGender() : ""; }
    public String getBirthDate() { return user != null && user.getBirthDate() != null ? user.getBirthDate().toString() : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Patient patient = (Patient) o;
        return id != null && Objects.equals(id, patient.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public String getEmail() {
        return user != null ? user.getEmail() : null;
    }
}