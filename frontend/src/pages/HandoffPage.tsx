import { Navigate, useNavigate, useParams } from 'react-router-dom'
import Button from '../components/Button'
import { useSessionFlow } from '../context/SessionFlowContext'

export default function HandoffPage() {
  const navigate = useNavigate()
  const { id, index } = useParams()
  const { setup } = useSessionFlow()

  if (!setup || setup.sessionId !== Number(id)) return <Navigate to="/" replace />

  const personIndex = Number(index)
  if (!Number.isInteger(personIndex) || personIndex < 0 || personIndex >= setup.groupSize) {
    return <Navigate to="/" replace />
  }

  const goToForm = () => navigate(`/session/${id}/person/${personIndex}`)

  return (
    <div className="handoff-screen">
      <p className="handoff-message">
        Pass the laptop to person {personIndex + 1} of {setup.groupSize}
      </p>

      <div className="handoff-dots">
        {Array.from({ length: setup.groupSize }, (_, i) => (
          <span key={i} className={`handoff-dot ${i < personIndex ? 'done' : i === personIndex ? 'current' : ''}`} />
        ))}
      </div>

      <Button autoFocus onClick={goToForm}>
        I'm ready
      </Button>

      {personIndex > 0 && (
        <button type="button" className="handoff-back-link" onClick={() => navigate(`/session/${id}/person/${personIndex - 1}`)}>
          Go back
        </button>
      )}
    </div>
  )
}
