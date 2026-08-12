package com.compose.wonderlearn.server

fun renderStatsPage(stats: UsageStats): String {
  val tiles = listOf(
    "Installs (all-time)" to stats.distinctInstalls.toString(),
    "New installs (since 1.1)" to stats.newInstalls.toString(),
    "Active today" to stats.activeToday.toString(),
    "Active last 7 days" to stats.activeLast7Days.toString(),
    "Opens today" to stats.opensToday.toString(),
    "Opens (all-time)" to stats.totalOpens.toString(),
  )

  return buildString {
    append("<!doctype html><html lang=\"en\"><head><meta charset=\"utf-8\">")
    append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">")
    append("<title>Wisekins · Usage</title>")
    append("<style>")
    append(
      """
      :root { color-scheme: light dark; }
      * { box-sizing: border-box; }
      body { margin: 0; font-family: -apple-system, system-ui, Segoe UI, Roboto, sans-serif;
             background: #f6f7fb; color: #1c1e26; padding: 24px; }
      h1 { font-size: 20px; margin: 0 0 4px; }
      .muted { color: #7a7f8c; font-size: 13px; margin: 0 0 20px; }
      h2 { font-size: 14px; text-transform: uppercase; letter-spacing: .04em; color: #7a7f8c;
           margin: 28px 0 12px; }
      .grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(160px, 1fr)); gap: 12px; }
      .tile { background: #fff; border: 1px solid #e7e9f0; border-radius: 14px; padding: 16px; }
      .tile .n { font-size: 30px; font-weight: 700; }
      .tile .l { font-size: 12px; color: #7a7f8c; margin-top: 4px; }
      table { width: 100%; border-collapse: collapse; background: #fff; border: 1px solid #e7e9f0;
              border-radius: 14px; overflow: hidden; }
      td { padding: 9px 14px; border-top: 1px solid #f0f1f6; font-size: 14px; }
      tr:first-child td { border-top: none; }
      td.v { text-align: right; color: #7a7f8c; font-variant-numeric: tabular-nums; }
      .bar { position: relative; }
      .bar .fill { position: absolute; inset: 0; background: #dfe6ff; border-radius: 8px;
                   z-index: 0; }
      .bar .txt { position: relative; z-index: 1; }
      .empty { color: #9aa0ad; font-size: 14px; }
      @media (prefers-color-scheme: dark) {
        body { background: #14151a; color: #e8eaf0; }
        .tile, table { background: #1e2027; border-color: #2c2f38; }
        td { border-color: #26282f; }
        .bar .fill { background: #2f3a63; }
      }
      """.trimIndent(),
    )
    append("</style></head><body>")
    append("<h1>Wisekins · Usage</h1>")
    append("<p class=\"muted\">Opens and installs are counted from app version 1.1 onward. ")
    append("All-time installs are inferred from every anonymous install id ever seen.</p>")

    append("<div class=\"grid\">")
    tiles.forEach { (label, value) ->
      append("<div class=\"tile\"><div class=\"n\">$value</div><div class=\"l\">${esc(label)}</div></div>")
    }
    append("</div>")

    append("<h2>Opens per day</h2>")
    append(barTable(stats.opensPerDay.map { it.day to it.count }))

    append("<h2>New installs per day</h2>")
    append(barTable(stats.newInstallsPerDay.map { it.day to it.count }))

    append("<h2>By platform</h2>")
    append(countTable(stats.byPlatform.map { it.name to it.count }))

    append("<h2>By app version</h2>")
    append(countTable(stats.byVersion.map { it.name to it.count }))

    append("<h2>Game starts</h2>")
    append(countTable(stats.games.map { it.gameId to it.starts }))

    append("</body></html>")
  }
}

private fun barTable(rows: List<Pair<String, Int>>): String {
  if (rows.isEmpty()) return "<p class=\"empty\">No data yet.</p>"
  val max = rows.maxOf { it.second }.coerceAtLeast(1)
  return buildString {
    append("<table>")
    rows.forEach { (label, count) ->
      val pct = (count * 100 / max).coerceIn(0, 100)
      append("<tr><td class=\"bar\"><span class=\"fill\" style=\"width:$pct%\"></span>")
      append("<span class=\"txt\">${esc(label)}</span></td>")
      append("<td class=\"v\">$count</td></tr>")
    }
    append("</table>")
  }
}

private fun countTable(rows: List<Pair<String, Int>>): String {
  if (rows.isEmpty()) return "<p class=\"empty\">No data yet.</p>"
  return buildString {
    append("<table>")
    rows.forEach { (label, count) ->
      append("<tr><td>${esc(label)}</td><td class=\"v\">$count</td></tr>")
    }
    append("</table>")
  }
}

private fun esc(raw: String): String = raw
  .replace("&", "&amp;")
  .replace("<", "&lt;")
  .replace(">", "&gt;")
  .replace("\"", "&quot;")
