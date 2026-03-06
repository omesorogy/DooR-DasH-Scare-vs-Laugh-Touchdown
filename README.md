# DooR DasH – Scare vs Laugh Touchdown

A strategic board game inspired by the world of *Monsters Inc.*, where two monsters compete to collect energy and reach Boo’s Door first. Players must balance movement, strategy, and energy management while navigating a dynamic board full of doors, special cells, and powerful cards.

This project is a **fully functional JavaFX implementation** of the game with interactive gameplay, dynamic board updates, and character abilities.

---

## Overview

Monstropolis runs on energy collected from children. Traditionally this energy came from **screams**, but a revolutionary discovery revealed that **laughter produces significantly more energy**.

In **DooR DasH**, players compete as monsters representing either:

* **Scarers** – specialists in collecting scream energy
* **Laughers** – pioneers of the new laughter energy system

The goal is to **reach Boo’s Door while collecting enough energy to win the competition**.

---

## Game Objective

A player wins when **both conditions are satisfied**:

* The monster reaches **cell 99 (Boo’s Door)**
* The monster has **at least 1000 energy**

Reaching the final cell without sufficient energy means the game continues until the requirement is met.

---

## Features

* Fully interactive **JavaFX graphical interface**
* **100-cell zigzag board** similar to Snakes and Ladders
* **Unique monsters** with special abilities and attributes
* **Strategic energy management**
* **Random card system**
* **Transport mechanics** that move players forward or backward
* **Team-based energy effects**
* **Power-ups and shields**
* **Turn-based gameplay system**

---

## Game Board

The board contains **100 cells** arranged in a zigzag pattern.

Cell types include:

| Cell Type           | Description                                                     |
| ------------------- | --------------------------------------------------------------- |
| Doors               | Energy sources that benefit or penalize teams depending on role |
| Monster Cells       | Allow monsters to activate abilities without energy cost        |
| Conveyor Belts      | Move players forward                                            |
| Contamination Socks | Move players backward and reduce energy                         |
| Card Cells          | Draw a random card with special effects                         |
| Normal Cells        | No effect                                                       |

---

## Cards System

The game contains **25 cards** shuffled at the beginning.

Card types include:

* **Swapper Cards** – swap positions with the opponent
* **Energy Steal Cards** – steal energy from the opponent
* **Start Over Cards** – send a player back to the starting cell
* **Shield Cards** – protect from the next negative energy effect
* **Confusion Cards** – temporarily swap monster roles

Cards are **reshuffled when the deck becomes empty**.

---

## Monsters

Each monster belongs to a **type** with unique gameplay mechanics.

### Monster Types

#### Dasher

* Faster movement
* Dice movement doubled
* Special ability increases speed further

#### Dynamo

* Energy gains and losses are doubled
* Special ability freezes the opponent for one turn

#### Multitasker

* Slower movement
* Energy gains and losses receive bonus energy
* Special ability restores normal speed temporarily

#### Schemer

* Slight advantage in all energy changes
* Special ability steals energy from all monsters

Each monster also has:

* A **team** (Scarer or Laugher)
* **Starting energy**
* A **special power-up ability**

---

## Turn Flow

Each turn follows this sequence:

1. **Power-up Phase** (optional)
   Player may activate their monster ability if they have enough energy.

2. **Dice Roll**
   A six-sided dice determines movement.

3. **Movement Attempt**
   Player moves forward based on the roll.

4. **Cell Action**
   The landed cell activates its effect.

5. **Board Update**
   Positions and energy values are updated.

6. **Turn Switch**
   Control passes to the other player unless the win condition is met.

---

## Technologies Used

* **Java**
* **JavaFX**
* **Object-Oriented Design**
* **Event-Driven Programming**
* **Game State Management**

---


## Contributors

* **Ali Ayman**
* **Amr Salem**
* **Omar El-Serougi**

---


