import { useId } from 'react'
import type { InputHTMLAttributes } from 'react'

interface TextInputProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string
  error?: string
}

export default function TextInput({ label, error, id, className = '', ...rest }: TextInputProps) {
  const generatedId = useId()
  const inputId = id ?? generatedId
  const errorId = `${inputId}-error`

  return (
    <div className={`text-input ${error ? 'text-input-error' : ''} ${className}`.trim()}>
      <label htmlFor={inputId}>{label}</label>
      <input
        id={inputId}
        aria-invalid={!!error}
        aria-describedby={error ? errorId : undefined}
        {...rest}
      />
      {error && (
        <span id={errorId} className="text-input-error-msg">
          {error}
        </span>
      )}
    </div>
  )
}
