param(
    [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"

$articleId = $null

function Invoke-CurlJson {
    param(
        [Parameter(Mandatory = $true)]
        [string[]]$Args
    )

    $result = & curl.exe @Args
    if ($LASTEXITCODE -ne 0) {
        throw "curl failed with exit code $LASTEXITCODE."
    }

    return $result
}

function Invoke-PostJson {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Url,
        [Parameter(Mandatory = $true)]
        [string]$Json
    )

    $tmp = [System.IO.Path]::GetTempFileName()
    try {
        [System.IO.File]::WriteAllText($tmp, $Json, [System.Text.Encoding]::UTF8)
        return Invoke-CurlJson -Args @("-sS", "--fail-with-body", "-H", "Content-Type: application/json", "--data-binary", "@$tmp", $Url)
    }
    finally {
        Remove-Item -Path $tmp -ErrorAction SilentlyContinue
    }
}

Write-Host "[1/4] Create article"
$createPayload = @{
    title = "Smoke Article"
    text = "Smoke test article body"
} | ConvertTo-Json -Compress

$created = Invoke-PostJson -Url "$BaseUrl/articles" -Json $createPayload
$createdObj = $created | ConvertFrom-Json
$articleId = $createdObj.id
Write-Host "Created id: $articleId"

Write-Host "[2/4] Add comments"
$comment1 = @{ text = "Great"; score = 90 } | ConvertTo-Json -Compress
$comment2 = @{ text = "Nice"; score = 70 } | ConvertTo-Json -Compress

Invoke-PostJson -Url "$BaseUrl/articles/$articleId/comments" -Json $comment1 | Out-Null
Invoke-PostJson -Url "$BaseUrl/articles/$articleId/comments" -Json $comment2 | Out-Null
Write-Host "Added 2 comments"

Write-Host "[3/4] Read article list"
$articlesJson = Invoke-CurlJson -Args @("-sS", "--fail-with-body", "$BaseUrl/articles")
$articles = $articlesJson | ConvertFrom-Json
$article = $articles | Where-Object { $_.id -eq $articleId } | Select-Object -First 1
if (-not $article) {
    throw "Article $articleId not found in /articles"
}
Write-Host "List includes article: id=$($article.id), avg=$($article.averageRating), comments=$($article.commentsCount)"

Write-Host "[4/4] Read trending"
$trendingJson = Invoke-CurlJson -Args @("-sS", "--fail-with-body", "$BaseUrl/trending")
$trending = $trendingJson | ConvertFrom-Json
Write-Host "Trending article: id=$($trending.id), avg=$($trending.averageRating), comments=$($trending.commentsCount)"

Write-Host "Smoke flow completed successfully."


