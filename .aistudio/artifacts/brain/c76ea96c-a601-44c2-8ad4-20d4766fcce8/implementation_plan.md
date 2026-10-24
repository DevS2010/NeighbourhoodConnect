# NeighbourConnect — Enterprise Architectural Specification & Implementation Plan

> **Tagline:** *Connecting every neighborhood with trusted professionals.*  
> **Platform Target:** Native Android Client (Jetpack Compose, Kotlin, Material 3) connected to FastAPI / PostgreSQL Cloud Backend & Firebase AI Services.  
> **Launch Market:** India (INR, Razorpay, Multi-lingual support for English, Hindi, and regional languages).

---

## User Review & Critical Decisions

> [!IMPORTANT]
> **Confirmed Product Decisions:**
> 1. **Client Architecture:** Native Android client engineered with Jetpack Compose, Kotlin Coroutines/Flow, Clean Architecture, and MVVM, communicating with a containerized FastAPI microservices/modular monolith backend.
> 2. **Target Market & Currency:** India (INR ₹) with Razorpay payment gateway integration, supporting UPI, cards, net banking, and automatic 2% platform commission splits.
> 3. **Service Vertical Scope:** Universal marketplace architecture supporting all 25+ local professions from Day 1 (Electricians, Plumbers, AC/Appliance Technicians, Doctors, Mechanics, Carpenters, Tutors, Lawyers, Emergency Services, etc.).
> 4. **AI Capabilities:** Gemini 3.8 Flash integrated via Firebase AI Logic / FastAPI AI pipeline for multimodal problem diagnosis (images of broken pipes/wiring), profile quality scoring, dynamic price estimation, and fake review/fraud detection.

---

## STEP 1: Business Analysis

### 1.1 Executive Summary
NeighbourConnect is a hyper-local on-demand services marketplace designed to bridge the trust and accessibility deficit between urban/suburban households and local service professionals. By combining verifiable government ID authentication, AI-driven quality validation, dynamic Trust Scores, and instantaneous emergency dispatching, NeighbourConnect elevates hyper-local gig commerce into a dependable utility.

### 1.2 Market Opportunity & Competitive Landscape
- **Current Deficiencies in Existing Aggregators (e.g., Urban Company, Justdial, Sulekha, TaskRabbit):**
  - High commissions (up to 20–30%) alienate micro-contractors and independent artisans.
  - Opaque rating mechanisms driven by fake reviews and punitive algorithms.
  - Lack of rapid triage for emergency utility and medical failures.
  - Exclusion of specialized independent professions (lawyers, doctors, tutors, water suppliers) under rigid single-vertical platforms.
- **NeighbourConnect Value Proposition:**
  - Ultra-low flat 2% commission structure on completed jobs.
  - AI-assisted self-onboarding allowing any skilled professional to register in under 5 minutes.
  - Multi-dimensional Trust Score (KYC status, completion velocity, repeat rate, verified reviews).
  - Computer Vision Problem Diagnosis: customers take a photo of an issue (e.g., dripping valve, burnt socket), and AI provides root-cause diagnosis and estimated cost range before booking.

### 1.3 Strengths, Weaknesses, Opportunities, and Threats (SWOT)

```
┌──────────────────────────────────────────────┬──────────────────────────────────────────────┐
│ STRENGTHS                                    │ WEAKNESSES                                   │
├──────────────────────────────────────────────┼──────────────────────────────────────────────┤
│ • Ultra-low 2% commission attracts top talent│ • Marketplace cold-start challenge in new    │
│ • Universal service taxonomy (25+ trades)    │   neighborhoods                              │
│ • Multimodal AI diagnosis eliminates quoting │ • High initial verification manual audit     │
│   ambiguity                                  │   overhead before full AI OCR automation     │
│ • Real-time GPS and emergency SOS mode       │ • Varied digital literacy across providers   │
├──────────────────────────────────────────────┼──────────────────────────────────────────────┤
│ OPPORTUNITIES                                │ THREATS                                      │
├──────────────────────────────────────────────┼──────────────────────────────────────────────┤
│ • Rapid Tier-2 and Tier-3 Indian city growth │ • Disintermediation (parties connecting and  │
│ • B2B subscriptions for residential societies│   dealing off-platform via cash)             │
│ • Value-added micro-insurance and tool loans │ • Regulatory shifts regarding gig-worker     │
│ • Enterprise facilities management add-ons   │   compliance and data localization           │
└──────────────────────────────────────────────┴──────────────────────────────────────────────┘
```

