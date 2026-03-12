package com.celine.onlineticketmanagementserver.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "persons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = true)
    private String phone;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(nullable = false)
    private Boolean accountNonExpired = true;

    @Column(nullable = false)
    private Boolean accountNonLocked = true;

    @Column(nullable = false)
    private Boolean credentialsNonExpired = true;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date registeredAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastLogin;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    private Integer failedLoginAttempts = 0;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lockTime;

    @Column(length = 500)
    private String refreshToken;

    @Temporal(TemporalType.TIMESTAMP)
    private Date refreshTokenExpiryDate;

    // Password reset token fields
    @Column(length = 100)
    private String passwordResetToken;

    @Temporal(TemporalType.TIMESTAMP)
    private Date passwordResetTokenExpiry;

    // Email verification fields
    @Column(length = 100)
    private String emailVerificationToken;

    @Temporal(TemporalType.TIMESTAMP)
    private Date emailVerificationTokenExpiry;

    @Column(nullable = false)
    private Boolean emailVerified = false;

    // Two-Factor Authentication fields
    @Column(nullable = false)
    private Boolean twoFactorEnabled = false;

    @Column(length = 32)
    private String twoFactorSecret; // Secret key for Google Authenticator (TOTP)

    @Column(length = 20)
    private String twoFactorMethod; // "EMAIL" or "AUTHENTICATOR"

    @Column(length = 10)
    private String emailOtpCode; // Temporary OTP code for email-based 2FA

    @Temporal(TemporalType.TIMESTAMP)
    private Date emailOtpExpiry; // Expiry time for email OTP

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "person_roles",
            joinColumns = @JoinColumn(name = "person_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @JsonIgnoreProperties("persons")
    private Set<Role> roles = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    @JsonIgnoreProperties({"parentLocation", "childLocations", "residents"})
    private Location livesIn;

    @OneToMany(mappedBy = "bookedBy", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("bookedBy")
    private Set<Booking> bookings = new HashSet<>();

    @OneToOne(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private PersonProfile profile;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "venue_managers",
            joinColumns = @JoinColumn(name = "person_id"),
            inverseJoinColumns = @JoinColumn(name = "venue_id")
    )
    @JsonIgnoreProperties({"managers", "events"})
    private Set<Venue> managesVenues = new HashSet<>();

    @OneToMany(mappedBy = "person", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("person")
    private Set<Notification> notifications = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        if (registeredAt == null) {
            registeredAt = new Date();
        }
        updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
}
