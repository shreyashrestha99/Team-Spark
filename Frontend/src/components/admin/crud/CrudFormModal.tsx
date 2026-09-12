import React, { useEffect, useState } from 'react'
import { X, Plus, Save, Loader2, AlertCircle } from 'lucide-react'
import { api } from '../../../services/api'
import { extractError } from './apiError'
import type { CrudField, CrudRow, FieldOption, ResourceConfig } from './types'

const INPUT_CLASS =
  'h-10 w-full rounded-xl border border-[#E2E8F0] bg-white px-3 text-sm text-[#1E293B] outline-none transition placeholder:text-[#94A3B8] focus:border-[#2563EB] focus:ring-2 focus:ring-[#2563EB]/10'

interface Props {
  config: ResourceConfig
  editing: CrudRow | null
  onSubmit: (payload: Record<string, unknown>) => Promise<void>
  onClose: () => void
}

export function CrudFormModal({ config, editing, onSubmit, onClose }: Props) {
  const isEdit = editing !== null

  const [values, setValues] = useState<Record<string, any>>({})
  const [options, setOptions] = useState<Record<string, FieldOption[]>>({})
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  // Seed the form from the edited row or defaults
  useEffect(() => {
    const seeded: Record<string, any> = {}
    for (const field of config.fields) {
      const current = editing ? editing[field.name] : undefined

      if (field.type === 'checkbox') {
        seeded[field.name] = isEdit ? Boolean(current) : (field.defaultChecked ?? true)
      } else if (field.type === 'time' && typeof current === 'string') {
        // "10:00:00" -> "10:00" for the time input
        seeded[field.name] = current.slice(0, 5)
      } else {
        seeded[field.name] = current ?? ''
      }
    }
    setValues(seeded)
    setError(null)
  }, [config.fields, editing, isEdit])

  // Load dropdown choices that come from the API
  useEffect(() => {
    const dynamic = config.fields.filter((f) => f.optionsEndpoint)
    if (dynamic.length === 0) return

    let cancelled = false

    async function loadOptions() {
      const loaded: Record<string, FieldOption[]> = {}

      await Promise.all(
        dynamic.map(async (field) => {
          try {
            const res = await api.get(field.optionsEndpoint!, {
              params: { size: 200, ...(field.optionsParams ?? {}) },
            })
            const data = res.data?.data
            // Endpoints return either a plain array or a page
            const list: CrudRow[] = Array.isArray(data) ? data : (data?.content ?? [])

            loaded[field.name] = list
              .map((row) => ({
                value: String(row[field.optionValue ?? 'id'] ?? ''),
                label: typeof field.optionLabel === 'function'
                  ? field.optionLabel(row)
                  : String(row[field.optionLabel ?? 'label'] ?? ''),
              }))
              .filter((opt) => opt.value !== '')
          } catch {
            loaded[field.name] = []
          }
        })
      )

      if (!cancelled) setOptions(loaded)
    }

    loadOptions()
    return () => { cancelled = true }
  }, [config.fields])

  const setValue = (name: string, value: any) => {
    setValues((prev) => ({ ...prev, [name]: value }))
    setError(null)
  }

  // Build the request body from form values
  const buildPayload = (): Record<string, unknown> => {
    const payload: Record<string, unknown> = {}

    for (const field of config.fields) {
      const value = values[field.name]

      if (field.type === 'checkbox') {
        payload[field.name] = Boolean(value)
        continue
      }

      if (value === '' || value === null || value === undefined) {
        // Skip blank optional fields entirely
        continue
      }

      payload[field.name] = field.type === 'number' ? Number(value) : value
    }

    return payload
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    // Required fields must be filled
    for (const field of config.fields) {
      if (!field.required) continue
      const value = values[field.name]
      if (field.type === 'checkbox') continue
      if (value === '' || value === null || value === undefined) {
        return setError(`${field.label} is required`)
      }
    }

    setSaving(true)
    setError(null)
    try {
      await onSubmit(buildPayload())
      onClose()
    } catch (err) {
      setError(extractError(err, `Failed to save ${config.singular.toLowerCase()}.`))
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center overflow-y-auto bg-black/40 px-4 py-6 backdrop-blur-sm">
      <div className="relative max-h-[90vh] w-full max-w-xl overflow-y-auto rounded-2xl border border-[#E2E8F0] bg-white p-6 shadow-2xl sm:p-8">
        <button
          type="button"
          onClick={onClose}
          className="absolute right-4 top-4 rounded-lg p-1.5 text-[#94A3B8] transition hover:bg-[#F1F5F9] hover:text-[#1E293B] cursor-pointer"
        >
          <X className="h-5 w-5" />
        </button>

        <div className="mb-6 flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#EFF6FF] text-[#2563EB]">
            {isEdit ? <Save className="h-5 w-5" /> : <Plus className="h-5 w-5" />}
          </div>
          <div>
            <h2 className="text-xl font-bold text-[#0F172A]">
              {isEdit ? `Edit ${config.singular}` : `Add New ${config.singular}`}
            </h2>
            <p className="text-sm text-[#64748B]">{config.description}</p>
          </div>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4" noValidate>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            {config.fields.map((field) => (
              <div key={field.name} className={field.half ? '' : 'sm:col-span-2'}>
                <FieldInput
                  field={field}
                  value={values[field.name]}
                  options={options[field.name]}
                  onChange={(value) => setValue(field.name, value)}
                />
              </div>
            ))}
          </div>

          {error && (
            <div className="flex items-center gap-2 rounded-xl bg-[#FEF2F2] px-3 py-2.5 text-sm text-[#DC2626]">
              <AlertCircle className="h-4 w-4 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <div className="flex items-center justify-end gap-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="rounded-xl border border-[#E2E8F0] px-5 py-2.5 text-sm font-medium text-[#475569] transition hover:bg-[#F8FAFC] cursor-pointer"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={saving}
              className="inline-flex items-center gap-2 rounded-xl bg-[#2563EB] px-5 py-2.5 text-sm font-semibold text-white shadow-md shadow-[#2563EB]/20 transition hover:bg-[#1D4ED8] disabled:opacity-60 cursor-pointer"
            >
              {saving ? (
                <><Loader2 className="h-4 w-4 animate-spin" /><span>Saving...</span></>
              ) : isEdit ? (
                <><Save className="h-4 w-4" /><span>Save Changes</span></>
              ) : (
                <><Plus className="h-4 w-4" /><span>Create {config.singular}</span></>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

// Renders one field based on its type
function FieldInput({
  field, value, options, onChange,
}: {
  field: CrudField
  value: any
  options?: FieldOption[]
  onChange: (value: any) => void
}) {
  if (field.type === 'checkbox') {
    return (
      <label className="flex cursor-pointer items-center gap-2.5 rounded-xl border border-[#E2E8F0] bg-[#F8FAFC] px-3 py-2.5">
        <input
          type="checkbox"
          checked={Boolean(value)}
          onChange={(e) => onChange(e.target.checked)}
          className="h-4 w-4 cursor-pointer accent-[#2563EB]"
        />
        <span className="text-sm font-medium text-[#1E293B]">{field.label}</span>
      </label>
    )
  }

  const choices = field.options ?? options ?? []

  return (
    <div>
      <label className="mb-1.5 block text-sm font-semibold text-[#1E293B]">
        {field.label}
        {field.required
          ? <span className="text-[#EF4444]"> *</span>
          : <span className="font-normal text-[#94A3B8]"> (optional)</span>}
      </label>

      {field.type === 'select' ? (
        <select
          value={value ?? ''}
          onChange={(e) => onChange(e.target.value)}
          disabled={choices.length === 0}
          className={`${INPUT_CLASS} disabled:bg-[#F1F5F9] disabled:text-[#94A3B8]`}
        >
          <option value="">
            {choices.length === 0 ? 'No options available' : `Select ${field.label.toLowerCase()}…`}
          </option>
          {choices.map((opt) => (
            <option key={opt.value} value={opt.value}>{opt.label}</option>
          ))}
        </select>
      ) : (
        <input
          type={field.type}
          value={value ?? ''}
          onChange={(e) => onChange(e.target.value)}
          placeholder={field.placeholder}
          min={field.min}
          max={field.max}
          maxLength={field.maxLength}
          className={INPUT_CLASS}
        />
      )}

      {field.helpText && <p className="mt-1 text-[11px] text-[#94A3B8]">{field.helpText}</p>}
    </div>
  )
}
