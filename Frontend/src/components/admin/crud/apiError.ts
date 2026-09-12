// Turns any API failure into one readable sentence
export function extractError(err: any, fallback = 'Something went wrong. Please try again.'): string {
  const data = err?.response?.data

  // Bean-validation errors arrive as { field: message }
  if (data?.data && typeof data.data === 'object' && !Array.isArray(data.data)) {
    const fieldErrors = Object.values(data.data).filter((v) => typeof v === 'string')
    if (fieldErrors.length > 0) return fieldErrors.join(', ')
  }

  return data?.message || data?.errors?.[0] || err?.message || fallback
}
