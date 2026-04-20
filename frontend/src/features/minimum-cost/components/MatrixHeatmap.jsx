import { useMemo, useState } from 'react';

function previewMatrix(matrix) {
  if (!Array.isArray(matrix)) {
    return [];
  }
  return matrix.slice(0, 14).map((row) => row.slice(0, 14));
}

export default function MatrixHeatmap({ matrix }) {
  const [showLowestTen, setShowLowestTen] = useState(true);
  const [hoverInfo, setHoverInfo] = useState(null);

  const preview = useMemo(() => previewMatrix(matrix), [matrix]);

  const metrics = useMemo(() => {
    if (!preview.length) {
      return null;
    }
    const values = preview.flat();
    const sorted = [...values].sort((a, b) => a - b);
    const thresholdIndex = Math.max(0, Math.floor(sorted.length * 0.1) - 1);
    const threshold = sorted[thresholdIndex];
    const min = sorted[0];
    const max = sorted[sorted.length - 1];
    return { threshold, min, max };
  }, [preview]);

  if (!preview.length) {
    return (
      <article className="panel">
        <h3>Battle Grid</h3>
        <p className="empty-state">Start a round to view the cost matrix heatmap.</p>
      </article>
    );
  }

  const denominator = Math.max(1, metrics.max - metrics.min);

  return (
    <article className="panel">
      <div className="mc-section-head">
        <h3>Battle Grid</h3>
        <label className="mc-inline-toggle">
          <input
            type="checkbox"
            checked={showLowestTen}
            onChange={(event) => setShowLowestTen(event.target.checked)}
          />
          Highlight lowest 10%
        </label>
      </div>

      <p className="grid-hint">Preview of first {preview.length} x {preview[0].length} cells. Hover a cell for mission intel.</p>

      <div className="matrix-scroll">
        <table className="matrix-table">
          <tbody>
            {preview.map((row, rowIndex) => (
              <tr key={`row-${rowIndex}`}>
                {row.map((value, columnIndex) => {
                  const ratio = (value - metrics.min) / denominator;
                  const isLow = value <= metrics.threshold;
                  const background = `linear-gradient(135deg, rgba(244, 114, 182, ${0.1 + ratio * 0.2}), rgba(56, 189, 248, ${0.2 + ratio * 0.45}))`;

                  return (
                    <td
                      key={`cell-${rowIndex}-${columnIndex}`}
                      className={showLowestTen && isLow ? 'matrix-cell matrix-cell-target' : 'matrix-cell'}
                      style={{ background }}
                      onMouseEnter={() => setHoverInfo({ row: rowIndex, col: columnIndex, value })}
                    >
                      {value}
                    </td>
                  );
                })}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="mc-telemetry">
        {hoverInfo ? (
          <p>
            Employee {hoverInfo.row + 1} to Task {hoverInfo.col + 1} costs <strong>{hoverInfo.value}</strong>.
          </p>
        ) : (
          <p>Hover a cell to inspect assignment cost telemetry.</p>
        )}
      </div>
    </article>
  );
}
