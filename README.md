# NPUDebugger — Real-Time Mobile On-Device Profiler & Security Auditor

> **Phase 1 Hackathon Submission**
> This repository contains the standalone Android on-device agent (UI & Mock Telemetry). The local Python FastAPI and React bridge integration (Office Kit) is slated for Phase 2 development.

## Screenshots

| Performance Dashboard | Security Auditor |
| :---: | :---: |
| <img width="200" height="500" alt="Performance Dashboard" src="https://github.com/user-attachments/assets/1bc142d9-4027-4f0f-9cc2-08b644c6b7de" /> | <img width="200" height="500" alt="Security Auditor" src="https://github.com/user-attachments/assets/e2bd84ec-8071-4b39-8aa1-3dff05fa525b" /> |

**The Problem**
Mobile developers lack visibility into on-device neural network latency, battery consumption, and privacy or data leaks during live application runtime. 

**The Solution**
A comprehensive developer suite running locally on the iQOO device that profiles app execution, monitors memory leak vectors, and utilizes local AI to suggest real-time code patches.

**Core Features**
* **Hardware Profiling:** A responsive dashboard tracking NPU Utilization (%), Thermal Load (°C), RAM Usage (MB), Battery Drain (mW), and Inference Latency (ms).
* **Security Auditor:** A dedicated system integrity tab logging intercepted anomalies such as unauthorized background location access and unencrypted payload transmissions.
* **AI Code Fixer:** An automated anomaly detection engine that triggers a Material 3 bottom sheet providing localized Kotlin patch suggestions when memory spikes (e.g., unoptimized tensor allocations) occur.
* **Telemetry Terminal:** An auto-scrolling log console displaying raw JSON metrics.

**Tech Stack**
* **Language:** Kotlin
* **UI Framework:** Jetpack Compose (Material 3)
* **Concurrency:** Kotlin Coroutines & StateFlow
* **Networking:** OkHttp & WebSockets (Architecture prepared for Phase 2)

**How to Run (Phase 1 Demo Mode)**
For the Phase 1 video submission, the application relies on an internal loop generating simulated hardware telemetry to demonstrate the UI and state management without requiring the external laptop backend.

1. Clone the repository:
   `git clone https://github.com/Sakshichou/NPUDebugger-Agent.git`
2. Open the directory in **Android Studio**.
3. Allow Gradle to sync and build the project.
4. Launch the application on an Android Emulator or physical device.
5. Navigate to the **Performance** tab and toggle **Enable NPUDebugger Agent** to initiate the mock data stream.
6. Observe the terminal logs and wait approximately 10 seconds for the simulated memory spike to trigger the AI Insight modal.
