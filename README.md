# PixaVault - Android Gallery App

📷 A modern Android Gallery Application built with Jetpack Compose

![Android](https://img.shields.io/badge/Android-8.0%2B-green)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple)
![Compose BOM](https://img.shields.io/badge/Compose%20BOM-2026.04.00-blue)
![AGP](https://img.shields.io/badge/AGP-9.2.0-orange)

## 🌟 Features

- **Material 3 Design** - Modern UI with Material Design 3
- **Photo Grid** - Beautiful 3-column responsive grid
- **Tab Navigation** - Photos, Albums, Favorites
- **Full-screen Viewer** - View photos in full screen with zoom
- **Favorite Photos** - Mark and view favorite photos
- **Dark Mode** - Automatic dark/light theme support
- **Smooth Animations** - Beautiful transitions and animations

## 📱 Screenshots

| Photos Tab | Photo Detail |
|:----------:|:------------:|
| ![Photos](screenshots/photos.png) | ![Detail](screenshots/detail.png) |

## 🚀 Getting Started

### Prerequisites

- Android Studio Ladybug (2024.2.2) or later
- JDK 21
- Android SDK 35
- Android device/emulator with API 26+

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/senkuboy0-cyber/PixaVault.git
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned folder

3. **Sync & Build**
   - Click "Sync Project with Gradle Files"
   - Build → Build Bundle(s) / APK(s) → Build APK(s)

4. **Run**
   - Connect your Android device or start an emulator
   - Click "Run" (Shift + F10)

## 📦 Download APK

Get the latest debug APK from [Releases](https://github.com/senkuboy0-cyber/PixaVault/releases)

## 🛠️ Tech Stack

| Category | Technology | Version |
|----------|------------|---------|
| Language | Kotlin | 2.0.21 |
| UI | Jetpack Compose | BOM 2026.04.00 |
| Design | Material 3 | Latest |
| Architecture | MVVM | - |
| Navigation | Navigation Compose | 2.8.5 |
| Image Loading | Coil | 3.0.4 |
| Build Tool | AGP | 9.2.0 |
| Build Tool | Gradle | 9.1.0 |

## 📂 Project Structure

```
app/
└── src/main/
    ├── java/com/pixavault/app/
    │   ├── MainActivity.kt
    │   ├── PixaVaultApp.kt
    │   └── ui/
    │       ├── components/
    │       ├── screens/
    │       ├── theme/
    │       └── viewmodel/
    └── res/
        └── values/
```

## 🤝 Contributing

Contributions are welcome! Feel free to submit issues and pull requests.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

Made with ❤️ by Senku
