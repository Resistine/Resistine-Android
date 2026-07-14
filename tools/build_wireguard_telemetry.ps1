param(
    [string]$NdkVersion = "27.1.12297006"
)

$ErrorActionPreference = "Stop"

$goVersion = "1.24.3"
$goArchiveName = "go$goVersion.windows-amd64.zip"
$goArchiveSha256 = "be9787cb08998b1860fe3513e48a5fe5b96302d358a321b58e651184fa9638b3"

function Set-RegexReplacement {
    param(
        [string]$Path,
        [string]$Pattern,
        [string]$Replacement,
        [int]$ExpectedCount
    )

    $content = [System.IO.File]::ReadAllText($Path)
    $matches = [System.Text.RegularExpressions.Regex]::Matches($content, $Pattern)
    if ($matches.Count -ne $ExpectedCount) {
        throw "Unexpected WireGuard Go runtime source in ${Path}: expected $ExpectedCount matches, found $($matches.Count)"
    }
    $updated = [System.Text.RegularExpressions.Regex]::Replace($content, $Pattern, $Replacement)
    [System.IO.File]::WriteAllText($Path, $updated, [System.Text.UTF8Encoding]::new($false))
}

function Install-PinnedGoToolchain {
    param(
        [string]$ProjectRoot
    )

    $cacheRoot = Join-Path $env:USERPROFILE ".gradle\caches\golang"
    $archive = Join-Path $cacheRoot $goArchiveName
    $buildRoot = Join-Path $ProjectRoot "app\build\wireguard-go"
    $toolchainRoot = Join-Path $buildRoot "go-$goVersion"
    $preparedMarker = Join-Path $toolchainRoot ".resistine-boottime-v2"
    if (Test-Path $preparedMarker) {
        return Join-Path $toolchainRoot "bin\go.exe"
    }

    New-Item -ItemType Directory -Force -Path $cacheRoot | Out-Null
    $validArchive = (Test-Path $archive) -and
        ((Get-FileHash -Algorithm SHA256 $archive).Hash.ToLowerInvariant() -eq $goArchiveSha256)
    if (-not $validArchive) {
        $temporaryArchive = "$archive.tmp"
        Invoke-WebRequest -UseBasicParsing `
            -Uri "https://go.dev/dl/$goArchiveName" `
            -OutFile $temporaryArchive
        $downloadedHash = (Get-FileHash -Algorithm SHA256 $temporaryArchive).Hash.ToLowerInvariant()
        if ($downloadedHash -ne $goArchiveSha256) {
            Remove-Item $temporaryArchive -Force
            throw "Go toolchain checksum mismatch: expected $goArchiveSha256, found $downloadedHash"
        }
        Move-Item -Force $temporaryArchive $archive
    }

    New-Item -ItemType Directory -Force -Path $buildRoot | Out-Null
    $extractionRoot = Join-Path $buildRoot "extract-$goVersion"
    Remove-Item $extractionRoot -Recurse -Force -ErrorAction SilentlyContinue
    Remove-Item $toolchainRoot -Recurse -Force -ErrorAction SilentlyContinue
    Expand-Archive -Path $archive -DestinationPath $extractionRoot
    Move-Item (Join-Path $extractionRoot "go") $toolchainRoot
    Remove-Item $extractionRoot -Recurse -Force

    Set-RegexReplacement `
        -Path (Join-Path $toolchainRoot "src\runtime\sys_linux_386.s") `
        -Pattern 'MOVL\s+\$1,\s+0\(SP\)\s+// CLOCK_MONOTONIC' `
        -Replacement "MOVL`t`$7, 0(SP)`t// CLOCK_BOOTTIME" `
        -ExpectedCount 1
    Set-RegexReplacement `
        -Path (Join-Path $toolchainRoot "src\runtime\sys_linux_386.s") `
        -Pattern 'MOVL\s+\$1,\s+BX\s+// CLOCK_MONOTONIC' `
        -Replacement "MOVL`t`$7, BX`t`t// CLOCK_BOOTTIME" `
        -ExpectedCount 1
    Set-RegexReplacement `
        -Path (Join-Path $toolchainRoot "src\runtime\sys_linux_amd64.s") `
        -Pattern 'MOVL\s+\$1,\s+DI\s+// CLOCK_MONOTONIC' `
        -Replacement "MOVL`t`$7, DI // CLOCK_BOOTTIME" `
        -ExpectedCount 1
    foreach ($architecture in @("arm", "arm64")) {
        Set-RegexReplacement `
            -Path (Join-Path $toolchainRoot "src\runtime\sys_linux_$architecture.s") `
            -Pattern '#define CLOCK_MONOTONIC\s+1' `
            -Replacement "#define CLOCK_BOOTTIME`t7" `
            -ExpectedCount 1
        Set-RegexReplacement `
            -Path (Join-Path $toolchainRoot "src\runtime\sys_linux_$architecture.s") `
            -Pattern '\$CLOCK_MONOTONIC' `
            -Replacement "`$CLOCK_BOOTTIME" `
            -ExpectedCount 1
    }
    New-Item -ItemType File -Force -Path $preparedMarker | Out-Null
    return Join-Path $toolchainRoot "bin\go.exe"
}

