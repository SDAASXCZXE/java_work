<#
    move_class_files.ps1
    用途：把 `src/` 下混在源代码里的 .class 文件移动到 `target/classes/` 下，保持包目录结构。
    使用方法：在项目根目录（包含 src 和 target 的目录）执行：
      powershell -ExecutionPolicy Bypass -File .\scripts\move_class_files.ps1    # 实际执行移动
      powershell -ExecutionPolicy Bypass -File .\scripts\move_class_files.ps1 -WhatIf  # 仅预演，不移动

    说明：脚本会创建目标目录并覆盖同名文件（如果存在）。请在执行前备份重要文件。
#>
[CmdletBinding()]
param(
    [switch]$WhatIf
)

try {
    $scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
    $projectRoot = Resolve-Path (Join-Path $scriptDir '..')
    $src = Join-Path $projectRoot 'src'
    $target = Join-Path $projectRoot 'target\classes'

    if (-not (Test-Path $src)) {
        Write-Error "未找到 src 目录： $src"; exit 1
    }

    if (-not (Test-Path $target)) {
        if ($WhatIf) { Write-Output "将创建目录： $target (预演)" } else { New-Item -ItemType Directory -Path $target -Force | Out-Null }
    }

    $classFiles = Get-ChildItem -Path $src -Recurse -Filter *.class -ErrorAction SilentlyContinue
    if (-not $classFiles) {
        Write-Output "未在 src 下找到任何 .class 文件，操作结束。"
        exit 0
    }

    foreach ($file in $classFiles) {
        $srcFull = $file.FullName
        # 计算相对于 src 的相对路径
        $relative = $srcFull.Substring((Resolve-Path $src).Path.Length + 1)
        $destFull = Join-Path $target $relative
        $destDir = Split-Path $destFull
        if (-not (Test-Path $destDir)) {
            if ($WhatIf) { Write-Output "将创建目录： $destDir (预演)" } else { New-Item -ItemType Directory -Path $destDir -Force | Out-Null }
        }

        if ($WhatIf) {
            Write-Output "[预演] 移动: $srcFull -> $destFull"
        } else {
            Move-Item -Path $srcFull -Destination $destFull -Force
            Write-Output "已移动: $srcFull -> $destFull"
        }
    }

    Write-Output "操作完成。请检查 target/classes 是否包含期望的 .class 文件，并确认 src 下已不含 .class。"
} catch {
    Write-Error "发生错误: $_"
    exit 2
}

