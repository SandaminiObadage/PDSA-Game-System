/**
 * Component to display algorithm comparison results
 */
function AlgorithmComparison({ fordFulkerson, dinic, maxFlow }) {
  if (!fordFulkerson || !dinic) {
    return <div className="algorithm-comparison">Waiting for algorithm results...</div>;
  }

  return (
    <div className="algorithm-comparison">
      <h3>Algorithm Comparison</h3>
      <div className="algorithm-results">
        <div className="algorithm-result ford-fulkerson">
          <h4>Ford-Fulkerson</h4>
          <p>
            <strong>Max Flow:</strong> {fordFulkerson.maxFlow} vehicles/min
          </p>
          <p>
            <strong>Execution Time:</strong> {fordFulkerson.executionTimeMs.toFixed(2)} ms
          </p>
        </div>

        <div className="algorithm-result dinic">
          <h4>Dinic's Algorithm</h4>
          <p>
            <strong>Max Flow:</strong> {dinic.maxFlow} vehicles/min
          </p>
          <p>
            <strong>Execution Time:</strong> {dinic.executionTimeMs.toFixed(2)} ms
          </p>
        </div>
      </div>

      <div className="correctAnswer">
        <p>
          <strong>Correct Answer (Verified):</strong> {maxFlow} vehicles/min
        </p>
      </div>
    </div>
  );
}

export default AlgorithmComparison;
