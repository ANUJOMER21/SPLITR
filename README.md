
# 💰 Splitr

### Ultimate Personal & Shared Finance Ecosystem for Android

<p align="center">
  <img src="app/src/main/res/drawable/logo.png" alt="Splitr Logo" width="140"/>
</p>

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Hilt](https://img.shields.io/badge/DI-Hilt-FF6F00?logo=android&logoColor=white)](https://dagger.dev/hilt/)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28?logo=firebase&logoColor=black)](https://firebase.google.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20%2B%20MVI-purple)]()

> A high-performance, privacy-focused financial management app built with **Clean Architecture**, **MVI**, and modern Android best practices.  
> Not just another expense logger — a complete personal + shared finance suite with offline-first design and zero-cost push notifications.

---

## ✨ Why Splitr?

Most expense apps force you to choose between **privacy**, **powerful features**, or **shared groups**.  
Splitr aims to give you all three — without requiring paid cloud plans.

- 🔒 Privacy-first (local-first with optional cloud sync)
- 👥 Full group expense splitting with debt simplification
- 📊 Rich analytics & budgeting tools
- ⚡ Offline-first architecture
- 🔔 Production-grade notifications on Firebase free tier

---

## 🌟 Feature Highlights

### 📔 Personal Finance
- Fast income & expense entry with notes, dates, and categories
- Powerful filtering (category, date range, type)
- Soft delete + restore for safety
- Fully customizable categories with icons & colors
- Recurring transactions (rent, salary, subscriptions)
- Biometric lock (Fingerprint / Face Unlock)

### 👥 Split with Friends (Social Finance)
- Create groups for roommates, trips, or events
- Flexible split methods:
  - **Equally**
  - **Exact amounts**
  - **Percentages**
- Advanced **Debt Simplification Algorithm** (minimizes number of transactions)
- Interactive comments on expenses
- Real-time activity feed

### 🎯 Budgeting & Goals
- Category-level budgets with progress indicators
- Savings goals with contribution tracking
- Visual alerts when approaching or exceeding limits

### 📊 Intelligence & Analytics
- Beautiful charts powered by **Vico**
  - Spending trends (line & bar)
  - Category breakdowns (donut/pie)
  - Month-over-month comparisons
  - Spending heatmaps
- Anomaly detection for unusual spending patterns

### ⚙️ Productivity Features
- Jetpack Glance home screen widgets (balance + Quick Add)
- Smart bill reminders
- Offline-first with reliable sync

---

## 🏗 Architecture

Built with **Clean Architecture** + unidirectional data flow:

| Layer            | Responsibility                                          |
|------------------|---------------------------------------------------------|
| **Domain**       | Pure Kotlin — UseCases, Repository interfaces, Entities |
| **Data**         | Room (local) + Firestore (remote) + Sync Outbox         |
| **Presentation** | 100% Jetpack Compose + MVI (Intents → ViewModel → State)|

**Key Design Decisions:**
- Offline-first with Sync Outbox pattern
- Strict separation of concerns
- Testable UseCases and repositories
- Fully reactive with Kotlin Flow + Coroutines

---

## 🔔 The "Firebase Spark" Notification Server

Firebase Cloud Functions require the **Blaze (paid)** plan for Firestore triggers.

**Solution included in this repo:**
A lightweight Node.js server (`notification-server/`) that:
- Uses the Firebase Admin SDK
- Listens to Firestore with `onSnapshot` (works on free Spark plan)
- Sends push notifications via FCM

This gives you production-grade real-time notifications at **$0 cost**.

→ See [Notification Server README](notification-server/README.md) for deployment (Render / Fly.io).

---

## 🛠 Tech Stack

| Category     | Technology                          |
|--------------|-------------------------------------|
| Language     | Kotlin + Coroutines + Flow          |
| UI           | Jetpack Compose + Material 3        |
| Architecture | Clean Architecture + MVI            |
| DI           | Hilt                                |
| Local DB     | Room                                |
| Remote       | Cloud Firestore                     |
| Auth         | Firebase Authentication             |
| Charts       | Vico                                |
| Widgets      | Jetpack Glance                      |
| Networking   | OkHttp + Retrofit                   |
| Testing      | JUnit 5, MockK, Hilt Testing, Espresso |

---

## 🚀 Getting Started

### Prerequisites
- Android Studio **Ladybug (2024.2.1)** or newer
- JDK 17+

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/anujomer/expensetracker.git
   cd expensetracker
   ```

2. **Firebase Setup**
    - Create a project in the [Firebase Console](https://console.firebase.google.com/)
    - Enable **Authentication** (Email + Google)
    - Enable **Cloud Firestore**
    - Download `google-services.json` and place it in the `app/` directory

3. **Open & Run**
    - Open the project in Android Studio
    - Let Gradle sync
    - Run on an emulator or physical device

### Notifications (Optional)
Follow the instructions in [`notification-server/README.md`](notification-server/README.md) to deploy the free notification bridge.

---

## 📸 Screenshots

> *Add screenshots here once available*

| Home | Analytics | Groups | Budget |
|------|-----------|--------|--------|
|      |           |        |        |

---



## 🤝 Contributing

Contributions are welcome and appreciated!

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

Please open an issue first for major changes so we can discuss them.

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

<div align="center">

**Built by [Anuj Omer](https://github.com/ANUJOMER21)**

If you find this project useful, please consider giving it a ⭐

</div>
