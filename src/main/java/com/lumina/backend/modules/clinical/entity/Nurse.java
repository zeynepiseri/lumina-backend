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
@Table(name = "nurse")
public class Nurse {

    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    @JsonIgnore
    private User user;

    private String department;
    private String shiftType;  // Ex: Night, Day, Rotating
    private String employeeId;

    public String getFullName() { return user != null ? user.getFullName() : ""; }
}