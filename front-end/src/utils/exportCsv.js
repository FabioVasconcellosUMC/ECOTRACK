export const exportCsv = (filename, rows = []) => {
  if (!rows.length) return false

  const headers = Object.keys(rows[0])
  const escapeValue = (value) => {
    const safe = value === null || value === undefined ? '' : String(value)
    return `"${safe.replace(/"/g, '""')}"`
  }

  const csv = [
    headers.join(';'),
    ...rows.map((row) => headers.map((header) => escapeValue(row[header])).join(';')),
  ].join('\n')

  const blob = new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')

  link.href = url
  link.download = filename.endsWith('.csv') ? filename : `${filename}.csv`
  link.style.display = 'none'

  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)

  return true
}
