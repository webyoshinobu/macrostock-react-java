# macrostock-react-java プロジェクトメモリ

このファイルはClaude Codeがセッション開始時に自動的に読み込むプロジェクトコンテキストです。
経緯・仕様が変わったら、このファイルを更新してコミットしてください。

## プロジェクト概要

カナダでの就職活動用ポートフォリオとして、以前 Vue.js + Laravel(PHP) で作成した写真販売ECサイトを、
React + Java(Spring Boot) でフルリプレイスする。

- リポジトリ名: `macrostock-react-java`
- 開発環境: 自宅QNAP TS-264上のDocker / Docker Compose（NAS本体にはaptが無いためClaude Code自体もDockerコンテナ内(node:20-slimベース)で稼働）
- インフラ費用の上限: 月3000円

## 技術スタック

- フロントエンド: React + TypeScript（Vite、ESLintあり）
- バックエンド: Java 21 / Spring Boot 4.1.0（Maven、Group=io.github.yoshinobu-shibata, Artifact=backend, Jar, YAML設定）
  - 依存関係: Spring Web, Spring Data JPA, PostgreSQL Driver, Spring Security, Validation, Flyway Migration, Lombok, Spring Boot DevTools, Testcontainers（Spring HATEOASは除外）
- DB: PostgreSQL、Flywayでマイグレーション管理
- 決済: Stripe（**必ずテストモードのみ**）
- QNAP上ではmvnwではなく公式Mavenイメージ(`maven:3.9-eclipse-temurin-21`)でビルドする方針

## 旧バージョン(Vue.js+Laravel)の機能

商品一覧、カート、会員機能、購入画面（実際には決済されない仕様）

## リプレイス版の差別化・仕様決定事項

- 購入後のみ高解像度画像をDL可能にする仕組み
- 出品者（フォトグラファー）ダッシュボード
- ユーザーロールは「一般会員」「出品者」の2種類。登録時に選択し、完全に別ロールとして管理
- 出品者は自分が出品した写真を購入できない
- 出品者情報の更新はメールアドレス・パスワード変更のみ（既存 `PUT /api/users/me` で対応、プロフィール拡張は不要）
- ダウンロード可否判定は、フロント直接ではなくバックエンド経由でStripeに確認するAPI + Webhook併用方式
- 売上ページは独立させず、販売履歴ページに売上情報をまとめる（APIエンドポイント自体は分離したまま、画面側で複数エンドポイントを呼び出す）
- 返金機能は今回は未実装（本来あった方がよい機能としてリストには残す）
- 不要と決定した画面・機能: メール確認フロー、コンタクト画面、SNSリンク画面、受注詳細画面、パスワードリセットフロー、利用規約画面

## DB設計方針

- 各テーブルに更新日時カラムを追加
- どの機能・APIが登録・更新したかを追跡するカラムを追加（値はAPIエンドポイント形式、例: `POST /api/photos`）
- `users.id` に論理削除フラグ（`is_active`等）。外部キー削除時はCASCADE/RESTRICT + 論理削除を使い分け
- インデックス追加対象: `photos.category`, `photos.seller_id`, `photos.title`（将来のあいまい検索を見越して）, `cart_items.photo_id`, `orders.user_id`, `orders.status`, `order_items.order_id` / `order_items.photo_id`, `download_tokens.order_item_id`

## 設計ツール

- ER図・DB設計: dbdiagram.io
- 画面遷移図・ワイヤーフレーム: Figma（旧Vue版のFigmaを参考にしつつ、不足画面を洗い出し済み）
- API設計: Swagger Editor / OpenAPI
- 全体アーキテクチャ図: draw.io

## 現在の進捗（2026-08-23時点）

- [x] 画面遷移・ワイヤーフレーム整理、個別画面のデザインカンプ作成
- [x] backend雛形作成
- [x] frontend雛形作成（`docker run`で `-p 5173:5173` + `--host`、動作確認済み）
- [x] `V1__init_schema.sql` を `backend/src/main/resources/db/migration/` へ移設
- [x] A5M2でNAS上のPostgreSQLに接続し、Flyway適用後のテーブルからER図をリバース生成 → dbdiagram.ioの設計と一致確認済み
- [x] `docker-compose up --build` で postgres + backend + frontend の3コンテナ構成が正常起動することを確認（backendはSpring Securityのデフォルトログイン画面が表示され認証機構が機能、frontendも `http://192.168.40.163:5173` で表示確認済み）
- NASのIPアドレスは `192.168.40.163` に固定化済み

## 次にやること（未着手・実装フェーズ）

- バックエンド: エンティティ・リポジトリ・サービス・APIコントローラの実装
- フロントエンド: 各画面（商品一覧、カート、会員機能、購入、出品者ダッシュボード等）の実装
- 認証・ロール管理（一般会員／出品者）の実装
- Stripe決済（テストモードのみ）の実装
- ダウンロードトークン方式（Stripe確認API + Webhook）の実装

## 運用ルール（重要）

- Claude Codeの役割は基本的に「相談・原因調査・ソース解析」に限定する方針だったが、今後は実装作業も依頼する予定（人間側の指示があった範囲で実施）
- Gitのコミット・プッシュは人間側（SourceTree等）で行う。Claude Codeに勝手にコミットさせない
- `projects` 共有フォルダはNFS経由で読み取り専用マウントされる構成のものと、Docker内で書き込み可能な構成のものが混在するため、書き込みが必要な作業ではマウント方式を確認すること
