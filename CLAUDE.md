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

### 0. 前提: ローカル開発環境の整備

- [x] `docker/docker-compose.yml` の作成（PostgreSQL + backend起動用）
- [x] `.env` ファイルの作成（`DB_USERNAME`、`DB_PASSWORD`など、`.gitignore`対象）
- [x] `docker-compose up`でPostgreSQLコンテナが起動し、Flywayが`V1__init_schema.sql`を正しく適用するか確認

## バックエンド実装タスクリスト（進行中）

### 1. 共通基盤（全機能に先立って作るもの）

**Entity共通部分**
- [ ] `entity/BaseEntity.java` — `created_at`/`updated_at`/`created_by_source`/`updated_by_source`を持つ共通親クラス(`@MappedSuperclass`)
- [ ] `config/JpaAuditingConfig.java` — `@EnableJpaAuditing`の設定(`@CreatedDate`/`@LastModifiedDate`用)

**リクエスト元追跡(`created_by_source`)**
- [ ] `interceptor/RequestSourceInterceptor.java` — リクエストのメソッド+パスをThreadLocalに保持
- [ ] `config/WebMvcConfig.java` — Interceptorの登録

**例外・エラーハンドリング（バリデーション設計書に対応）**
- [ ] `exception/BusinessRuleException.java` — 業務ルール違反用のカスタム例外（例:出品者の自己購入禁止）
- [ ] `exception/ResourceNotFoundException.java` — 404用
- [ ] `exception/GlobalExceptionHandler.java`(`@ControllerAdvice`) — 統一エラーレスポンス形式(`error.code`/`message`/`details`)の実装
- [ ] `dto/ErrorResponse.java` — エラーレスポンスのDTO

**セキュリティ(JWT認証)**
- [ ] `security/JwtTokenProvider.java` — JWTの発行・検証
- [ ] `security/JwtAuthenticationFilter.java` — リクエストごとのトークン検証
- [ ] `config/SecurityConfig.java` — Spring Security設定（エンドポイントごとの認可ルール、🔒/👤SELLERマークに対応）

### 2. Auth（会員機能）

**Entity / Repository**
- [ ] `entity/User.java` — `id`, `email`, `passwordHash`, `role`(GENERAL/SELLER), `isActive`
- [ ] `entity/Role.java`(enum) — `GENERAL`, `SELLER`
- [ ] `repository/UserRepository.java`

**DTO**
- [ ] `dto/RegisterRequest.java` — バリデーションアノテーション付き（email形式、パスワード8〜64文字等）
- [ ] `dto/LoginRequest.java`
- [ ] `dto/AuthResponse.java`(accessToken/refreshToken)
- [ ] `dto/UserResponse.java`

**Service / Controller**
- [ ] `service/AuthService.java` — 登録、ログイン、リフレッシュ、`is_active`チェック
- [ ] `service/UserService.java` — 自分の情報取得・更新・退会（論理削除）
- [ ] `controller/AuthController.java`
    - [ ] `POST /api/auth/register`
    - [ ] `POST /api/auth/login`
    - [ ] `POST /api/auth/refresh`
    - [ ] `GET /api/users/me`
    - [ ] `PUT /api/users/me`
    - [ ] `DELETE /api/users/me`

### 3. Photos（商品/写真関連）

**Entity / Repository**
- [ ] `entity/Photo.java` — `sellerId`, `title`, `price`, `watermarkedImageUrl`, `originalImageUrl`, `category`, `isDeleted`
- [ ] `repository/PhotoRepository.java` — カテゴリ・タイトル検索用クエリメソッド

**DTO**
- [ ] `dto/PhotoCreateRequest.java`
- [ ] `dto/PhotoUpdateRequest.java`
- [ ] `dto/PhotoResponse.java`
- [ ] `dto/PhotoListResponse.java`（ページネーション対応）

**Service / Controller**
- [ ] `service/PhotoService.java` — 一覧・詳細・登録・編集・論理削除
- [ ] `service/WatermarkService.java` — 画像アップロード時のウォーターマーク合成処理
- [ ] `service/FileStorageService.java` — 画像ファイルの保存・取得
- [ ] `controller/PhotoController.java`
    - [ ] `GET /api/photos`（検索・カテゴリ絞り込み）
    - [ ] `GET /api/photos/{id}`
    - [ ] `POST /api/photos`（出品者権限チェック含む）
    - [ ] `PUT /api/photos/{id}`（自分の出品物のみ編集可）
    - [ ] `DELETE /api/photos/{id}`（論理削除）

### 4. Cart（カート）

**Entity / Repository**
- [ ] `entity/Cart.java` — `userId`(1対1)
- [ ] `entity/CartItem.java` — `cartId`, `photoId`（複合ユニーク制約）
- [ ] `repository/CartRepository.java`
- [ ] `repository/CartItemRepository.java`

**Service / Controller**
- [ ] `service/CartService.java`
    - [ ] カート内容取得
    - [ ] カートへの追加（重複チェック、**出品者の自己購入禁止チェックを含む**）
    - [ ] カートからの削除
- [ ] `controller/CartController.java`
    - [ ] `GET /api/cart`
    - [ ] `POST /api/cart/items`
    - [ ] `DELETE /api/cart/items/{photoId}`

