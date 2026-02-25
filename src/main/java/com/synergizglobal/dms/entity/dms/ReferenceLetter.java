package com.synergizglobal.dms.entity.dms;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.List;

@Entity
@Table(name = "REFERENCE_LETTER")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReferenceLetter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long refId;

    @Column(name = "ref_letters", length = 100, nullable = false)
    private String refLetters;
   
    @OneToMany(mappedBy = "referenceLetter", cascade = CascadeType.ALL)
    private List<CorrespondenceReference> correspondenceReferences;

    
}