### 1.4 Technical Risks & Mitigation Strategies
1. **Disintermediation:** Mitigated through customer milestone escrows, guarantee warranties on jobs booked on-platform, digital GST-compliant tax invoices, and recurring loyalty discounts.
2. **Fraudulent Profiles & Sybil Reviews:** Real-time Government ID OCR and liveness check during registration; AI semantic analysis to flag coordinated, cloned, or unnatural review spikes.
3. **Emergency Dispatch Latency:** Geohash-based spatial indexing (PostGIS + Redis GEO) for sub-50ms neighbor search and real-time WebSocket push notifications to providers within a 3-5 km radius.

---

## STEP 2: Software Requirement Specification (SRS)

### 2.1 Scope & Purpose
This specification establishes the structural, functional, and performance baselines for the NeighbourConnect client application and backend microservices. The system must operate under low-bandwidth networks (2G/3G/4G/5G) across Indian geographic zones and adapt to multilingual user bases.

### 2.2 System Standards & Compatibility
- **Client OS:** Android 7.0 (API level 24) through Android 15/16 (API level 36).
- **Client UI:** Jetpack Compose with Material 3 design tokens.
- **Backend Runtime:** Python 3.11+ / FastAPI with asynchronous ASGI Uvicorn workers.
- **Database:** PostgreSQL 16 with PostGIS spatial extension.
- **Cache & Pub/Sub:** Redis 7.2 for spatial indexing, session caching, and live GPS streams.
- **AI Engine:** Google Gemini 3.8 Flash via Firebase AI Logic and secure backend APIs.

---

## STEP 3: Functional Requirements

### FR-01: Authentication & Identity Management
- Phone OTP and Google Sign-In via Jetpack Credential Manager.
- Dual-role account switcher (Customer mode vs. Service Provider mode) in a single unified account.
- JWT session management with short-lived access tokens (15 mins) and rotating refresh tokens (30 days).

### FR-02: Service Provider Onboarding & AI Verification
- Multi-step registration: Personal info, Trade categories, Service radius, Base rates, Working hours.
- KYC Upload: Aadhaar/PAN or national photo ID and trade certification.
- AI Profile Quality Checker: Evaluates bio readability, profile photo clarity, service catalog completeness, and flags deceptive or low-effort profiles.

### FR-03: Universal Catalog & Smart Search
- Hierarchical multi-tier categorization: Home Repair, Personal Health, Professional Advisory, Logistics/Automotive, Domestic Help, Emergency.
- Natural language semantic search (e.g., *"my sink is clogged and overflowing"* -> auto-matches Plumber).
- Filter by distance (km), price tier, Trust Score (min 80), availability (now vs. scheduled), and language spoken.

### FR-04: Multimodal AI Problem Diagnosis & Cost Estimation
- Photo/video upload of the fault with descriptive voice or text prompt.
- Gemini 3.8 Flash diagnoses the defect, categorizes complexity, lists likely replacement parts, and produces an estimated price band (e.g., ₹350 - ₹600).
- One-tap "Request Quotes from Nearby Specialists" passing the diagnosis report directly.

### FR-05: Real-Time Availability, Scheduling & Emergency Mode
- Instant booking or scheduled booking calendar slots.
- **Emergency SOS Mode:** One-tap broadcast to on-duty urgent responders within 5 km with audio ring alerts, bypassing standard scheduling.
- Real-time GPS location tracking of dispatched provider once the job is accepted.

### FR-06: Real-Time In-App Communication
- WebSocket/FCM-backed secure text and media chat.
- In-chat digital quotation transmission: provider submits itemized quote, customer taps "Accept & Pay Escrow".
- Phone number masking/in-app VoIP dialer to protect privacy.

### FR-07: Payments, Escrow & Invoicing
- Razorpay SDK integration supporting UPI (GPay, PhonePe, Paytm), Net Banking, Credit/Debit Cards.
- Milestone Escrow: customer funds the job upfront; funds are held in escrow and released to provider upon digital OTP verification of completion.
- Automated 2% platform fee deduction; automated generation of GST-compliant PDF invoices.

