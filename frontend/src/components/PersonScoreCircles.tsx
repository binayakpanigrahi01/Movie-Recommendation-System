interface PersonScore {
  personIndex: number
  name: string
  score: number
}

interface PersonScoreCirclesProps {
  scores: PersonScore[]
}

export default function PersonScoreCircles({ scores }: PersonScoreCirclesProps) {
  return (
    <div className="scores-row">
      {scores.map((s) => (
        <div
          key={s.personIndex}
          className={`score-circle ${s.score >= 70 ? 'green' : 'amber'}`}
          title={`${s.name}: ${s.score}%`}
        >
          {s.name.charAt(0)}
        </div>
      ))}
    </div>
  )
}
