# PixaVault - Android Gallery App

📷 A modern Android Gallery Application built with Jetpack Compose

![Android](https://img.shields.io/badge/Android-8.0%2B-green)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.20-purple)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202023.10.01-blue)

## 🌟 Features

- **Material 3 Design** - Modern UI with Material Design 3
- **Photo Grid** - Beautiful 3-column responsive grid
- **Tab Navigation** - Photos, Albums, Favorites
- **Full-screen Viewer** - View photos in full screen with zoom
- **Favorite Photos** - Mark and view favorite photos
- **Dark Mode** - Automatic dark/light theme support
- **Smooth Animations** - Beautiful transitions and animations

## 📸 Screenshots

| Photos Tab | Photo Detail |
|:----------:|:------------:|
| ![Photos](screenshots/photos.png) | ![Detail](screenshots/detail.png) |

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34
- Android device/emulator with API 24+

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

| Category | Technology |
|----------|------------|
| Language | Kotlin |
| UI | Jetpack Compose |
| Design | Material 3 |
| Architecture | MVVM |
| Navigation | Navigation Compose |
| Image Loading | Coil |
| Permissions | Accompanist |

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