### 5. Orders（注文・決済）

**Entity / Repository**
- [ ] `entity/Order.java` — `userId`, `stripePaymentIntentId`(unique), `status`（デフォルトPENDING）
- [ ] `entity/OrderItem.java` — `orderId`, `photoId`, `price`
- [ ] `entity/OrderStatus.java`(enum) — PENDING/PAID/FAILED/CANCELLED/REFUNDED
- [ ] `repository/OrderRepository.java`
- [ ] `repository/OrderItemRepository.java`

**Stripe連携**
- [ ] `config/StripeConfig.java` — テストモードAPIキーの設定（環境変数から読み込み）
- [ ] `service/StripeService.java` — PaymentIntent作成、決済状況確認（Stripe API問い合わせ）
- [ ] `controller/StripeWebhookController.java` — Webhook受信、署名検証

**Service / Controller**
- [ ] `service/OrderService.java`
    - [ ] カートから注文作成
    - [ ] 注文履歴一覧・詳細取得
    - [ ] **決済状況確認 → download_tokens自動生成**（バックエンド経由の確認方式）
- [ ] `controller/OrderController.java`
    - [ ] `POST /api/orders`
    - [ ] `GET /api/orders`
    - [ ] `GET /api/orders/{id}`
    - [ ] `POST /api/orders/{id}/payment-intent`
    - [ ] `GET /api/orders/{id}/payment-status`（フロント直接ではなくバックエンド経由確認方式）

### 6. Downloads（ダウンロード）

**Entity / Repository**
- [ ] `entity/DownloadToken.java` — `orderItemId`, `token`(unique), `expiresAt`
- [ ] `repository/DownloadTokenRepository.java`

**Service / Controller**
- [ ] `service/DownloadService.java` — トークン発行、有効期限チェック、ファイル配信
- [ ] `controller/DownloadController.java`
    - [ ] `GET /api/downloads/{token}`（期限切れ410、不正404）

### 7. Seller（出品者ダッシュボード）

**Service / Controller**
- [ ] `service/SellerService.java`
    - [ ] 自分の出品写真一覧取得
    - [ ] 売上集計（総売上額、今月の売上、販売件数）
    - [ ] 販売履歴（受注一覧）取得
- [ ] `controller/SellerController.java`
    - [ ] `GET /api/seller/photos`
    - [ ] `GET /api/seller/sales`
    - [ ] `GET /api/seller/orders`

### 8. テスト（JUnit・Testcontainers）

- [ ] `test/.../AuthServiceTest.java` — 会員登録・ログインのユニットテスト
- [ ] `test/.../PhotoServiceTest.java`
- [ ] `test/.../CartServiceTest.java` — 重複防止・自己購入禁止のテスト
- [ ] `test/.../OrderServiceTest.java`
- [ ] `test/.../*IntegrationTest.java` — Testcontainersを使ったPostgreSQL連携の結合テスト

### 実装の推奨順序

依存関係を踏まえた、着手順：

1. **共通基盤**（BaseEntity、例外処理、Interceptor）— 他すべての土台
2. **Auth**（User Entity、JWT認証）— 認可が絡む機能の前提
3. **Photos**（商品CRUD）— 比較的シンプルで、他機能から参照される
4. **Cart** — Photosに依存
5. **Orders + Stripe** — Cart・Photosに依存、最も複雑な部分
6. **Downloads** — Ordersに依存
7. **Seller** — Photos・Ordersの集計、最後に着手

## 次にやること（未着手）

- フロントエンド: 各画面（商品一覧、カート、会員機能、購入、出品者ダッシュボード等）の実装（バックエンドAPI実装完了後に着手）

## 運用ルール（重要）

- Claude Codeの役割は基本的に「相談・原因調査・ソース解析」に限定する方針だったが、今後は実装作業も依頼する予定（人間側の指示があった範囲で実施）
- Gitのコミット・プッシュは人間側（SourceTree等）で行う。Claude Codeに勝手にコミットさせない
- `projects` 共有フォルダはNFS経由で読み取り専用マウントされる構成のものと、Docker内で書き込み可能な構成のものが混在するため、書き込みが必要な作業ではマウント方式を確認すること
- 【2026-08-23判明】現状のClaude Codeセッションが動くコンテナからは `/workspace` 全体が読み取り専用マウントになっており、CLAUDE.md含む一切のファイルの新規作成・編集ができない状態。書き込み可能なコンテナ/マウント設定は未整備（要対応・保留中）
- 上記の暫定対応として、CLAUDE.md更新時はClaude Codeがセッション専用の書き込み可能ディレクトリ（`/tmp/claude-0/-workspace-Git-macrostock-react-java/<セッションID>/scratchpad/`）に更新済みファイルそのものを出力し、人間側が `docker cp` でホストの実ファイルへ上書き→SourceTree等でコミットする運用とする
- 調査依頼などでExcel(.xlsx)ファイル出力が必要な場合も同様に、上記のセッション専用スクラッチパッドディレクトリに出力し、人間側が `docker cp` で都度取得する。パスはセッションごとに変わるため使い回し不可
