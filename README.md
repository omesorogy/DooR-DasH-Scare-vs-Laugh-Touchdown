# 🎮 DooR DasH: Scare vs Laugh Touchdown

A competitive Java/JavaFX board game inspired by the world of Monsters, Inc., where Scarers and Laughers battle to dominate the Floor and power Monstropolis.

---

# 📖 About the Game

**DooR DasH: Scare vs Laugh Touchdown** is a turn-based strategic board game set in the Monsters, Inc. universe. Players compete in an energy-collecting race across a dangerous 100-cell Floor filled with doors, conveyor systems, contamination hazards, monster encounters, and mysterious cards.

The game revolves around the revolutionary conflict between:

* ⚡ Traditional scream energy (Scarers)
* 😂 Modern laughter energy (Laughers)

Inspired by the discovery that laughter generates significantly more energy than screams, monsters now compete to prove which method truly powers Monstropolis best.

Players must carefully balance:

* Movement strategy
* Energy management
* Card usage
* Character abilities
* Risk and reward decisions

The first monster to reach **Boo’s Door** with at least **1000 energy** wins the game.

---

# ✨ Features

## 🎲 Core Gameplay

* Turn-based board game mechanics
* 100-cell zigzag game board
* Dice-based movement system
* Energy collection and management
* Win-condition progression system
* Competitive player vs opponent gameplay

---

## 👹 Monster System

The game includes 8 unique monsters divided into:

* SCARERS
* LAUGHERS

Each monster belongs to a specific monster class with unique gameplay mechanics.

### Monster Types

* ⚡ Dasher
* 🔋 Dynamo
* 🧠 Schemer
* 🎯 MultiTasker

Each monster includes:

* Passive abilities
* Unique active powerups
* Different starting energy values
* Strategic strengths and weaknesses

---

## 🃏 Card System

The game contains a fully implemented card system including:

| Card         | Effect                             |
| ------------ | ---------------------------------- |
| Swapper      | Swap positions with opponent       |
| Energy Steal | Steal opponent energy              |
| Start Over   | Send player/opponent back to start |
| Shield       | Block negative energy effects      |
| Confusion    | Swap player roles temporarily      |

Cards are shuffled into a deck and dynamically drawn throughout gameplay.

---

## 🚪 Board Cell System

The board contains multiple interactive cell types:

### 🚪 Door Cells

* SCARER doors
* LAUGHER doors
* Team-wide energy effects
* Activated/exhausted door system

### 👹 Monster Cells

* Free monster power activation
* Energy swapping interactions

### 🚛 Transport Cells

* Conveyor Belts (positive movement)
* Contamination Socks (negative movement + energy loss)

### 🃏 Card Cells

* Random card drawing and execution

### 🟨 Normal Cells

* Safe cells with no effects

---

# 🎨 GUI & Visual Features

Built using JavaFX with:

* Animated interfaces
* Interactive game board
* Character rendering
* Winning screens
* Pop-up effects
* Smooth gameplay transitions
* Dynamic overlays and effects

---

# 🔊 Audio Features

The game includes:

* Background music
* Gameplay sound effects
* Event-triggered audio
* Integrated audio resource management

---

# 🧠 OOP & Software Design

The project heavily applies Object-Oriented Programming concepts including:

* Encapsulation
* Inheritance
* Polymorphism
* Abstraction
* Modular architecture
* Separation of concerns

The codebase is divided into:

* Game engine logic
* GUI management
* Controllers
* Models
* Data loading
* Resource handling
* Audio management

---

# 🛠️ Technologies Used

| Technology  | Purpose                      |
| ----------- | ---------------------------- |
| Java 8      | Core programming language    |
| JavaFX      | GUI framework                |
| Eclipse IDE | Development environment      |
| Launch4j    | Windows executable packaging |
| CSV         | Game data management         |

---


# 🚀 Running the Game

## Option 1 — Run From Source Code

### Requirements

* Java 8
* JavaFX
* Eclipse IDE (recommended)

### Steps

1. Clone or download the repository
2. Import the project into Eclipse
3. Configure JavaFX libraries
4. Run the main application class

---

## Option 2 — Run the Executable (.exe)

The repository includes a Windows executable version of the game.

### Requirements

* Java installed on the system

Run:

```text
Door Dash.exe
```

---

## Option 3 — Standalone Packaged Version (Recommended)

A standalone version is provided in the GitHub Releases section.

This version includes:

* Executable file
* Bundled Java Runtime Environment (JRE)

✅ No Java installation required.

### Steps

1. Download the standalone ZIP release
2. Extract the ZIP completely
3. Open the extracted folder
4. Run:

```text
Door Dash.exe
```

⚠️ Important:

* Do not run directly from inside the ZIP file
* Keep the `jre` folder beside the executable

---


# 🧪 Testing

The game was tested for:

* GUI responsiveness
* Gameplay correctness
* Resource loading
* Audio playback
* Executable portability
* Standalone runtime compatibility

---

# 📦 Releases

Standalone builds and executable releases are available here:

https://github.com/omesorogy/DooR-DasH-Scare-vs-Laugh-Touchdown/releases

---

# 👨‍💻 Authors

Ali Ayman 

Omar Elserougi

Amro Salem


