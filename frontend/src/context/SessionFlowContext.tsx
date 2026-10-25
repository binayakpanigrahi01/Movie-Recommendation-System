import { createContext, useContext, useState, type ReactNode } from 'react'
import type { AgeBand, Intensity, SessionMediaType, TimeLimit } from '../api/sessions'
import type { TitleSearchResult } from '../api/titles'

export interface PersonAnswerDraft {
  personIndex: number
  name: string
  ageBand: AgeBand | null
  moods: string[]
  genresUpFor: number[]
  hardNoGenres: number[]
  languagesOk: string[]
  intensity: Intensity
  somethingLikeTitle: TitleSearchResult | null
}

export interface SessionSetupInfo {
  sessionId: number
  mediaType: SessionMediaType
  groupSize: number
  timeLimit: TimeLimit
}

interface SessionFlowContextValue {
  setup: SessionSetupInfo | null
  answers: Record<number, PersonAnswerDraft>
  begin: (info: SessionSetupInfo) => void
  saveAnswer: (answer: PersonAnswerDraft) => void
  clear: () => void
}

const SessionFlowContext = createContext<SessionFlowContextValue | null>(null)

export function SessionFlowProvider({ children }: { children: ReactNode }) {
  const [setup, setSetup] = useState<SessionSetupInfo | null>(null)
  const [answers, setAnswers] = useState<Record<number, PersonAnswerDraft>>({})

  const begin = (info: SessionSetupInfo) => {
    setSetup(info)
    setAnswers({})
  }

  const saveAnswer = (answer: PersonAnswerDraft) => {
    setAnswers((prev) => ({ ...prev, [answer.personIndex]: answer }))
  }

  const clear = () => {
    setSetup(null)
    setAnswers({})
  }

  return (
    <SessionFlowContext.Provider value={{ setup, answers, begin, saveAnswer, clear }}>
      {children}
    </SessionFlowContext.Provider>
  )
}

export function useSessionFlow() {
  const ctx = useContext(SessionFlowContext)
  if (!ctx) throw new Error('useSessionFlow must be used within SessionFlowProvider')
  return ctx
}
