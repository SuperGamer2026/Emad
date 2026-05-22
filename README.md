<div align="center">
<img width="1200" alt="Emad Showcase Banner" src="EmadScreenshot.png" />
</div>

# Emad 🌌
A highly polished, celestial-glassmorphic Islamic prayer and Qada tracker built with Kotlin and Jetpack Compose. 

Unlike standard utilities that offer basic logs, Emad is designed around high-end visual polish and deep behavioral mechanics to make tracking both spiritually grounding and visually engaging.

> **Note on Architecture:** This app was entirely designed, architected, and engineered through precise orchestration and prompt streaming inside Google AI Studio utilizing the Gemini 3.5 Flash model.

---

## 🚀 Quick Setup & Run Locally

**Prerequisites:** [Android Studio](https://developer.android.com/studio)

1. **Clone or Download** this repository to your machine.
2. Open Android Studio, select **Open**, and choose this project directory.
3. Create a file named `.env` in the root directory and add your key: `GEMINI_API_KEY=your_key_here` (see `.env.example` for reference).
4. Connect a physical device or start an emulator, and hit **Run**.

---

## 🔥 Key Architectural Features

### 1. The Qada Forge & Rebuild Engine
A core tracking interface built for managing missed or historical prayers rather than just the daily cycle.
* **Target Calibration:** Users establish custom recovery metrics for specific prayer windows.
* **Auto-Reset Milestones:** A persistence meter maps progression toward full recovery. Once a set threshold is achieved, the Forge automatically recalibrates and clears the interface for the next block.

### 2. Micro-Habit Pillars & Dashboard
The primary landing dashboard uses a minimalist layout to deliver instant context:
* **Celestial Countdown:** A fluid, centralized elliptical countdown timer tracking the exact remaining time until the next prayer call.
* **Consistency Tiers:** A gamified progression metric that shifts the user's tier status (e.g., *Seeker of Light*) based on historical tracking stability.
* **Weekly Metrics Bar Chart:** A custom, clean vertical bar layout mapping daily completion status across both Fardh (obligatory) and Sunnah prayers over a rolling 7-day canvas.

### 3. System Polish & Themes
* **19 Custom Color Presets:** Includes highly specific, premium colorways like *Neon Sapphire*, *Cosmic Noir*, *Sunset Rose*, and *Emerald Oasis*. 
* **Two-Dot Unified Interface:** Theme selectors utilize a minimal two-dot color indicator layout across onboarding and settings menus for absolute visual consistency.
* **Fluid Streak Feedback:** Achieving tracking milestones triggers a tailored, full-screen interactive fluid-dynamic flame animation that sweeps up from the bottom boundary of the device and extinguishes smoothly over a balanced 5-second window.
* **Floating Dock Navigation:** A minimal navigation bar that keeps labels hidden to avoid UI clutter, expanding smoothly only for the active focus icon.

---

## 🛠️ Built With
* **Language:** Kotlin
* **UI Framework:** Jetpack Compose (Declarative UI)
* **Local Storage:** SQLite via Room Database / SharedPreferences
* **State Management:** Jetpack ViewModel

## 📜 License
This project is open-source and licensed under the **GNU General Public License v3.0 (GPL-3.0)**. See the `LICENSE` file for details.
