## [> Versão em português <](README.pt-BR.md)
___
# Asteroids
The game Asteroids recreated in Java.

![screenshot_16](https://github.com/user-attachments/assets/fb716666-91f2-41f2-b05b-3dde22503266)

![screenshot_32](https://github.com/user-attachments/assets/5c211ab4-0abd-4917-8c1d-b0948c7be5e1)

![screenshot_17](https://github.com/user-attachments/assets/f870e6c8-54ce-44f7-aebd-cf443ca92e40)

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
- **ESC or PAUSE:** Pause game
- **P:** Print screenshot (saved to scr folder)
- **ALT+ENTER:** Toggle full screen

# How to build
To build the game, you'll need a JDK updated to version 17 or higher that includes JavaFX (such as [LibericaJDK](https://bell-sw.com/pages/downloads/#jdk-21-lts)), and [Maven](https://maven.apache.org/download.cgi).

To create a Windows installer for the game (and a JAR file), just clone this repository and run maven with the package goal:

    git clone https://github.com/VitorApolonio/Asteroids

    cd Asteroids
    mvn package

The EXE can then be found on the root of the project folder, while the JAR will be inside the `target` folder.
