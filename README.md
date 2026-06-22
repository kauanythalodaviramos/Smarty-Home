# 🏠 Smarty Home

**Intelligent Environment Monitoring with Arduino and Java**

An IoT system prototype for reading temperature, humidity, and luminosity, integrating hardware (Arduino), data processing (Java), and a mobile Android interface.

---

## 📖 About the Project

Smarty Home is a smart home monitoring prototype that connects physical sensors to a mobile interface. The system collects environmental data — temperature, humidity, and light intensity — from an Arduino board and displays it in real time through an Android application built in Java.

This project was developed as a study in IoT integration, bridging embedded hardware with a native mobile interface.

---

## ✨ Features

- 🌡️ Real-time temperature monitoring
- 💧 Humidity level reading
- 💡 Luminosity (light intensity) sensing
- 📱 Android mobile interface for data visualization
- 🔌 Arduino hardware integration for sensor data collection

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Hardware | Arduino |
| Mobile App | Java (Android) |
| Build System | Gradle (Kotlin DSL) |
| IDE | Android Studio |

---

## 📁 Project Structure

```
Smarty-Home/
├── app/                   # Android application source code
│   └── src/
│       └── main/
│           ├── java/      # Java source files (Activities, logic)
│           └── res/       # Layouts, drawables, strings
├── gradle/                # Gradle wrapper files
├── build.gradle.kts       # Project-level build configuration
├── settings.gradle.kts    # Module settings
└── gradle.properties      # Gradle properties
```

---

## 🚀 Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) (latest stable version recommended)
- Android SDK (API Level 21 or higher)
- An Arduino board with temperature, humidity, and luminosity sensors
- Java Development Kit (JDK 11+)

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/kauanythalodaviramos/Smarty-Home.git
   ```

2. Open the project in Android Studio:
   - File → Open → select the `Smarty-Home` folder

3. Let Gradle sync and resolve all dependencies.

4. Connect your Android device or start an emulator.

5. Run the app:
   - Click **Run ▶** or press `Shift + F10`

### Arduino Setup

- Wire up your DHT sensor (temperature & humidity) and an LDR (luminosity) to your Arduino board.
- Flash the corresponding Arduino sketch to the board.
- Connect the Arduino to your device via USB/Bluetooth/Wi-Fi depending on your configuration.

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!
Feel free to open an [issue](https://github.com/kauanythalodaviramos/Smarty-Home/issues) or submit a pull request.

---

## 👩‍💻 Author

**Charlotte**
- GitHub: [@kauanythalodaviramos](https://github.com/kauanythalodaviramos)

---

## 📝 License

This project is open source. Feel free to use and adapt it for your own learning and projects.

---

> *Smarty Home — making your environment smarter, one sensor at a time.*