### FR-08: Trust Score & AI Fraud Protection
- Algorithmic Trust Score (0 to 100) calculated from:
  - Identity verification status (+25 pts)
  - Completed jobs & dispute rate (+35 pts)
  - Verified customer ratings (+25 pts)
  - Punctuality & acceptance rate (+15 pts)
- AI fake review detection: flags anomalous timestamps, IP collisions, and duplicate linguistic patterns.

---

## STEP 4: Non-Functional Requirements

- **Performance:** 
  - Search query response time `< 150ms`.
  - App cold startup time `< 1.2s` on mid-tier hardware.
  - Image diagnosis response time `< 3.5s`.
- **Scalability:** Horizontal scaling of FastAPI workers behind Nginx load balancers capable of serving 50,000+ concurrent WebSocket connections.
- **Reliability & Availability:** 99.9% uptime with automated database failover and read replicas.
- **Security & Data Privacy:** AES-256 at rest, TLS 1.3 in transit, strict adherence to DPDP Act (Digital Personal Data Protection Act) and zero plain-text storage of government ID numbers.
- **Offline Resilience:** Local Room database caching of active bookings, recent chats, and offline draft capability for poor network conditions.

---

## STEP 5: User Personas

### Persona A: Rahul Sharma (Urban Customer)
- **Profile:** 34, Senior Product Designer living in a high-rise apartment in Bengaluru.
- **Pain Points:** AC stops cooling on a Saturday evening; aggregators offer slots 48 hours out. Doesn't know local trusted technicians.
- **Goal:** Wants an immediate, vetted AC technician with transparent pricing and live GPS tracking.

### Persona B: Suresh Kumar (Independent Master Electrician)
- **Profile:** 42, certified electrician with 18 years experience in Pune.
- **Pain Points:** Large aggregators take 25% cut and penalize him for declining distant jobs. Local classifieds generate spam calls.
- **Goal:** Wants direct neighborhood clients, low commission (2%), instant UPI payouts, and a verifiable digital portfolio.

### Persona C: Dr. Anjali Mehta (Pediatrician & Tele-Consultant)
- **Profile:** 38, physician offering neighborhood clinic visits and home consultations.
- **Pain Points:** Needs formal appointment booking with verified patient identity and upfront consultation fee escrows.
- **Goal:** Professional profile showcasing medical registration, structured slots, and secure in-app messaging.

---

## STEP 6: User Stories

| ID | As a... | I want to... | So that I can... |
|---|---|---|---|
| US-01 | Customer | Take a picture of a sparking switchboard | Get an instant AI diagnosis and estimated repair cost before booking |
| US-02 | Customer | Activate Emergency Mode for a pipe burst | Instantly ping all verified plumbers within 3 km who are active right now |
| US-03 | Provider | Register my trade and upload my ID | Build a verified profile with an algorithmic Trust Score |
| US-04 | Provider | Submit an itemized quote in chat | Agree with the customer on parts and labor costs before starting work |
| US-05 | Customer | Pay through UPI into an escrow account | Ensure payment is only released once the work is satisfactorily completed |
| US-06 | Provider | View daily analytics and earnings | Track revenue, platform commission (2%), and customer satisfaction |

---

## STEP 7: Use Case Diagram (Text Representation)

```
                       NEIGHBOURCONNECT ECOSYSTEM
 ┌──────────────────────┐                     ┌────────────────────────┐
 │       CUSTOMER       │                     │    SERVICE PROVIDER    │
 └──────────┬───────────┘                     └───────────┬────────────┘
            │                                             │
            ├─► [UC-01: Authenticate / Switch Role] ◄─────┤
            │                                             │
            ├─► [UC-02: Smart / AI Search & Filter]       │
            │                                             │
            ├─► [UC-03: Multimodal Fault Diagnosis]       │
            │                                             │
            ├─► [UC-04: Emergency SOS Dispatch] ─────────►│ (Instant Audio Alert)
            │                                             │
            ├─► [UC-05: Real-time Chat & Quote] ◄────────►├─► [UC-08: Submit Digital Quote]
            │                                             │
            ├─► [UC-06: Escrow Payment (Razorpay)]        ├─► [UC-09: Complete KYC / ID]
            │                                             │
            ├─► [UC-07: Live GPS Provider Tracking] ◄────►├─► [UC-10: Stream GPS Coords]
            │                                             │
            └─► [UC-11: Rate & Release Escrow] ──────────►├─► [UC-12: Receive UPI Payout]
                                                          │
                                                          └─► [UC-13: Analytics Dashboard]
```

