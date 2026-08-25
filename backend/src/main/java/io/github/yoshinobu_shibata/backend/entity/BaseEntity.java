package io.github.yoshinobu_shibata.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 全Entityの共通親クラス。
 * created_at / created_by_source / updated_at / updated_by_source の
 * 4カラムをまとめて提供する。
 *
 * これを継承するだけで、各テーブル(users, photos, orders...)に
 * 同じ監査用カラムを重複記述せずに持たせられる。
 */
@Getter
@Setter
// このクラス自体はテーブルを持たない。継承先のEntityにカラムだけを提供する「部品」であることを示す
@MappedSuperclass
// 保存・更新のタイミングでAuditing機能(下記の@CreatedDate等)を発動させるためのリスナー登録
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    // レコード作成日時。保存(INSERT)時に自動でセットされる
    @CreatedDate
    // updatable = false:一度セットされたら、後から更新処理が走っても上書きされないようにする
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // レコードを作成したAPIエンドポイント(例: "POST /api/photos")。保存時に自動でセットされる
    // ※実際の値は JpaAuditingConfig の AuditorAware が供給する
    @CreatedBy
    @Column(name = "created_by_source", updatable = false, length = 255)
    private String createdBySource;

    // レコード最終更新日時。保存・更新のたびに自動で最新化される
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // レコードを最後に更新したAPIエンドポイント。更新のたびに自動で最新化される
    @LastModifiedBy
    @Column(name = "updated_by_source", length = 255)
    private String updatedBySource;
}