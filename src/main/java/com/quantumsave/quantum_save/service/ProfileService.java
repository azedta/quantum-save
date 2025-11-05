package com.quantumsave.quantum_save.service;


import com.quantumsave.quantum_save.dto.ProfileDTO;
import com.quantumsave.quantum_save.entity.ProfileEntity;
import com.quantumsave.quantum_save.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {


    private final ProfileRepository profileRepository;
    private final EmailService emailService;

    public ProfileDTO registerProfile(ProfileDTO profileDTO) {

        ProfileEntity newProfile = toEntity(profileDTO);
        newProfile.setActivationToken(UUID.randomUUID().toString());
        newProfile = profileRepository.save(newProfile);

        // Send Activation Email
        String activationLink = "http://localhost:8080/api/v1.0/activate?token=" + newProfile.getActivationToken();
        String subject = "Activate Your Quantum Save Account";

        String body = "Hello " + newProfile.getFullName() + ",\n\n" +
                "Welcome to Quantum Save — we're excited to have you on board!\n\n" +
                "To complete your registration and start tracking your expenses, please activate your account by clicking the link below:\n\n" +
                activationLink + "\n\n" +
                "If you didn’t create an account with Quantum Save, please ignore this email.\n\n" +
                "Thank you,\n" +
                "The Quantum Save Team";

        emailService.sendEmail(newProfile.getEmail(), subject, body);

        return toDTO(newProfile);
    }



    // This is a helper method that converts a profileDTO to a profileEntity
    public ProfileEntity toEntity(ProfileDTO profileDTO) {
        return ProfileEntity.builder()
                .id(profileDTO.getId())
                .fullName(profileDTO.getFullName())
                .email(profileDTO.getEmail())
                .password(profileDTO.getPassword())
                .profileImageUrl(profileDTO.getProfileImageUrl())
                .createdAt(profileDTO.getCreatedAt())
                .updatedAt(profileDTO.getUpdatedAt())
                .build();
    }

    // This is a helper method that converts a profileEntity to a profileDTO
    public ProfileDTO toDTO(ProfileEntity profileEntity) {
        return ProfileDTO.builder()
                .id(profileEntity.getId())
                .fullName(profileEntity.getFullName())
                .email(profileEntity.getEmail())
                .profileImageUrl(profileEntity.getProfileImageUrl())
                .createdAt(profileEntity.getCreatedAt())
                .updatedAt(profileEntity.getUpdatedAt())
                .build();
    }

    public boolean activateProfile(String activationToken) {
        return profileRepository.findByActivationToken(activationToken)
                .map(profile -> {
                    profile.setIsActive(true);
                    profileRepository.save(profile);
                    return true;
                }).orElse(false);
    }
}