---

## STEP 8: System Architecture

```
┌─────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                  PRESENTATION LAYER (ANDROID)                                   │
│  Jetpack Compose M3 UI • Navigation Compose • Coil • Google Maps SDK • Razorpay Android SDK     │
│  StateFlow • ViewModel • Clean Architecture (Domain UseCases + Data Repositories)               │
└───────────────────────────────────────────────┬─────────────────────────────────────────────────┘
                                                │ HTTPS / WSS / REST
                                                ▼
┌─────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                 API GATEWAY & SECURITY LAYER                                    │
│  Nginx Reverse Proxy • SSL Termination • Rate Limiting (Token Bucket) • JWT Bearer Auth        │
└───────────────────────────────────────────────┬─────────────────────────────────────────────────┘
                                                │
                                                ▼
┌─────────────────────────────────────────────────────────────────────────────────────────────────┐
│                             BACKEND SERVICES (FASTAPI ASYNC ENGINE)                             │
│  ┌──────────────────────┐  ┌──────────────────────┐  ┌──────────────────────┐  ┌──────────────┐ │
│  │ Auth & Identity Svc  │  │ Search & Geo Engine  │  │ Booking & Escrow Svc │  │ Chat & WS Svc│ │
│  └──────────────────────┘  └──────────────────────┘  └──────────────────────┘  └──────────────┘ │
│  ┌──────────────────────┐  ┌──────────────────────┐  ┌──────────────────────┐  ┌──────────────┐ │
│  │ AI Diagnosis Engine  │  │ Trust & Fraud Engine │  │ Invoicing & Tax Svc  │  │ Notif Svc    │ │
│  │ (Gemini 3.8 Flash)   │  │ (Scoring Algorithm)  │  │ (PDF Generator)      │  │ (FCM Push)   │ │
│  └──────────────────────┘  └──────────────────────┘  └──────────────────────┘  └──────────────┘ │
└───────────────────────┬───────────────────────────────────────┬─────────────────────────────────┘
                        │                                       │
                        ▼                                       ▼
┌──────────────────────────────────────────────┐  ┌──────────────────────────────────────────────┐
│        DATA STORAGE & SPATIAL ENGINE         │  │         CACHE, PUBSUB & IN-MEMORY            │
│  PostgreSQL 16 + PostGIS Spatial Extensions  │  │  Redis 7.2 Clusters (Spatial GEO, Caching,   │
│  (Relational records, geo-queries, audits)   │  │  WebSocket PubSub, Rate Limits)              │
└──────────────────────────────────────────────┘  └──────────────────────────────────────────────┘
```

---

## STEP 9: Technology Stack Justification

| Layer / Component | Technology Selected | Rationale & Enterprise Justification | Alternatives Considered & Why Rejected |
|---|---|---|---|
| **Mobile Client** | Kotlin + Jetpack Compose | Native 60fps performance, declarative UI, direct Google Maps & CameraX hardware acceleration, strict lifecycle control | React Native (bridge overhead, inconsistent native animations), Flutter (larger engine footprint, second-class Wear/Auto future integrations) |
| **Backend API** | FastAPI (Python 3.11+) | Asynchronous ASGI speed, Pydantic type validation, automated OpenAPI docs, seamless integration with AI/ML ecosystems | Django (heavyweight synchronous ORM defaults), Express.js (weaker typing, poor native AI tooling) |
| **Primary Database**| PostgreSQL 16 + PostGIS | Enterprise ACID compliance for financial escrows + unmatched spatial indexing (`ST_DWithin`, `ST_Distance`) for hyper-local lookups | MongoDB (weak multi-document financial ACID locks), MySQL (inferior spatial indexing features) |
| **Caching & PubSub**| Redis 7.2 | Microsecond latency key-value caching, native GEO commands (`GEOSEARCH`), and low-latency WebSocket pub/sub | Memcached (no native PubSub or spatial primitives) |
| **AI Processing** | Gemini 3.8 Flash | Sub-second multimodal reasoning on defect images, structured JSON responses, low token latency, high vision accuracy | GPT-4o (higher per-token cost, higher latency for edge India mobile networks) |
| **Payment Gateway**| Razorpay | Dominant Indian market integration: native UPI Intent flows, auto-split 2% fee marketplace routes, instant refunds | Stripe (higher Indian cross-border rates, sub-optimal domestic UPI flows) |

