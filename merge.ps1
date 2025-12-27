$outputFile = "AllJavaCode.txt"
$count = 0

# Папки, которые нужно игнорировать (библиотеки, зависимости и т.д.)
$excludeFolders = @(
    "lib", "libs", "library", "libraries",
    "vendor", "dependencies", "dependency",
    ".gradle", ".m2", "node_modules",
    "target", "build", "bin", "out",
    "test", "tests", "__tests__", "test.*"
)

# Паттерны для игнорирования файлов
$excludePatterns = @(
    "*Test*.java", "*test*.java", 
    "*Spec*.java", "*Fixture*.java"
)

# Удаляем старый файл, если существует
if (Test-Path $outputFile) {
    Remove-Item $outputFile
}

Write-Host "Объединение всех Java файлов (игнорируя библиотеки)..." -ForegroundColor Green

# Получаем все .java файлы, исключая системные папки
$javaFiles = Get-ChildItem -Recurse -Filter *.java | 
    Where-Object {
        $filePath = $_.FullName
        $shouldInclude = $true
        
        # Проверяем, находится ли файл в исключенной папке
        foreach ($excludeFolder in $excludeFolders) {
            if ($filePath -match "\\$excludeFolder\\" -or 
                $filePath -match "/$excludeFolder/") {
                $shouldInclude = $false
                break
            }
        }
        
        # Проверяем соответствие паттернам исключения
        if ($shouldInclude) {
            foreach ($pattern in $excludePatterns) {
                if ($_.Name -like $pattern) {
                    $shouldInclude = $false
                    break
                }
            }
        }
        
        $shouldInclude
    } | 
    Sort-Object FullName

Write-Host "Найдено Java файлов (после фильтрации): $($javaFiles.Count)" -ForegroundColor Yellow

foreach ($file in $javaFiles) {
    $count++
    $filename = $file.Name
    $relativePath = $file.FullName.Replace((Get-Location).Path + "\", "")
    
    Write-Host "Обрабатывается: $relativePath" -ForegroundColor Gray
    
    # Добавляем номер в формате "4.X Filename.java:"
    "4.$count. $filename`:" | Out-File -Append -FilePath $outputFile -Encoding UTF8
    "Путь: $relativePath" | Out-File -Append -FilePath $outputFile -Encoding UTF8
    
    # Добавляем содержимое файла
    Get-Content $file.FullName -Encoding UTF8 | Out-File -Append -FilePath $outputFile -Encoding UTF8
    
    # Добавляем разделитель между файлами (кроме последнего)
    if ($count -lt $javaFiles.Count) {
        "`n" + ("=" * 80) + "`n" | Out-File -Append -FilePath $outputFile -Encoding UTF8
    }
}

Write-Host "`nГотово!" -ForegroundColor Green
Write-Host "Объединено файлов: $count" -ForegroundColor Yellow
Write-Host "Результат сохранен в: $outputFile" -ForegroundColor Yellow

# Показываем размер файла
if (Test-Path $outputFile) {
    $size = (Get-Item $outputFile).Length / 1MB
    Write-Host "Размер файла: $([math]::Round($size, 2)) MB" -ForegroundColor Cyan
} else {
    Write-Host "Файл не был создан (возможно, не найдено Java файлов)" -ForegroundColor Red
}