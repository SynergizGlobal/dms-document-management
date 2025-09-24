package com.synergizglobal.dms.entity.dms;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "SEND_CORRESPONDENCE_LETTER")
@NoArgsConstructor
@AllArgsConstructor
public class SendCorrespondenceLetter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "to_user_id", length = 100)
    private String toUserId;

    @Column(name = "to_user_email", length = 100)
    private String toUserEmail;

    @Column(name = "to_user_name", length = 100)
    private String toUserName;

    @Column(name = "to_dept", length = 100)
    private String toDept;

    @Column(name = "from_user_id", length = 100)
    private String fromUserId;

    @Column(name = "from_user_email", length = 100)
    private String fromUserEmail;

    @Column(name = "from_user_name", length = 100)
    private String fromUserName;

    @Column(name = "is_cc")
    private boolean isCC;

    @Column(name = "from_dept", length = 100)
    private String fromDept;
    
    @Column(name="status")
    private String status;
    
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @JsonIgnore
    @OneToMany(mappedBy = "id", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CorrespondenceFile> files = new ArrayList<>();
    
    
    @ManyToOne
    @JoinColumn(name = "correspondence_id")
    private CorrespondenceLetter correspondenceLetter;
    
}
