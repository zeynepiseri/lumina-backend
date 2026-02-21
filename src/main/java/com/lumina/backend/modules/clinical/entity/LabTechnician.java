package com.lumina.backend.modules.clinical.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lumina.backend.modules.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lab_technician")
public class LabTechnician {

    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    @JsonIgnore
    private User user;
    private String department;
    private String employeeId;

    public String getFullName() { return user != null ? user.getFullName() : ""; }
}