---

## STEP 10: Database Design (PostgreSQL 16 + PostGIS)

### 10.1 Key Tables & Schemas
- `users`: Base identity table supporting both roles (`customer`, `provider`, `admin`).
- `provider_profiles`: Extended metadata for trades, experience, base rates, service radius, KYC status, and computed Trust Score.
- `categories`: Service classifications with hierarchical parent/child relations.
- `services`: Granular service offerings (e.g., "Ceiling Fan Repair", "Under-sink Pipe Leakage").
- `bookings`: Core transaction ledger tracking states (`REQUESTED`, `DISPATCHED`, `IN_PROGRESS`, `ESCROW_LOCKED`, `COMPLETED`, `CANCELLED`).
- `quotes`: Itemized estimates submitted by providers during negotiation.
- `escrow_transactions`: Financial ledger recording Razorpay order IDs, payments, 2% commissions, and provider disbursements.
- `reviews`: Customer ratings, textual reviews, and AI fake review detection flags.
- `chat_messages`: Archived conversations and media attachment URLs.
- `ai_diagnoses`: Cached vision diagnosis records, detected faults, and cost estimates.

---

## STEP 11: Entity Relationship Diagram (ERD - Text Representation)

```
  ┌──────────────┐             1:1             ┌───────────────────────┐
  │    USERS     ├────────────────────────────►│   PROVIDER_PROFILES   │
  │──────────────│                             │───────────────────────│
  │ id (UUID, PK)│                             │ id (UUID, PK)         │
  │ phone_number │                             │ user_id (FK)          │
  │ full_name    │                             │ trade_category        │
  │ role         │                             │ location (GEOMETRY)   │
  │ created_at   │                             │ trust_score (INT)     │
  └──────┬───────┘                             │ kyc_verified (BOOL)   │
         │                                     └───────────┬───────────┘
         │ 1:N                                             │ 1:N
         ▼                                                 ▼
  ┌──────────────┐        1:N                  ┌───────────────────────┐
  │   BOOKINGS   │◄────────────────────────────┤        QUOTES         │
  │──────────────│                             │───────────────────────│
  │ id (UUID, PK)│                             │ id (UUID, PK)         │
  │ customer_id  │                             │ booking_id (FK)       │
  │ provider_id  │                             │ labor_amount (DECIMAL)│
  │ status (ENUM)│                             │ parts_amount (DECIMAL)│
  │ is_emergency │                             │ status (ACCEPTED/...) │
  │ total_amount │                             └───────────────────────┘
  └──────┬───────┘
         │
         ├─────────────────────────────┬─────────────────────────────┐
         │ 1:1                         │ 1:1                         │ 1:N
         ▼                             ▼                             ▼
  ┌───────────────────────┐     ┌───────────────────────┐     ┌───────────────────────┐
  │  ESCROW_TRANSACTIONS  │     │        REVIEWS        │     │     CHAT_MESSAGES     │
  │───────────────────────│     │───────────────────────│     │───────────────────────│
  │ id (UUID, PK)         │     │ id (UUID, PK)         │     │ id (UUID, PK)         │
  │ booking_id (FK)       │     │ booking_id (FK)       │     │ booking_id (FK)       │
  │ razorpay_order_id     │     │ rating (1-5)          │     │ sender_id (FK)        │
  │ platform_fee (2%)     │     │ sentiment_score       │     │ content (TEXT)        │
  │ payout_status (ENUM)  │     │ is_flagged_fake (BOOL)│     │ timestamp             │
  └───────────────────────┘     └───────────────────────┘     └───────────────────────┘
```

---

## STEP 12: REST API Specification

