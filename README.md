<div align="center">
<img width="1200" alt="Emad Showcase Banner" src="EmadScreenshot.png" />
</div>

# Emad 🌌
A highly polished, celestial-glassmorphic Islamic prayer and Qada tracker built with Kotlin and Jetpack Compose.

## 💡 The Story Behind Emad

Most modern Islamic prayer applications suffer from the same issues: they are cluttered with intrusive ads, bogged down by corporate tracking, or trapped in outdated design philosophies from a decade ago. Tracking your spiritual habits shouldn't feel like navigating a bloated utility app.

Emad was born out of a desire for something better—a seamless, privacy-respecting, and visually stunning companion. Built entirely around modern glassmorphic design cues, premium animations, and deep user mechanics, it shifts the focus back to clarity, mindfulness, and intentional tracking.

> **Note on Architecture:** This app was entirely designed, architected, and engineered through precise orchestration and prompt streaming inside Google AI Studio utilizing the Gemini 3.5 Flash model.

---

## 📲 Direct Installation (Easiest)

You do not need any developer tools or code compilers to use Emad on your device. 

1. Head over to the [Releases](https://github.com/SuperGamer2026/Emad/releases) section on the right-hand side of this repository page.
2. Download the latest **`Emad_v1.0.apk`** binary directly to your Android device.
3. Open the downloaded file on your phone and install it instantly.

---

## 🔥 Key Visual & Technical Highlights

### 1. The Qada Forge & Rebuild Engine
A core tracking architecture designed specifically for managing historical missed prayers rather than just shifting through basic daily logs.
* **Target Calibration:** Establish custom recovery metrics for specific prayer windows.
* **Auto-Reset Milestones:** A persistence meter maps progression toward full recovery. Once your milestone threshold is achieved, the Forge automatically recalibrates and resets the interface for the next block.

### 2. Micro-Habit Pillars & Dashboard
The primary landing dashboard uses a minimalist layout to deliver instant context at a single glance:
* **Celestial Countdown:** A fluid, centralized elliptical countdown timer tracking the exact remaining time until the next prayer call.
* **Consistency Tiers:** A gamified progression metric that dynamically updates your status (e.g., *Seeker of Light*) based on your historical tracking stability.
* **Weekly Metrics Bar Chart:** A custom vertical bar layout mapping daily completion status across both Fardh (obligatory) and Sunnah prayers over a rolling 7-day canvas.

### 3. System Polish & Aesthetic Customization
* **19 Premium Color Presets:** Includes highly tailored colorways like *Neon Sapphire*, *Cosmic Noir*, *Sunset Rose*, and *Emerald Oasis* to completely alter the atmosphere of the app.
* **Two-Dot Unified Interface:** Theme selectors utilize a minimal, clean two-dot color indicator layout across onboarding and settings menus for absolute visual consistency.
* **Fluid Streak Feedback:** Achieving tracking milestones triggers a tailored, full-screen interactive fluid-dynamic flame animation that sweeps up from the bottom boundary of the device and extinguishes smoothly over a balanced 5-second window.
* **Floating Dock Navigation:** A minimal navigation bar that keeps labels hidden to avoid UI clutter, expanding smoothly only for the active focus icon.

---

## 🛠️ Tech Stack & Architecture
* **Language:** Kotlin
* **UI Framework:** Jetpack Compose (Declarative UI)
* **Local Storage:** SQLite via Room Database / SharedPreferences (for theme persistence and streak tracking)
* **State Management:** Jetpack ViewModel

## 📜 License
This project is open-source and licensed under the **GNU General Public License v3.0 (GPL-3.0)**. See the `LICENSE` file for details.
