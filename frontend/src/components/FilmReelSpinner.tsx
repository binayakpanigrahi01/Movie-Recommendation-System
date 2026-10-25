interface FilmReelSpinnerProps {
  label?: string
  subLabel?: string
}

export default function FilmReelSpinner({ label, subLabel }: FilmReelSpinnerProps) {
  return (
    <div className="film-reel-spinner" role="status" aria-live="polite">
      <svg
        className="spinner-reel"
        width="42"
        height="42"
        viewBox="0 0 24 24"
        fill="none"
        stroke="var(--color-accent)"
        strokeWidth="1.5"
        aria-hidden="true"
      >
        <circle cx="12" cy="12" r="10" />
        <circle cx="12" cy="12" r="3" />
        <circle cx="12" cy="6" r="1.5" fill="var(--color-accent)" />
        <circle cx="12" cy="18" r="1.5" fill="var(--color-accent)" />
        <circle cx="6" cy="12" r="1.5" fill="var(--color-accent)" />
        <circle cx="18" cy="12" r="1.5" fill="var(--color-accent)" />
      </svg>
      {(label || subLabel) && (
        <div>
          {label && <div className="film-reel-spinner-label">{label}</div>}
          {subLabel && <div className="film-reel-spinner-sublabel">{subLabel}</div>}
        </div>
      )}
    </div>
  )
}
