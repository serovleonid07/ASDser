Лиса и гуси (курсовая АСД, вариант 17). Java 17+, Swing.

КАК ЗАПУСТИТЬ
1. Двойной щелчок по Play.bat (нужна установленная Java 17 или новее).
   Можно и двойным щелчком по FoxAndGeese.jar.
2. Настоящая программа .exe с иконкой лисы (Java внутри, на другом компьютере Java не нужна):
   двойной щелчок по build_exe.bat (нужен JDK 17+). Через минуту появится папка exe\FoxAndGeese,
   в ней FoxAndGeese.exe. Из неё можно сделать ярлык на рабочий стол.
3. Из исходников: java -cp out frontend.Main (после сборки: mkdir out &&
   javac -encoding UTF-8 -d out src/backend/*.java src/frontend/*.java,
   запускать из папки course: картинки лежат в src/frontend/images).

ЕСЛИ МЕНЯЛИ КОД: build.bat (Windows) или ./build.sh (Linux, macOS) заново собирают FoxAndGeese.jar.

ФАЙЛЫ
src/backend/        бэк-энд: правила, поле, ходы, оценка, MiniMax, альфа-бета, таблица позиций, бот, контроллер
src/frontend/       фронт-энд: окна, меню, рисование поля, стили, курсоры, фоны, настройки
                    (src/frontend/images: картинки и фоны, src/frontend/images/cursors: курсоры)
icon.ico            иконка для .exe
FoxAndGeese.jar     готовая игра
Play.bat            запуск двойным щелчком
build.bat, build.sh сборка jar
build_exe.bat       сборка .exe

Проверка без окна (компьютер против компьютера + сравнение MiniMax и альфа-бета):
java -cp out backend.ConsoleDemo 6