$root = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$source = Join-Path $root "app\src\main\wireguard-go"
$outputRoot = Join-Path $root "app\src\main\jniLibs"
$ndkRoot = Join-Path $env:LOCALAPPDATA "Android\Sdk\ndk\$NdkVersion"
$toolchain = Join-Path $ndkRoot "toolchains\llvm\prebuilt\windows-x86_64\bin"
$go = Install-PinnedGoToolchain -ProjectRoot $root
$reportedGoVersion = (& $go version)
if ($reportedGoVersion -notmatch "\bgo$([regex]::Escape($goVersion))\b") {
    throw "Unexpected Go compiler version: $reportedGoVersion"
}
Write-Host "Using Go compiler: $go"
Write-Host "Using Go version: $reportedGoVersion"

if (-not (Test-Path $source)) {
    throw "WireGuard telemetry source directory not found: $source"
}
if (-not (Test-Path $toolchain)) {
    throw "Android NDK toolchain not found: $toolchain"
}

$targets = @(
    @{ Abi = "armeabi-v7a"; GoArch = "arm"; GoArm = "7"; Compiler = "armv7a-linux-androideabi24-clang.cmd" },
    @{ Abi = "arm64-v8a"; GoArch = "arm64"; GoArm = ""; Compiler = "aarch64-linux-android24-clang.cmd" },
    @{ Abi = "x86"; GoArch = "386"; GoArm = ""; Compiler = "i686-linux-android24-clang.cmd" },
    @{ Abi = "x86_64"; GoArch = "amd64"; GoArm = ""; Compiler = "x86_64-linux-android24-clang.cmd" }
)

$previous = @{
    GOOS = $env:GOOS
    GOARCH = $env:GOARCH
    GOARM = $env:GOARM
    CGO_ENABLED = $env:CGO_ENABLED
    CC = $env:CC
    CGO_CFLAGS = $env:CGO_CFLAGS
    CGO_LDFLAGS = $env:CGO_LDFLAGS
    GOFLAGS = $env:GOFLAGS
    GOTOOLCHAIN = $env:GOTOOLCHAIN
}

try {
    $env:GOOS = "android"
    $env:CGO_ENABLED = "1"
    $env:CGO_CFLAGS = ""
    $env:CGO_LDFLAGS = "-Wl,-soname=libwg-go-telemetry.so"
    $env:GOFLAGS = "-mod=readonly"
    $env:GOTOOLCHAIN = "local"

    Push-Location $source
    & $go mod verify
    if ($LASTEXITCODE -ne 0) {
        throw "WireGuard Go module checksum verification failed"
    }
    foreach ($target in $targets) {
        $compiler = Join-Path $toolchain $target.Compiler
        if (-not (Test-Path $compiler)) {
            throw "Android compiler not found: $compiler"
        }

        $env:GOARCH = $target.GoArch
        $env:GOARM = $target.GoArm
        $env:CC = $compiler

        $output = Join-Path $outputRoot "$($target.Abi)\libwg-go-telemetry.so"
        New-Item -ItemType Directory -Force -Path (Split-Path $output) | Out-Null

        Write-Host "Building WireGuard telemetry for $($target.Abi)"
        Write-Host "Using C compiler: $compiler"
        $stdoutLog = Join-Path $env:TEMP "resistine-wireguard-go-$($target.Abi)-stdout.log"
        $stderrLog = Join-Path $env:TEMP "resistine-wireguard-go-$($target.Abi)-stderr.log"
        $arguments = "build -tags linux " +
            "-ldflags `"-X golang.zx2c4.com/wireguard/ipc.socketDirectory=/data/data/com.resistine.android/cache/wireguard -buildid=`" " +
            "-trimpath -buildvcs=false -buildmode c-shared -o `"$output`" ."
        $process = Start-Process `
            -FilePath $go `
            -ArgumentList $arguments `
            -Wait `
            -NoNewWindow `
            -PassThru `
            -RedirectStandardOutput $stdoutLog `
            -RedirectStandardError $stderrLog
        Get-Content $stdoutLog -ErrorAction SilentlyContinue | ForEach-Object { Write-Host $_ }
        Get-Content $stderrLog -ErrorAction SilentlyContinue | ForEach-Object { Write-Host $_ }
        if ($process.ExitCode -ne 0) {
            throw "Go build failed for $($target.Abi)"
        }

        $header = [System.IO.Path]::ChangeExtension($output, ".h")
        if (Test-Path $header) {
            Remove-Item $header
        }
    }
} finally {
    Pop-Location
    foreach ($name in $previous.Keys) {
        if ($null -eq $previous[$name]) {
            Remove-Item -Path "Env:$name" -ErrorAction SilentlyContinue
        } else {
            Set-Item -Path "Env:$name" -Value $previous[$name]
        }
    }
}
