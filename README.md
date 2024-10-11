# Asteroids
An Asteroids clone created in Java.

![screenshot_10](https://github.com/user-attachments/assets/1b24fa27-9b3a-4e97-a21a-8ee08688ea0d)
![screenshot_2](https://github.com/user-attachments/assets/b28b4f10-76f4-4e4e-957a-824bbb8c0da5)

The UI is made with [JavaFX](https://openjfx.io/), a Java library meant for creating GUI apps.

Splash screen and icon were made in Photoshop by me.

All sound effects were obtained from [Freesound](https://freesound.org/) and are CC0 licensed.

# Features
- Score counter
- Leaderboards
- Saving and loading scores from a file
- Title screen
- Game over screen
- Pausing
- Saving screenshots
- Splash screen
- Replay functionality
- Sound effects

# Controls
- **A / D or Left Arrow / Right Arrow:** Turn ship
- **W or Up Arrow:** Accelerate ship
- **Space:** Shoot
- **Esc:** Pause game
- **P:** Print screenshot (saved to scr folder)

# How to build
To build the app, you need a JDK version 17 or higher than includes JavaFX (such as [LibericaJDK](https://bell-sw.com/pages/downloads/#jdk-21-lts)) and [Maven](https://maven.apache.org/download.cgi).

Then, just clone the repo and run `mvn clean package` to create a JAR file and an installer for Windows.
