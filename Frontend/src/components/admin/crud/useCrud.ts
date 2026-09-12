import { useCallback, useEffect, useState } from 'react'
import { api } from '../../../services/api'
import { extractError } from './apiError'
import type { CrudRow, ResourceConfig } from './types'

/** Owns list state and the four write operations for one resource. */
export function useCrud(config: ResourceConfig) {
  const pageSize = config.pageSize ?? 10

  const [rows, setRows] = useState<CrudRow[]>([])
  const [loading, setLoading] = useState(false)
  const [listError, setListError] = useState<string | null>(null)

  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)

  const [search, setSearch] = useState('')
  const [searchInput, setSearchInput] = useState('')
  const [sortBy, setSortBy] = useState(config.defaultSortBy ?? 'createdAt')
  const [sortDir, setSortDir] = useState<'asc' | 'desc'>(config.defaultSortDir ?? 'desc')

  const fetchRows = useCallback(async () => {
    setLoading(true)
    setListError(null)
    try {
      const params: Record<string, string | number> = {
        page,
        size: pageSize,
        sortBy,
        sortDirection: sortDir,
      }
      if (config.searchable !== false && search.trim()) params.search = search.trim()

      const res = await api.get(config.endpoint, { params })
      const data = res.data?.data

      setRows(data?.content ?? [])
      setTotalPages(data?.totalPages ?? 0)
      setTotalElements(data?.totalElements ?? 0)
    } catch (err) {
      setRows([])
      setTotalPages(0)
      setTotalElements(0)
      setListError(extractError(err, `Failed to load ${config.title.toLowerCase()}.`))
    } finally {
      setLoading(false)
    }
  }, [config.endpoint, config.searchable, config.title, page, pageSize, search, sortBy, sortDir])

  useEffect(() => {
    fetchRows()
  }, [fetchRows])

  // Back to page one when the query changes
  useEffect(() => {
    setPage(0)
  }, [search])

  const create = async (payload: Record<string, unknown>) => {
    await api.post(config.endpoint, payload)
    await fetchRows()
  }

  const update = async (id: string, payload: Record<string, unknown>) => {
    await api.put(`${config.endpoint}/${id}`, payload)
    await fetchRows()
  }

  const remove = async (id: string) => {
    await api.delete(`${config.endpoint}/${id}`)
    await fetchRows()
  }

  // Flip direction when the same column is clicked
  const toggleSort = (field: string) => {
    if (sortBy === field) {
      setSortDir((prev) => (prev === 'asc' ? 'desc' : 'asc'))
    } else {
      setSortBy(field)
      setSortDir('asc')
    }
  }

  return {
    rows,
    loading,
    listError,
    page,
    pageSize,
    totalPages,
    totalElements,
    search,
    searchInput,
    sortBy,
    sortDir,
    setPage,
    setSearch,
    setSearchInput,
    toggleSort,
    fetchRows,
    create,
    update,
    remove,
  }
}
