# IdleGuard 

**IdleGuard** is a personalized, minimalist focus companion and weekly task manager built with modern Android development practices, **Jetpack Compose (Material 3)**, and **Room Database**. Specifically tailored for high-focus personal productivity on devices like the **Samsung Galaxy S25 Ultra**, it keeps you accountable with persistent lock screen notifications and hourly refocus alerts.

---

##  Features

### 1. Home — Minimal Focus Timer
* **15-Minute Default:** Launches immediately into a clean, distraction-free countdown timer set to 15 minutes by default.
* **Custom Scroll Picker (Hours : Minutes : Seconds):** Tap the timer or the *Custom Scroll Picker* button to open a wheel-style scroll picker where you can dial in any exact duration (e.g., 20m, 35m, 1h 15m).
* **Quick Presets:** Instant-switch chips for 15m, 20m, 30m, and 45m.
* **Tactile Controls:** Start, pause, reset, and instant `+5m` snooze buttons.
* **Completion Dialog:** When time expires, easily snooze for `+5m`, `+10m`, or `+15m`, or reset.

### 2. Lock Screen & Ongoing Notifications (Optimized for Samsung One UI)
* **Persistent Lock Screen Notification:** An ongoing, high-priority foreground notification that displays live timer progress directly on your **Samsung S25 Ultra Lock Screen** and **Always-On Display (AOD)** without unlocking the phone.
* **Hourly Refocus Reminders:** If the timer is turned off or idle, an hourly nudge reminds you to start a session and regain momentum.
* **Quick Actions:** Pause, resume, and snooze right from the notification drawer or lock screen widget.

### 3. Weekly Task Manager
* **Monday to Sunday View:** Easily switch between all 7 days of the week using the horizontal day selector.
* **Independent Daily Lists:** Set specific goals and to-dos for each day.
* **Accidental-Click Protection:** 
  * Tapping the **circular check button** ticks off / completes the task with strikethrough.
  * Tapping the **actual task text** opens the **Edit Task** dialog to modify task details without toggling completion.
  * Tapping the **bin icon** deletes the task immediately.
* **Daily Progress Bar:** Shows the percentage and count of completed tasks for each day.

### 4. Settings & Customization
* **Repeat Interval:** Choose how frequently nudges and repeat alerts trigger (**5m, 10m, 15m, 20m**).
* **Dynamic Color Themes:** Instantly switch between 6 themes:
  *  **Emerald Focus** (Default)
  *  **Sky Minimal**
  *  **Deep Violet**
  *  **Warm Amber**
  *  **Crimson Rose**
  *  **Slate Titanium (Monochrome)**

---

## 🛠️ Tech Stack & Architecture

* **UI:** Jetpack Compose with Material Design 3 (M3)
* **Language:** Kotlin
* **Architecture:** MVVM (Model-View-ViewModel) + Clean Architecture
* **Persistence:** Room Database (`AppDatabase`, `TaskDao`, `TaskEntity`)
* **Background Processing:** Android Foreground Service (`IdleTrackingService`), BroadcastReceivers, and AlarmManager
* **State Management:** Kotlin StateFlow & Coroutines

---

##  How to Open and Run in VS Code

### Prerequisites
1. **JDK 17 or JDK 21** installed (`java -version`).
2. **Android SDK & Command-Line Tools** (or Android Studio installed to supply the SDK).
3. **VS Code Extensions:**
   * *Kotlin* (Mathias Frohlich) or *Kotlin Language*
   * *Android iOS Emulator* or *Android* extension (optional)

### Steps
1. **Clone or Download the Repository:**
   Download the project ZIP from AI Studio (via Settings -> Export / Push to GitHub) and unzip it into a folder.
2. **Open in VS Code:**
   Launch VS Code and choose **File > Open Folder...**, then select the project root.
3. **Configure Android SDK Environment:**
   Ensure `ANDROID_HOME` (or `ANDROID_SDK_ROOT`) is configured in your shell environment:
   ```bash
   # macOS / Linux
   export ANDROID_HOME=$HOME/Android/Sdk
   export PATH=$PATH:$ANDROID_HOME/platform-tools

   # Windows PowerShell
   $env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
   $env:Path += ";$env:ANDROID_HOME\platform-tools"
   ```
4. **Compile & Test via Terminal:**
   Open the integrated VS Code terminal (`Ctrl+` ` or `Cmd+` `) and run:
   ```bash
   # Check build
   ./gradlew assembleDebug

   # Run Unit & Robolectric Tests
   ./gradlew testDebugUnitTest
   ```
5. **Run on an Connected Device or Emulator:**
   ```bash
   ./gradlew installDebug
   ```

*(Alternatively, you can open this directory directly in **Android Studio**, which will automatically configure Gradle and provide a 1-click **Run (▶)** button).*

---

##  How to Install Directly on Your Phone (Samsung Galaxy S25 Ultra)

You can install IdleGuard onto your Samsung Galaxy S25 Ultra using either **Direct APK Download** or **USB / Wireless Debugging**.

### Method 1: Export & Download APK from AI Studio (Easiest)
1. In the AI Studio Build interface, click on the **Settings / Export** menu in the top right.
2. Select **Generate APK** (or **Download Project ZIP**).
3. Transfer the generated `app-debug.apk` to your Galaxy S25 Ultra:
   * Send it via **Google Drive**, **Quick Share**, **WhatsApp/Telegram Saved Messages**, or email it to yourself.
4. On your Samsung S25 Ultra:
   * Open the **My Files** app > **Downloads** (or **Installation files**).
   * Tap `app-debug.apk`.
   * If prompted with *"For your security, your phone is not allowed to install unknown apps from this source"*, tap **Settings** and toggle **Allow from this source**.
   * Tap **Install** and then **Open**.

---

### Method 2: Install via USB / Wireless Debugging (Developer Mode)

#### 1. Enable Developer Options on your Samsung S25 Ultra:
1. Open **Settings** on your phone.
2. Scroll to the bottom and select **About phone** > **Software information**.
3. Tap **Build number** 7 times until you see the message: *"Developer mode has been turned on."*
4. Go back to **Settings** > **Developer options**.
5. Turn on **USB debugging** (and optionally **Wireless debugging**).

#### 2. Connect and Install via Terminal:
1. Connect your Samsung S25 Ultra to your computer using a USB-C cable (select **Allow USB debugging** on your phone when the popup appears).
2. Verify connection in your terminal:
   ```bash
   adb devices
   ```
   *(Your Samsung device should appear with `device` next to it).*
3. Build and install directly with one command:
   ```bash
   ./gradlew installDebug
   ```
4. IdleGuard will now be installed and ready on your home screen!

---

##  Lock Screen & Samsung Permissions Tip

To make sure your notifications remain permanently active on One UI:
1. Open phone **Settings** > **Apps** > **IdleGuard**.
2. Tap **Notifications**:
   * Ensure **Allow notifications** is ON.
   * Under Notification types, ensure **Lock screen** is set to **Show content**.
3. Tap **Battery**:
   * Change from *Optimized* to **Unrestricted** so One UI doesn't pause the background timer service.

---

##  License
Personal Project — Created for personalized productivity and habit tracking.
