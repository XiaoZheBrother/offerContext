package com.campus.recruitment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "cities")
public class City {

    @Id
    @Column(name = "city_id")
    private Integer cityId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "initial", length = 10)
    private String initial;

    @Column(length = 255)
    private String pinyin;

    @Column(name = "is_top")
    private Short isTop;

    @Column
    private Integer code;

    @Column
    private Integer weight;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
