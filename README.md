# 🎵 Montage Music — Original Hi-Fi Audio Streaming & Player

<div align="center">

![Montage Music Logo](./public/favicon.svg)

### High-Fidelity Music Streaming Experience & Offline Android Player
**A production-quality music player crafted with bespoke soundwave branding, fluid micro-interactions, dark aesthetic, and real persistent playback.**

[![Live Web Player](https://img.shields.io/badge/Live%20Demo-GitHub%20Pages-1DB954?style=for-the-badge)](https://debajoyti007-max.github.io/music_player/)
[![Android APK](https://img.shields.io/badge/Download-Android%20APK%20(Offline)-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://github.com/debajoyti007-max/music_player/releases/latest)
[![Tests Passing](https://img.shields.io/badge/Tests-12%2F12%20Passing-brightgreen?style=for-the-badge)](https://debajoyti007-max.github.io/music_player/)

</div>

---

## 🌟 Key Highlights

### 🎨 Authentic Dark Interface
- **Deep Charcoal Palette**: Carefully calibrated `#121212` base, `#181818` card containers, and `#282828` hover surfaces.
- **Bespoke Soundwave Identity**: Original equalizer "M" emblem featuring vivid emerald green (`#1DB954`) radiant gradients.
- **Contextual Ambient Lighting**: Dynamic top header color wash that shifts naturally according to the album artwork hues.
- **4-Bar Dancing Audio Equalizer**: Custom animated CSS/Compose keyframed bars that dance when audio is playing and pause smoothly when stopped.

### 🎧 Complete Playback Engine
- **Persistent Bottom Player**: Uninterrupted playback across all tab transitions (Home, Search, Library, Lyrics).
- **Karaoke Synchronized Lyrics**: Fullscreen lyrics mode (`Mic2` icon) featuring auto-scrolling synchronization, font magnification, and click-to-seek jump.
- **Floating Toast Feedback**: Real-time pill notifications for liking/unliking, playlist additions, and clipboard copies.
- **Smart Queue State Machine**: Sequential progression, repeat one / repeat all looping, shuffle randomize, and scrubbable time-seeking.
- **Offline Procedural Synthesizer**: Built-in Web Audio API fallback synthesizer so the music never crashes or fails even offline or without network.

---

## 📱 Offline Android App (Montage Player)

In addition to the web streaming player, this repository includes the native Android offline player codebase built with Jetpack Compose & ExoPlayer:

- **Local Storage Scanner**: Queries Android `MediaStore.Audio.Media.EXTERNAL_CONTENT_URI` to catalog all MP3, AAC, FLAC, and WAV files stored on phone storage with zero internet required.
- **Native Compose UI**: Modern Android Material 3 with customized dark palette and animated equalizer bars.
- **ExoPlayer Audio Architecture**: Persistent notification playback, headset hook support, and seamless scrubbing.

---

## 🚀 Quick Start (Web Player)

### Prerequisites
- Node.js 18+
- npm or yarn

### Installation & Development
```bash
# Clone the repository
git clone https://github.com/debajoyti007-max/music_player.git
cd music_player

# Install dependencies
npm install

# Start local development server with Hot Module Replacement
npm run dev

# Run automated test suite
npm test

# Build optimized production bundle
npm run build
```

---

## 🛠️ Tech Stack

### Web Application
- **Frontend Core**: React 19, TypeScript, Vite
- **Styling**: Tailwind CSS v4, Custom CSS Animations
- **Icons**: Lucide React
- **Audio**: HTML5 Audio API & Web Audio Procedural Fallback Engine
- **Testing**: Vitest / Node Native Test Runner

### Android Application
- **Language**: Kotlin 2.0
- **UI Framework**: Jetpack Compose (Material 3)
- **Audio Engine**: AndroidX Media3 / ExoPlayer
- **Storage Scanner**: Android MediaStore ContentProvider
- **Target SDK**: Android 14 (API 34)

---

## 📄 License
MIT License. Built with ❤️ for music enthusiasts.
