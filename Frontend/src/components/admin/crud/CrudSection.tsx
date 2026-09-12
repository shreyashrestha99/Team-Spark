import { useState } from 'react'
import { Plus } from 'lucide-react'
import { CrudTable } from './CrudTable'
import { CrudFormModal } from './CrudFormModal'
import { CrudViewModal } from './CrudViewModal'
import { ConfirmDialog } from './ConfirmDialog'
import { useCrud } from './useCrud'
import { extractError } from './apiError'
import type { CrudRow, ResourceConfig } from './types'

interface Props {
  config: ResourceConfig
  // Primary key field, used for update and delete
  idField: string
}

export function CrudSection({ config, idField }: Props) {
  const crud = useCrud(config)

  const [showForm, setShowForm] = useState(false)
  const [editing, setEditing] = useState<CrudRow | null>(null)
  const [viewing, setViewing] = useState<CrudRow | null>(null)

  const [deleting, setDeleting] = useState<CrudRow | null>(null)
  const [deleteBusy, setDeleteBusy] = useState(false)
  const [deleteError, setDeleteError] = useState<string | null>(null)

  const openCreate = () => {
    setEditing(null)
    setShowForm(true)
  }

  const openEdit = (row: CrudRow) => {
    setEditing(row)
    setShowForm(true)
  }

  // Create or update depending on the mode
  const handleSubmit = async (payload: Record<string, unknown>) => {
    if (editing) {
      await crud.update(String(editing[idField]), payload)
    } else {
      await crud.create(payload)
    }
  }

  const confirmDelete = async () => {
    if (!deleting) return
    setDeleteBusy(true)
    setDeleteError(null)
    try {
      await crud.remove(String(deleting[idField]))
      setDeleting(null)
    } catch (err) {
      // Backend guards explain why a delete was refused
      setDeleteError(extractError(err, 'Failed to delete.'))
    } finally {
      setDeleteBusy(false)
    }
  }

  return (
    <>
      <div className="mb-4 flex items-center justify-between">
        <div>
          <h2 className="text-lg font-bold text-[#0F172A]">{config.title}</h2>
          <p className="text-xs text-[#94A3B8]">{config.description}</p>
        </div>
        <button
          type="button"
          onClick={openCreate}
          className="inline-flex items-center gap-2 rounded-xl bg-[#2563EB] px-5 py-2.5 text-sm font-semibold text-white shadow-md shadow-[#2563EB]/20 transition hover:bg-[#1D4ED8] hover:shadow-lg cursor-pointer"
        >
          <Plus className="h-4 w-4" />
          <span>Add {config.singular}</span>
        </button>
      </div>

      <CrudTable
        config={config}
        rows={crud.rows}
        loading={crud.loading}
        listError={crud.listError}
        page={crud.page}
        pageSize={crud.pageSize}
        totalPages={crud.totalPages}
        totalElements={crud.totalElements}
        sortBy={crud.sortBy}
        sortDir={crud.sortDir}
        search={crud.search}
        searchInput={crud.searchInput}
        onSearchInputChange={crud.setSearchInput}
        onSearchSubmit={(e) => { e.preventDefault(); crud.setSearch(crud.searchInput) }}
        onClearSearch={() => { crud.setSearchInput(''); crud.setSearch('') }}
        onRefresh={crud.fetchRows}
        onToggleSort={crud.toggleSort}
        onPageChange={crud.setPage}
        onView={setViewing}
        onEdit={openEdit}
        onDelete={(row) => { setDeleting(row); setDeleteError(null) }}
      />

      {showForm && (
        <CrudFormModal
          config={config}
          editing={editing}
          onSubmit={handleSubmit}
          onClose={() => { setShowForm(false); setEditing(null) }}
        />
      )}

      {viewing && (
        <CrudViewModal config={config} row={viewing} onClose={() => setViewing(null)} />
      )}

      {deleting && (
        <ConfirmDialog
          title={`Delete ${config.singular}?`}
          message={`This permanently removes the ${config.singular.toLowerCase()}. This cannot be undone.`}
          error={deleteError}
          busy={deleteBusy}
          onConfirm={confirmDelete}
          onCancel={() => { setDeleting(null); setDeleteError(null) }}
        />
      )}
    </>
  )
}