### 12.1 Authentication & Profile
- `POST /api/v1/auth/otp/send` -> Sends SMS OTP to Indian mobile numbers (+91).
- `POST /api/v1/auth/otp/verify` -> Validates OTP, issues JWT tokens.
- `GET /api/v1/users/me` -> Retrieves current user profile and role capabilities.
- `POST /api/v1/providers/register` -> Submits trade profile, service area, and catalog.
- `POST /api/v1/providers/kyc/upload` -> Uploads Government ID documents for AI OCR & manual audit.

### 12.2 Search, Discovery & AI Diagnosis
- `POST /api/v1/search/nearby` -> Geohash spatial query returning nearby verified providers with filters.
- `POST /api/v1/ai/diagnose` -> Multimodal vision analysis; takes image/video + description, returns diagnosis, parts list, and price range.
- `GET /api/v1/categories` -> Returns full taxonomy of 25+ trades with localized titles and icons.

### 12.3 Bookings & Emergency Dispatch
- `POST /api/v1/bookings/create` -> Initializes a standard or scheduled booking.
- `POST /api/v1/bookings/emergency` -> Triggers SOS broadcast to all active providers within 5 km.
- `PATCH /api/v1/bookings/{id}/status` -> Updates job state (`ACCEPTED`, `ON_THE_WAY`, `ARRIVED`, `COMPLETED`).
- `GET /api/v1/bookings/{id}/live-tracking` -> SSE/WebSocket endpoint for live provider GPS coords.

### 12.4 Payments & Escrow
- `POST /api/v1/payments/create-order` -> Generates Razorpay order ID for the agreed quote amount.
- `POST /api/v1/payments/verify-webhook` -> Server-to-server webhook validating payment signature and locking escrow.
- `POST /api/v1/payments/release-escrow` -> Releases escrow funds to provider upon completion OTP verification.

---

## STEP 13: Folder Structure (Android Clean Architecture)

```
app/src/main/java/com/neighbourconnect/
├── MainActivity.kt
├── NeighbourConnectApp.kt
├── core/
│   ├── common/             # Result wrappers, Dispatchers, Extensions
│   ├── network/            # Retrofit clients, OkHttp interceptors, Auth headers
│   ├── database/           # Room DB, DAOs, Entity models for offline caching
│   └── util/               # Geohash calculations, Currency formatters (₹)
├── data/
│   ├── models/             # API DTOs (Request / Response models)
│   ├── repository/         # Repository implementations
│   └── datasource/         # Remote ApiService & Local Room DataSources
├── domain/
│   ├── model/              # Pure domain entities (Provider, Booking, Quote, TrustScore)
│   ├── repository/         # Repository interfaces (Clean Architecture contract)
│   └── usecase/            # Use cases (DiagnoseProblemUseCase, SearchNearbyUseCase, etc.)
└── presentation/
    ├── theme/              # Material 3 ColorScheme, Typography, Shapes
    ├── components/         # Common UI: TrustBadge, PriceTag, EmergencyFAB, RatingStars
    ├── navigation/         # NavHost, AppDestinations, BottomBar
    ├── screens/
    │   ├── splash/         # Animated startup & session check
    │   ├── auth/           # Phone OTP & Google Sign-In
    │   ├── home/           # Category grid, AI diagnosis banner, active bookings
    │   ├── search/         # Filtered map & list view with distance calculation
    │   ├── ai_diagnosis/   # Camera capture, AI visual analysis, instant quote view
    │   ├── provider_detail/# Trust Score breakdown, reviews, catalog, booking flow
    │   ├── booking/        # Active job status, milestone tracker, live GPS map
    │   ├── chat/           # Real-time negotiation, digital quote cards
    │   ├── payment/        # Razorpay integration sheet & digital tax invoice
    │   ├── emergency/      # SOS emergency radar dispatch mode
    │   └── provider_mode/  # Provider dashboard, lead requests, earnings & KYC status
```

---

## STEP 14: Screen List

