package com.synergizglobal.dms.entity.dms;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "CORRESPONDENCE_LETTER")
@NoArgsConstructor
@AllArgsConstructor
public class CorrespondenceLetter {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CORRESPONDENCE_ID")
    private Long correspondenceId;

    @Column(name = "CATEGORY", length = 100)
    private String category;

    @Column(name = "letter_number", length = 200, nullable = false)
    private String letterNumber;

    @Column(name = "LETTER_DATE")
    private LocalDate letterDate;

    @Column(name = "RECIPIENT", length = 200)
    private String to;


    @Column(name = "SUBJECT", length = 500)
    private String subject;


    @Column(name = "CC_RECIPIENT", length = 200)
    private String ccRecipient;


    @Column(name = "KEY_INFORMATION")
    private String keyInformation;

    @Column(name = "REQUIRED_RESPONSE")
    private String requiredResponse;

    @Column(name = "DUE_DATE")
    private LocalDate dueDate;


    @Column(name = "FILE_COUNT")
    private Integer fileCount;

    @Column(name = "CREATED_AT", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "ACTION")
    private String action;

    @Column(name = "department")
    private String department;


    @Column(name = "current_status")
    private String currentStatus;


    @OneToMany(mappedBy = "correspondenceLetter", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<CorrespondenceReference> correspondenceReferences;
    @JsonIgnore
    @OneToMany(mappedBy = "correspondenceLetter", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CorrespondenceFile> files = new ArrayList<>();
}
