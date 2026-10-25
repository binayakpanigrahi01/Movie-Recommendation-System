export interface SliderStep<T extends string> {
  value: T
  label: string
}

interface Slider3StepProps<T extends string> {
  steps: readonly [SliderStep<T>, SliderStep<T>, SliderStep<T>]
  value: T
  onChange: (value: T) => void
  label?: string
}

export default function Slider3Step<T extends string>({ steps, value, onChange, label }: Slider3StepProps<T>) {
  const index = steps.findIndex((s) => s.value === value)

  return (
    <div className="slider-3step">
      {label && <span className="slider-3step-label">{label}</span>}
      <input
        type="range"
        min={0}
        max={2}
        step={1}
        value={index}
        onChange={(e) => onChange(steps[Number(e.target.value)].value)}
        aria-valuetext={steps[index]?.label}
      />
      <div className="slider-3step-ticks">
        {steps.map((step) => (
          <span key={step.value} className={step.value === value ? 'active' : ''}>
            {step.label}
          </span>
        ))}
      </div>
    </div>
  )
}