1. **Splash & Onboarding:** Value propositions, language selection (English / Hindi / Regional).
2. **Auth Screen:** Mobile OTP input, auto-read SMS, role selection.
3. **Customer Home Dashboard:** Search bar, AI Diagnosis hero card, Emergency SOS button, Category grid, Trending verified neighbors.
4. **AI Problem Diagnosis Screen:** Camera viewfinder / gallery picker, issue description, real-time Gemini analysis results card with cost estimate.
5. **Provider Discovery & Map Screen:** Interactive Google Map with live pins, radius slider, Trust Score badge filters.
6. **Provider Profile & Trust Audit:** Verification badges (Aadhaar KYC, Police check), Trust Score dial, portfolio photos, customer reviews.
7. **Booking & Scheduling Modal:** Instant dispatch vs. selected date/time slot picker.
8. **In-App Chat & Quote Negotiation:** Message stream, image sharing, interactive "Quote Card" with Accept/Decline action.
9. **Live GPS Tracking Screen:** Real-time marker of provider en route, estimated time of arrival (ETA), phone call masking action.
10. **Payment & Escrow Release Screen:** Razorpay checkout sheet, escrow lock confirmation, completion 4-digit OTP.
11. **Review & Rating Screen:** 5-star scoring, attribute tags (Punctual, Professional, Fair Price), photo upload.
12. **Provider Onboarding & KYC Screen:** Trade selection, government ID camera capture, bank account/UPI ID setup.
13. **Provider Dashboard (Pro Mode):** Online/Offline toggle, incoming emergency pings, daily earnings chart, Trust Score breakdown.

---

## STEP 15: Navigation Flow

```
[Splash Screen] ──► [Language Selection] ──► [Mobile OTP Auth]
                                                   │
                ┌──────────────────────────────────┴──────────────────────────────────┐
                ▼                                                                     ▼
      [Customer Role Selected]                                              [Provider Role Selected]
                │                                                                     │
                ▼                                                                     ▼
    [Home Dashboard with M3 Nav]                                            [Provider Pro Dashboard]
    ├── BottomTab 1: Home (Categories & AI Banner)                          ├── Orders & Lead Pipeline
    ├── BottomTab 2: Explore Map (Nearby Geo Pins)                          ├── Active Service Tracking
    ├── BottomTab 3: Bookings (Active & History)                            ├── Earnings & 2% Fee Report
    └── BottomTab 4: Profile & Switch Role                                  └── KYC Verification Center
                │
    ┌───────────┼───────────────────────────┐
    ▼           ▼                           ▼
[AI Diagnosis] [Emergency SOS Mode]   [Search Providers]
    │           │                           │
    └───────────┼───────────────────────────┘
                ▼
      [Provider Profile View]
                │
                ▼
      [Initiate Booking / Slot]
                │
                ▼
      [Live Chat & Quote Acceptance]
                │
                ▼
      [Razorpay Escrow Payment]
                │
                ▼
      [Live GPS Tracking Screen]
                │
                ▼
      [Completion OTP & Escrow Release]
                │
                ▼
      [Review & Digital Invoice Download]
```

---

## STEP 16: UI Design System

### 16.1 Design Personality & Atmosphere
- **Mood:** Trustworthy, modern, clean, utility-first with tactile feedback. Avoids generic flat gray layouts.
- **Primary Color:** Deep Trust Indigo (`#1A365D`) — represents reliability, security, and institutional competence.
- **Secondary / Accent:** Electric Emerald (`#059669` / `#10B981`) — symbolizes active status, verified credentials, and successful completion.
- **Emergency Accent:** Radiant Crimson (`#DC2626`) — high-visibility emergency dispatch mode.
- **Backgrounds:** Clean layered surfaces with tonal elevations (`#F8FAFC` in Light, `#0F172A` in Dark).

### 16.2 Typography Tokens
- **Display & Headlines:** Bold, high-legibility geometric sans font for headings and prices.
- **Body & Labels:** Clean, accessible sans font with strict `sp` scaling for readability across font scale preferences.

### 16.3 Key UI Components
- **TrustBadge:** Hexagonal verified checkmark displaying the real-time Trust Score (e.g., `94/100`).
- **EmergencyFAB:** High-contrast pulsing button for instantaneous SOS service access.
- **EscrowCard:** Visual security shield showing funds held safely in escrow until completion OTP is verified.

---

## STEP 17: Security Architecture

1. **Authentication & Session Security:**
   - Short-lived asymmetric JWTs with HMAC-SHA256 signature verification.
   - Rate limiting on OTP generation (maximum 3 attempts per phone number per 15-minute window).
