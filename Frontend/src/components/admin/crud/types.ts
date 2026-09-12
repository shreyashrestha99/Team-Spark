import type React from 'react'

// One row from any resource endpoint
export type CrudRow = Record<string, any>

export interface FieldOption {
  value: string
  label: string
}

export type FieldType = 'text' | 'number' | 'select' | 'date' | 'time' | 'checkbox'

export interface CrudField {
  name: string
  label: string
  type: FieldType
  required?: boolean
  placeholder?: string
  min?: number
  max?: number
  maxLength?: number

  // Render two fields per row
  half?: boolean

  // Static dropdown choices
  options?: FieldOption[]

  // Dropdown loaded from the API
  optionsEndpoint?: string
  optionsParams?: Record<string, string | number>
  optionValue?: string
  optionLabel?: string | ((row: CrudRow) => string)

  // Checkbox default when creating
  defaultChecked?: boolean

  helpText?: string
}

export interface CrudColumn {
  key: string
  label: string

  // Custom cell rendering
  render?: (row: CrudRow) => React.ReactNode

  // Monospace accent styling
  mono?: boolean
  className?: string
}

export interface ResourceConfig {
  key: string
  title: string
  singular: string
  description: string

  // Base path under /api, e.g. "/admin/programmes"
  endpoint: string

  columns: CrudColumn[]
  fields: CrudField[]

  // Which column keys can be sorted
  sortable?: string[]
  defaultSortBy?: string
  defaultSortDir?: 'asc' | 'desc'

  // Hide the search box when the API has no search
  searchable?: boolean
  pageSize?: number
}
