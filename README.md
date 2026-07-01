# PukiBlox Source Code

Исходный код игры **PukiBlox**.

## Требования

Для сборки проекта потребуется:

* **Java Development Kit (JDK) 8** (Amazon Corretto 8, Oracle JDK 8, OpenJDK 8 или любой совместимый JDK);
* **IntelliJ IDEA Community/Ultimate** (рекомендуется использовать версию 2024.2.6, но если у вас не сильно отличается версия, то тоже можно использовать);

## Открытие проекта

1. Склонируйте репозиторий:

```bash
git clone https://github.com/dani1701d/pukiblox-src.git
```

2. Откройте папку проекта в **IntelliJ IDEA**.

3. Дождитесь, пока IDE проиндексирует проект.

## Сборка

1. Откройте проект.
2. Выберите **File → Project Structure**
3. Затем выберите **Project Settings → Artifact**
4. Нажмите +, выберите **JAR → From modules with dependencies**.
5. В открывшемся окне Module оставьте таким же, а Main Class поставьте "defpackage.Dolinablox". Нажмите OK.
6. В полученном jar-файле создайте директорию META-INF, а в ней создайте и укажите файл с расположением **"[Ваш проект]/app/src/main/java/defpackage/manifests/game/MANIFEST.MF"**
7. Также добавьте папки **sounds** и **sprites**. В папку **sounds** добавьте файлы из **"[Ваш проект]/app/src/main/resources/sounds"**, а в папку **sprites** добавьте файлы из **"[Ваш проект]/app/src/main/resources/sounds"**.
8. Нажмите Apply. Теперь повторите 4 шаг, в окне Module оставьте таким же, а Main Class поставьте "defpackage.server.ServerMain". Нажмите OK.
9. В полученном jar-файле создайте директорию META-INF, а в ней создайте и укажите файл с расположением **"[Ваш проект]/app/src/main/java/defpackage/manifests/server/MANIFEST.MF"**
10. Нажмите Apply и OK.

11. Выберите **Build → Build Artifacts → All Artifacts → Build**
12. JAR-файлы собраны.

## Запуск

После сборки перейдите в папку с готовым JAR-файлом и выполните:

```bash
java -jar Dolinablox.jar
```

## Структура проекта

```
src/        - исходный код
resources/  - ресурсы игры (текстуры, шрифты, звуки и т.д.)
lib/        - сторонние библиотеки
```

## Используемые технологии

* Java
* Swing/AWT

## Лицензия

На данный момент лицензия не указана.
