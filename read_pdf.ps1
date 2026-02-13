$bytes = [System.IO.File]::ReadAllBytes("Ejercicio Técnico Backend Java v2 5.pdf")
$text = [System.Text.Encoding]::ASCII.GetString($bytes)
$pattern = "(?<=\()([^\)]+)(?=\))"
$allMatches = [regex]::Matches($text, $pattern)
$results = @()
foreach($match in $allMatches) {
    $val = $match.Value
    if($val.Length -gt 2 -and $val -notmatch "^[\\/<>\[\]{}%#@!&]" -and $val -notmatch "^\d+\.\d+ \d+" -and $val -notmatch "^[A-Z]{2,}$") {
        $results += $val
    }
}
$results | Select-Object -Unique | ForEach-Object { Write-Output $_ }
