## [> English version <](README.md)
___
# Asteroids
Uma réplica do jogo Asteroids feita em Java.


![screenshot_16](https://github.com/user-attachments/assets/fb716666-91f2-41f2-b05b-3dde22503266)

![screenshot_15](https://github.com/user-attachments/assets/ab6f4ce2-9f25-4082-ab81-6764786fa5c7)

![screenshot_17](https://github.com/user-attachments/assets/f870e6c8-54ce-44f7-aebd-cf443ca92e40)

Esse projeto foi programado inteiramente em Java, utilizando a biblioteca [JavaFX](https://openjfx.io/) para componentes gráficos.

A tela de splash e o ícone foram feitos por mim com o Photoshop.

Todos os efeitos sonoros foram obtidos do site [Freesound](https://freesound.org/), e são licenciados com a licença CC0.

# Funcionalidades
- Marcador de pontos
- Melhores pontuações (salvas e carregadas de um arquivo)
- Capturas de tela
- Tela de splash
- Efeitos sonoros
- Seleção de resolução

# Controles
- **A / D ou Seta para Esquerda / Seta para Direita:** Virar nave
- **W ou Seta para Cima:** Acelerar nave
- **Barra de espaço:** Atirar
- **ESC:** Pausar o jogo
- **P:** Salvar captura de tela
- **ALT+ENTER:** Tela cheia

# Como compilar
Para compilar o jogo, é necessário ter um JDK instalado na versão 17 ou mais recente que inclui o JavaFX (como o [LibericaJDK](https://bell-sw.com/pages/downloads/#jdk-21-lts)), e o [Maven](https://maven.apache.org/download.cgi).

Depois é só clonar esse repositório e executar o maven com o "goal" package:

    git clone https://github.com/VitorApolonio/Asteroids
    cd Asteroids
    mvn package

O arquivo EXE pode então ser encontrado na pasta raíz do projeto, enquanto o arquivo JAR estará na pasta `target`.
