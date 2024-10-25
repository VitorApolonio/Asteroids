## [> Versão em português <](README.pt-BR.md)
___
# Asteroids
The game Asteroids recreated in Java.

![screenshot_10](https://github.com/user-attachments/assets/1b24fa27-9b3a-4e97-a21a-8ee08688ea0d)
![screenshot_2](https://github.com/user-attachments/assets/b28b4f10-76f4-4e4e-957a-824bbb8c0da5)

This project was coded entirely in Java, using the library [JavaFX](https://openjfx.io/) for the graphic components.

The splash screen and app icon were made in Photoshop by me.

All sound effects were obtained from [Freesound](https://freesound.org/), and are CC0 licensed.

# Features
- Score counter
- Leaderboards (saved and loaded automatically from a file)
- Saving screenshots
- Splash screen
- Sound effects
- Resolution selection

# Controls
- **A / D or Left Arrow / Right Arrow:** Turn ship
- **W or Up Arrow:** Accelerate ship
- **Space bar:** Shoot
- **ESC:** Pause game
- **P:** Print screenshot (saved to scr folder)
- **ALT+ENTER:** Toggle full screen

# How to build
To build the game, you'll need a JDK updated to version 17 or higher that includes JavaFX (such as [LibericaJDK](https://bell-sw.com/pages/downloads/#jdk-21-lts)), and [Maven](https://maven.apache.org/download.cgi).

To create a Windows installer for the game (and a JAR file), just clone this repository and run maven with the package goal:

    git clone https://github.com/VitorApolonio/Asteroids
    cd Asteroids
    mvn package

The EXE can then be found on the root of the project folder, while the JAR will be inside the `target` folder.
