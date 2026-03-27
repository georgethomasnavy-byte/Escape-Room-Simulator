# 🚪 Escape Room Simulator

A 2-level escape room puzzle game built with **Java** and **JavaFX**.  
Search for clues, manage your hint currency, and escape before the timer runs out!

---

## 🎮 Gameplay

### Level 1 – The Awakening
- Search under the **bed** to find a hidden **Access Card**
- Use the Access Card to **unlock the door** and advance to Level 2

### Level 2 – The Office
- Search the **table** to find a secret **code**
- Enter the code into the **door keypad** to escape and win

---

## ✨ Features

- 🖼️ Background images that change when doors are unlocked
- ⏱️ 5-minute countdown timer per level
- 💡 TEU hint system — spend TEUs to get hints when you're stuck
- 🔔 Animated on-screen notifications
- 🔢 Password input dialog for Level 2

---

## 🛠️ Built With

- Java
- JavaFX

---

## ▶️ How to Run

1. Make sure you have **JDK 11+** and **JavaFX** installed
2. Clone this repository:
   ```
   git clone https://github.com/georgethomasnavy-byte/Escape-Room-Simulator.git
   ```
3. Compile and run:
   ```
   javac --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml Main.java
   java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml Main
   ```

---

## 📁 Project Structure

```
Escape-Room-Simulator/
├── Main.java
└── images/
    ├── main_menu.jpg
    ├── room_closed.jpg
    ├── room_open.jpg
    ├── room2_closed.jpg
    └── room2_open.jpg
```

---

## 👨‍💻 Developer

**George Thomas**  
S4 CSE Student — Saintgits College of Engineering, Kottayam
