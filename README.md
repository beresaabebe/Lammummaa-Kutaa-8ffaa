# Lammummaa Kutaa 8ffaa

[![Android Build](https://img.shields.io/badge/Build-Android-brightgreen.svg)](https://developer.android.com/studio)
[![Version](https://img.shields.io/badge/Version-2.0.6-blue.svg)](CHANGELOG.md)
[![API](https://img.shields.io/badge/API-23%2B-orange.svg)](https://android-arsenal.com/api?level=23)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

An enterprise-grade Android application designed for Grade 8 students to master Civic and Ethical Education (Lammummaa) in Afaan Oromoo. Built with modern Android development best practices, featuring high-performance PDF rendering and optimized ad monetization strategies.

## 🚀 Key Features

- **High-Performance Content Delivery**: Full Grade 8 Lammummaa curriculum integrated via a high-performance native PDF engine.
- **Adaptive Ad Monetization**: Advanced competition logic between **AdMob**, **Liftoff (Vungle)**, and **Unity Ads** to maximize eCPM.
- **Dynamic Navigation**: Fluid chapter transitions utilizing `ViewPager2` and synchronized UI state.
- **Enterprise-Grade Stability**: Optimized `MediaCodec` handling and rigorous lifecycle management to ensure 99.9% crash-free sessions.
- **User Engagement**: Seamless In-App Review and Update integration to maintain high ratings and user retention.
- **Modern Architecture**: Built using Material Design 3 and fully compatible with 16KB page size devices.

## 🛠 Tech Stack

- **UI**: Jetpack components (ViewPager2, RecyclerView, FragmentStateAdapter)
- **Monetization**: AdMob, Liftoff (Vungle), Unity Ads (Mediation & Competition)
- **PDF Core**: Android PdfRenderer with custom LruCache implementation
- **Analytics**: Firebase Analytics & Crashlytics
- **Configuration**: Firebase Remote Config for dynamic ad intervals

## 📦 Release Information

### Version 2.0.6 (Build 17)
> **Summary**: This major update focuses on infrastructure stability and revenue optimization.

- **Monetization Engine 2.0**: Randomized ad network competition between AdMob and Liftoff to drive higher eCPM.
- **MediaCodec Resilience**: Fixed critical `NO_MEMORY` decoder initialization errors through optimized resource recycling and muted-load strategies.
- **Lifecycle Optimization**: Improved memory footprint across WebView and PDF rendering components.
- **Smart Fallbacks**: Redundant multi-network waterfall chains (Native → MREC → Banner).

[View full CHANGELOG](CHANGELOG.md)

## 📲 Get the App

Download the official application on the Google Play Store:

[**Lammummaa Kutaa 8ffaa on Play Store**](https://play.google.com/store/apps/details?id=com.beckytech.lammummaakutaa8ffaa)

---
© 2026 BeckyTech. All rights reserved.
