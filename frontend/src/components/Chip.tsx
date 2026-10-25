interface ChipProps {
  label: string
  selected: boolean
  onToggle: () => void
  disabled?: boolean
}

export default function Chip({ label, selected, onToggle, disabled = false }: ChipProps) {
  return (
    <button
      type="button"
      className={`chip ${selected ? 'active' : ''}`}
      data-selected={selected}
      aria-pressed={selected}
      disabled={disabled}
      onClick={onToggle}
    >
      {selected && (
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
          <polyline points="20 6 9 17 4 12" />
        </svg>
      )}
      <span>{label}</span>
    </button>
  )
}
