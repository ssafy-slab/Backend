$jdkHome = "C:\Program Files\Java\jdk-21"

if (-not (Test-Path "$jdkHome\bin\java.exe")) {
    Write-Error "JDK 21 was not found at $jdkHome"
    exit 1
}

$env:JAVA_HOME = $jdkHome
$env:Path = "$jdkHome\bin;$env:Path"

Set-Location $PSScriptRoot
.\mvnw.cmd spring-boot:run
