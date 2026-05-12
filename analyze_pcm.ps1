$file = "c:/Users/Yabo.sui/StudioProjects/kweaver-app/asr_audio_latest/recording_1778554293939.pcm"
$bytes = [System.IO.File]::ReadAllBytes($file)

$max = 0
$sum = 0
$count = 0
$positiveCount = 0
$silenceCount = 0

for ($i = 0; $i -lt $bytes.Length - 1; $i += 2) {
    $low = [int]$bytes[$i]
    $high = [int]$bytes[$i + 1]
    $value = $high * 256 + $low
    if ($value -gt 32767) { $value -= 65536 }
    $absValue = [Math]::Abs($value)
    $sum += $absValue
    $count++
    if ($absValue -gt $max) { $max = $absValue }
    if ($absValue -gt 100) { $positiveCount++ }
    if ($absValue -lt 50) { $silenceCount++ }
}

Write-Host "=== PCM 文件分析 ==="
Write-Host "文件: $file"
Write-Host "文件大小: $($bytes.Length) bytes"
Write-Host "采样点数: $count"
Write-Host ""
Write-Host "=== 音量分析 ==="
Write-Host "最大振幅: $max (占 32767 的 $([Math]::Round($max/32767*100, 2))%)"
Write-Host "平均振幅: $([Math]::Round($sum/$count, 2))"
Write-Host "非静音采样 (>100): $positiveCount ($([Math]::Round($positiveCount/$count*100, 2))%)"
Write-Host "静音采样 (<50): $silenceCount ($([Math]::Round($silenceCount/$count*100, 2))%)"
Write-Host ""
Write-Host "=== 评估 ==="
if ($max -lt 5000) {
    Write-Host "⚠️ 音量很低 - 最大振幅只有 $max"
} elseif ($max -lt 10000) {
    Write-Host "🔆 音量偏低 - 最大振幅 $max"
} elseif ($max -lt 20000) {
    Write-Host "✅ 音量正常 - 最大振幅 $max"
} else {
    Write-Host "🔊 音量较高 - 最大振幅 $max"
}