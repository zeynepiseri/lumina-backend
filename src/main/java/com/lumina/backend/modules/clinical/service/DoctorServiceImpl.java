package com.lumina.backend.modules.clinical.service;

import com.lumina.backend.modules.appointment.entity.Appointment;
import com.lumina.backend.modules.auth.dto.ChangePasswordRequest;
import com.lumina.backend.modules.auth.entity.Role;
import com.lumina.backend.modules.auth.entity.User;
import com.lumina.backend.modules.clinical.dto.DoctorRegisterRequest;
import com.lumina.backend.modules.clinical.dto.DoctorRequest;
import com.lumina.backend.modules.clinical.dto.DoctorResponse;
import com.lumina.backend.modules.clinical.dto.ScheduleRequest;
import com.lumina.backend.modules.clinical.entity.Doctor;
import com.lumina.backend.modules.clinical.entity.DoctorSchedule;
import com.lumina.backend.modules.operations.entity.Branch;
import com.lumina.backend.shared.exception.ResourceNotFoundException;
import com.lumina.backend.modules.appointment.repository.AppointmentRepository;
import com.lumina.backend.modules.operations.repository.BranchRepository;
import com.lumina.backend.modules.clinical.repository.DoctorRepository;
import com.lumina.backend.modules.auth.repository.UserRepository;
import com.lumina.backend.modules.clinical.dto.ScheduleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppointmentRepository appointmentRepository;

    @Override
    public List<DoctorResponse> getAllDoctors(Long branchId) {
        List<Doctor> doctors;
        if (branchId != null) {
            doctors = doctorRepository.findByBranchId(branchId);
        } else {
            doctors = doctorRepository.findAll();
        }
        return doctors.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public DoctorResponse getDoctorById(Long id) {
        Doctor doctor = findDoctorEntityById(id);
        return mapToResponse(doctor);
    }

    @Override
    public Doctor findDoctorEntityById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
    }

    @Override
    public List<Appointment> getDoctorAppointmentsByDate(Long doctorId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        return appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctorId, startOfDay, endOfDay);
    }

    @Override
    public DoctorResponse createDoctor(DoctorRegisterRequest request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }
        if (request.getBranchId() == null) {
            throw new IllegalArgumentException("Branch ID is mandatory");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.DOCTOR)
                .nationalId(request.getNationalId())
                .phoneNumber(request.getPhoneNumber())
                .gender(request.getGender())     
                .imageUrl(request.getImageUrl()) 
                .build();

        user = userRepository.save(user);

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        Doctor doctor = Doctor.builder()
                .user(user)
                .branch(branch)
                .title(request.getTitle())
                .specialty(request.getSpecialty())
                .diplomaNo(request.getDiplomaNo())
                .biography(request.getBiography())   
                .experience(request.getExperience() != null ? request.getExperience() : 0) 
                .subSpecialties(request.getSubSpecialties())
                .professionalExperiences(request.getProfessionalExperiences())
                .educations(request.getEducations())
                .acceptedInsurances(request.getAcceptedInsurances())
                .certificates(request.getCertificates())
                .languages(request.getLanguages())
                .patientCount(0)
                .rating(0.0)
                .reviewCount(0)
                .schedules(new ArrayList<>())
                .build();

        Doctor savedDoctor = doctorRepository.save(doctor);

        if (request.getSchedules() != null && !request.getSchedules().isEmpty()) {
            updateSchedules(savedDoctor, request.getSchedules());
            savedDoctor = doctorRepository.save(savedDoctor);
        }

        return mapToResponse(savedDoctor);
    }

    @Override
    public DoctorResponse updateDoctor(Long id, DoctorRequest request) {
        Doctor doctor = findDoctorEntityById(id);
        User user = doctor.getUser();

        if (user != null) {
            if (request.getFullName() != null) user.setFullName(request.getFullName());
            if (request.getEmail() != null && !request.getEmail().trim().isEmpty() && !request.getEmail().equals(user.getEmail())) {
                if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                    throw new IllegalArgumentException("Email already in use");
                }
                user.setEmail(request.getEmail());
            }
            if (request.getImageUrl() != null) user.setImageUrl(request.getImageUrl());
            if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
            if (request.getNationalId() != null) user.setNationalId(request.getNationalId());
            if (request.getGender() != null) user.setGender(request.getGender());
            if (request.getBirthDate() != null) user.setBirthDate(request.getBirthDate());
            userRepository.save(user);
        }

        if (request.getTitle() != null) doctor.setTitle(request.getTitle());
        if (request.getSpecialty() != null) doctor.setSpecialty(request.getSpecialty());
        if (request.getBiography() != null) doctor.setBiography(request.getBiography());
        if (request.getDiplomaNo() != null) doctor.setDiplomaNo(request.getDiplomaNo());
        if (request.getExperience() != null) doctor.setExperience(request.getExperience());
         if (request.getPatients() != null) {
            try { doctor.setPatientCount(Integer.parseInt(request.getPatients())); } catch (NumberFormatException ignored) {}
        }
        if (request.getReviews() != null) {
             try { doctor.setRating(Double.parseDouble(request.getReviews())); } catch (NumberFormatException ignored) {}
        }

        if (request.getBranchId() != null) {
            Branch branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
            doctor.setBranch(branch);
        }

        if (request.getSubSpecialties() != null) doctor.setSubSpecialties(request.getSubSpecialties());
        if (request.getProfessionalExperiences() != null) doctor.setProfessionalExperiences(request.getProfessionalExperiences());
        if (request.getEducations() != null) doctor.setEducations(request.getEducations());
        if (request.getAcceptedInsurances() != null) doctor.setAcceptedInsurances(request.getAcceptedInsurances());
         if (request.getCertificates() != null) doctor.setCertificates(request.getCertificates());
        if (request.getLanguages() != null) doctor.setLanguages(request.getLanguages());

        updateSchedules(doctor, request.getSchedules());

        Doctor updatedDoctor = doctorRepository.save(doctor);
        return mapToResponse(updatedDoctor);
    }

    private void updateSchedules(Doctor doctor, List<ScheduleRequest> scheduleRequests) {
        if (scheduleRequests == null) return;
        doctor.getSchedules().clear();
        for (ScheduleRequest sch : scheduleRequests) {
            try {
                DoctorSchedule schedule = new DoctorSchedule();
                schedule.setDayOfWeek(DayOfWeek.valueOf(sch.getDayOfWeek().toUpperCase()));
                schedule.setStartTime(LocalTime.parse(sch.getStartTime()));
                schedule.setEndTime(LocalTime.parse(sch.getEndTime()));
                schedule.setDoctor(doctor);
                doctor.getSchedules().add(schedule);
            } catch (Exception e) {
             }
        }
    }

    @Override
    public void deleteDoctor(Long id) {
        Doctor doctor = findDoctorEntityById(id);
        User user = doctor.getUser();
        doctorRepository.delete(doctor);
        if (user != null) {
            userRepository.delete(user);
        }
    }

    @Override
    public void validateDoctorOwnership(Long doctorId, String email) {
        Doctor doctor = findDoctorEntityById(doctorId);
        if (doctor.getUser() == null || !doctor.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized");
        }
    }
     private DoctorResponse mapToResponse(Doctor doctor) {
        return DoctorResponse.builder()
                .id(doctor.getId())
                .fullName(doctor.getFullName())
                .title(doctor.getTitle())
                .specialty(doctor.getSpecialty())
                .imageUrl(doctor.getImageUrl())
                .branchName(doctor.getBranch() != null ? doctor.getBranch().getName() : "")
                .branchId(doctor.getBranch() != null ? doctor.getBranch().getId() : null)
                .biography(doctor.getBiography())
                 .rating(doctor.getRating())
                .reviewCount(doctor.getReviewCount())
                .patientCount(doctor.getPatientCount())

                .experience(doctor.getExperience())
                .diplomaNo(doctor.getDiplomaNo())
                .consultationFee(doctor.getConsultationFee())
                .email(doctor.getEmail())            
                .phoneNumber(doctor.getPhoneNumber())  
                .nationalId(doctor.getNationalId())    
                .gender(doctor.getGender())            
                .birthDate(doctor.getBirthDate())
                .subSpecialties(doctor.getSubSpecialties())
                .professionalExperiences(doctor.getProfessionalExperiences())
                .educations(doctor.getEducations())
                .acceptedInsurances(doctor.getAcceptedInsurances())
                 .certificates(doctor.getCertificates())
                .languages(doctor.getLanguages())

                .schedules(doctor.getSchedules().stream()
                        .map(sch -> new ScheduleResponse(
                                sch.getDayOfWeek().name(),
                                sch.getStartTime().toString(),
                                sch.getEndTime().toString()
                        ))
                        .collect(Collectors.toList()))

                .build();
    }

    @Override
    public void changePassword(Long doctorId, ChangePasswordRequest request) {
        Doctor doctor = findDoctorEntityById(doctorId);
        User user = doctor.getUser();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("The current password is incorrect!");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}