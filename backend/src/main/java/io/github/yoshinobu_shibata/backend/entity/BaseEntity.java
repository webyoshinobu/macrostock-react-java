package io.github.yoshinobu_shibata.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
/**
 * 全Entityが継承する共通親クラス
 *
 * @author  Yoshinobu Shibata
 * @version 1.0
 * @since   2026-08-25
 */
public abstract class BaseEntity {

    // レコード作成日時
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // レコード作成元API
    @Column(name = "created_by_source", updatable = false)
    private String createdBySource;

    // レコード最終更新日時
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // レコード更新元API
    @Column(name = "updated_by_source")
    private String updatedBySource;
}
