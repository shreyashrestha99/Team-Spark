import type React from 'react'

interface Props {
  title: string
  subtitle?: string
  // Shown top-right, e.g. a total or an average
  aside?: React.ReactNode
  className?: string
  children: React.ReactNode
}

/** Shared frame so every chart on the page sits in the same box. */
export function ChartCard({ title, subtitle, aside, className = '', children }: Props) {
  return (
    <section
      className={`rounded-2xl border border-[#E2E8F0] bg-white p-5 shadow-sm ${className}`}
    >
      <header className="mb-4 flex items-start justify-between gap-4">
        <div>
          <h3 className="text-[14px] font-bold tracking-tight text-[#0F172A]">{title}</h3>
          {subtitle && <p className="mt-0.5 text-[12px] text-[#94A3B8]">{subtitle}</p>}
        </div>
        {aside && <div className="shrink-0 text-right">{aside}</div>}
      </header>
      {children}
    </section>
  )
}

/** Empty state used whenever a chart has nothing to plot yet. */
export function ChartEmpty({ message }: { message: string }) {
  return (
    <div className="flex h-[180px] items-center justify-center rounded-xl bg-[#F8FAFC] px-6 text-center text-[12px] text-[#94A3B8]">
      {message}
    </div>
  )
}
