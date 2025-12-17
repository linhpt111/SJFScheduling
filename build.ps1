# Script to download and run Maven
# Run this in PowerShell if you don't have Maven installed

$mavenVersion = "3.9.6"
$mavenDir = "$env:TEMP\apache-maven-$mavenVersion"
$mavenBin = "$mavenDir\bin\mvn.cmd"

if (-not (Test-Path $mavenBin)) {
    Write-Host "Downloading Maven $mavenVersion..."
    $url = "https://dlcdn.apache.org/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip"
    $zipFile = "$env:TEMP\maven.zip"

    try {
        Invoke-WebRequest -Uri $url -OutFile $zipFile -UseBasicParsing
        Write-Host "Extracting..."
        Expand-Archive -Path $zipFile -DestinationPath $env:TEMP -Force
        Remove-Item $zipFile
        Write-Host "Maven installed to: $mavenDir"
    } catch {
        Write-Host "Failed to download Maven. Please download manually from https://maven.apache.org/download.cgi"
        exit 1
    }
}

Write-Host "Running: mvn clean compile"
Set-Location $PSScriptRoot
& $mavenBin clean compile

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n`nBuild successful! You can now run the simulation with:"
    Write-Host "  & '$mavenBin' exec:java"
}

