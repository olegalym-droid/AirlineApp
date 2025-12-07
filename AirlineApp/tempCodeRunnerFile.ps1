# Путь к проекту
$projectPath = "C:\Users\Lymar\Desktop\AirlineApp"

# Пути к библиотекам Log4J
$log4jApi = "$projectPath\lib\log4j-api-2.25.2.jar"
$log4jCore = "$projectPath\lib\log4j-core-2.25.2.jar"

# Путь к исходникам и папке bin
$srcPath = "$projectPath\src"
$binPath = "$projectPath\bin"

# Создаем bin если нет
if (!(Test-Path $binPath)) {
    New-Item -ItemType Directory -Path $binPath
}

# Формируем classpath для компиляции и запуска
$classpath = ".;$binPath;$log4jApi;$log4jCore"

Write-Host "Компиляция проекта..."

# Компиляция всех Java файлов из src
Get-ChildItem -Recurse -Path $srcPath -Filter *.java | ForEach-Object {
    javac -cp $classpath -d $binPath $_.FullName
}

if ($LASTEXITCODE -ne 0) {
    Write-Host "Ошибка компиляции!"
    exit $LASTEXITCODE
}

Write-Host "Запуск проекта..."

# Формируем путь к конфигу с протоколом file://
$logConfig = "file:///$srcPath/log4j2.xml"
$logConfig = $logConfig -replace "\\", "/"

# Запуск приложения с конфигурацией Log4J
java -cp $classpath "-Dlog4j.configurationFile=$logConfig" ui.MainAppSwing
