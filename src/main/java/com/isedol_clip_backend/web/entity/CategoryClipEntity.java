package com.isedol_clip_backend.web.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Getter
@Setter
//@ToString
@Entity
@Table(name = "CATEGORY_CLIP")
public class CategoryClipEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "CATEGORY_ID")
    @ToString.Exclude
    private CategoryEntity category;

    @Column(name = "CLIP_ID", length = 100)
    private String clipId;
}
