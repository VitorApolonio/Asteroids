# Asteroids
An Asteroids clone created in Java.

![screenshot-1](https://github.com/user-attachments/assets/b5d51888-b105-4b45-88b3-6d7e1dcd2312)


The UI is made with [JavaFX](https://openjfx.io/), a Java library meant for creating GUI apps.

Splash screen and icon were made in Photoshop by me.

# Features
- Score counter
- Title screen
- Game over screen
- Pausing
- Saving screenshots
- Splash screen
- Replay functionality

# Controls
- **A / D or Left Arrow / Right Arrow:** Turn ship
- **W or Up Arrow:** Accelerate ship
- **Space:** Shoot
- **Esc:** Pause game
- **P:** Print screenshot (saved to scr folder)

# How to build
To build the app, you need a JDK version 17 or higher than includes JavaFX (such as [LibericaJDK](https://bell-sw.com/pages/downloads/#jdk-21-lts)) and [Maven](https://maven.apache.org/download.cgi).

Then, just clone the repo and run `mvn clean package` to create a JAR file and an installer for Windows.