2. **KYC & Identity Data Protection:**
   - Government ID scans stored in encrypted private buckets with pre-signed ephemeral URLs (valid for 5 minutes).
   - Identity numbers (Aadhaar/PAN) masked at ingestion (only last 4 digits stored; plain-text never saved).
3. **Payment & Financial Integrity:**
   - Razorpay Webhook signature verification (`X-Razorpay-Signature` HMAC-SHA256).
   - Strict idempotency keys on all payout and escrow release transactions.
4. **Zero-Trust Client Security:**
   - Android App Check integration to prevent unauthorized API calls from rooted or emulated scrapers.
   - SSL Pinning for backend API domains to prevent Man-in-the-Middle (MitM) attacks.

---

## STEP 18: Deployment Architecture

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                              PRODUCTION AWS INFRASTRUCTURE                             │
│                                                                                        │
│     [AWS Route 53 DNS] ──► [CloudFront CDN] ──► [AWS Application Load Balancer]        │
│                                                            │ (TLS 1.3)                 │
│                                                            ▼                           │
│                              ┌──────────────────────────────────────────────┐          │
│                              │        AWS ECS Fargate Cluster (Docker)      │          │
│                              │  ┌────────────────┐  ┌────────────────┐      │          │
│                              │  │ FastAPI Pod 1  │  │ FastAPI Pod 2  │ ...  │          │
│                              │  └────────────────┘  └────────────────┘      │          │
│                              └──────────────────────┬───────────────────────┘          │
│                                                     │                                  │
│                      ┌──────────────────────────────┴──────────────────────────────┐   │
│                      ▼                                                             ▼   │
│       ┌──────────────────────────────┐                             ┌───────────────────┴┐
│       │ AWS RDS PostgreSQL 16 Multi-AZ│                            │ AWS ElastiCache   │
│       │ + PostGIS + Read Replicas    │                             │ Redis 7.2 Cluster │
│       └──────────────────────────────┘                             └───────────────────┘
└────────────────────────────────────────────────────────────────────────────────────────┘
```

- **CI/CD Pipeline:** GitHub Actions triggering automated linting, unit tests, Docker multi-stage builds, and zero-downtime rolling deployments to AWS ECS.
- **Observability:** Prometheus metrics + Grafana dashboards + Sentry for client/backend crash reporting and distributed tracing.

---

## STEP 19: Development Roadmap

- **Phase 1: Foundation & Identity (Current)**
  - Setup core Android application architecture, Material 3 theme, and domain models.
  - Setup authentication flow, role switching, and local storage foundations.
- **Phase 2: Universal Catalog & Discovery**
  - Implement full 25+ profession taxonomy, smart search, and Geohash proximity filters.
  - Build interactive Google Maps integration with live provider markers.
- **Phase 3: AI Engine & Problem Diagnosis**
  - Integrate Gemini 3.8 Flash for photo/video defect analysis, parts identification, and cost estimates.
  - Implement AI Profile Quality auditing and Trust Score calculations.
- **Phase 4: Bookings, Emergency Dispatch & Real-Time Chat**
  - Build booking lifecycle state machine, Emergency SOS mode, and WebSocket messaging with digital quote cards.
- **Phase 5: Payments, Escrow & Invoicing**
  - Razorpay checkout, escrow milestone holding, OTP completion release, and automated tax invoicing.
- **Phase 6: Hardening, Polish & Provider Pro Suite**
  - Provider analytics dashboard, fake review detection engine, and multi-lingual localization.

---

## STEP 20: Milestones

- [x] **Milestone 1:** Enterprise Specification, System Architecture & Planning complete.
- [ ] **Milestone 2:** Android Client Core Architecture, Theme tokens, Navigation, and Domain entities.
- [ ] **Milestone 3:** Customer Dashboard, 25+ Category Explorer, and Search engine.
- [ ] **Milestone 4:** Multimodal Gemini AI Problem Diagnosis & Cost Estimator.
- [ ] **Milestone 5:** Provider Profile, Trust Score engine, and Emergency SOS dispatch.
- [ ] **Milestone 6:** In-App Chat, Digital Quotations, and Razorpay Escrow payment flow.
- [ ] **Milestone 7:** Provider Pro Suite (Analytics, KYC verification, and order management).
- [ ] **Milestone 8:** Final end-to-end integration verification, compilation, and polish.
