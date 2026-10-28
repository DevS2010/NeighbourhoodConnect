# Neighbourhood Connect

Neighbourhood Connect (also branded **NeighbourConnect** in parts of the app) is an Android app prototype for connecting people with local service professionals. It is designed around neighborhood discovery, provider profiles, booking conversations, and tools that help customers understand a household repair before requesting help.

> **Project status:** This repository is an early-stage prototype. Some screens and API contracts describe intended functionality, while the backend currently returns placeholder responses. Do not use it to process real payments, represent providers as verified, or dispatch emergency services.

## Product direction

The app brings together customers and local professionals for common home and neighborhood services. The current Android project includes UI flows for:

- Discovering providers by profession, neighborhood, and trust score
- Viewing provider details and starting a chat or quote request
- AI-assisted fault diagnosis
- Booking and escrow status
- Provider registration and profile management
- Emergency SOS
- Language selection

The product metadata describes a marketplace spanning 25+ trades, provider trust scores, emergency dispatch, AI diagnosis, and a 2% platform commission. These are product goals; see [Current implementation](#current-implementation) for important limitations.

## Technology

- **Android:** Kotlin, Jetpack Compose, Material 3
- **Local persistence:** Room and DataStore
- **Cloud services:** Firebase Authentication, Cloud Firestore, Firebase App Check, and Firebase AI dependencies
- **Backend prototype:** FastAPI service in `backend/main.py`

## Getting started

### Android app

1. Install Android Studio with Android SDK **36.1** support.
2. Clone this repository and open its root folder in Android Studio.
3. Create or select a Firebase project and register an Android app with application ID:
   `com.aistudio.neighbourconnect.vxqpmk`
4. Download that Firebase app's `google-services.json` and place it in `app/google-services.json`.
5. Configure the Firebase services used by the project, including Google sign-in, Firestore, and App Check. Review and deploy `firestore.rules` to the matching Firebase project.
6. Sync the Gradle project, then build and run the `app` configuration on an Android device or emulator (Android 7.0 / API 24 or newer).

Firebase configuration is not included in this repository. The build is configured to warn when Google Services configuration is missing, but Firebase-backed features will not work correctly without a valid project configuration.

### FastAPI prototype

The backend has Python imports for FastAPI and Pydantic. From the repository root, install those packages and start the development server:

```bash
python -m pip install fastapi uvicorn pydantic
python -m uvicorn backend.main:app --reload
```

The health endpoint is available at `http://127.0.0.1:8000/health`; the interactive API docs are at `http://127.0.0.1:8000/docs`.

This service is not yet a production backend. Several routes declared by the Android client are not implemented in this FastAPI file. The routes that are present return illustrative data rather than querying a provider database, calling a live AI model, or creating and settling real payment orders.

## Current implementation

The Android app is the most developed part of this repository: it contains Compose screens, navigation, Firebase authentication and data access, local persistence, and service interfaces. The backend is a scaffold that shows the planned API shape.

Before any real-world pilot, the following work is needed:

- Move provider KYC verification to a trusted review process. A client must not be able to mark its own identity documents as verified.
- Enforce booking and payment state transitions on a trusted server. A client must not be able to declare escrow funded or work completed.
- Implement and authenticate the backend endpoints for OTP, provider search, bookings, emergency requests, and status updates.
- Integrate real AI diagnosis and payment provider APIs, including webhook verification and idempotency, before displaying those actions as completed.
- Review Firestore access rules and remove any development-only account assumptions or hard-coded administrator identities.
- Add operational safeguards for SOS, provider vetting, privacy, data retention, and support.

Treat the current Firestore rules and service flows as development scaffolding, not as a security-reviewed production configuration.

## Repository layout

```text
app/                 Android application
backend/main.py      FastAPI prototype
backend/schema.sql   Draft relational database schema
firestore.rules      Firestore security rules
firestore.test.js    Firestore rules tests
firebase.json        Firebase and emulator configuration
```

## Contributing

Issues and pull requests are welcome. For substantial changes, describe the user problem and the intended behavior, and include steps to verify the change.

## License

No license is currently included. Unless a license is added, standard copyright restrictions apply.
