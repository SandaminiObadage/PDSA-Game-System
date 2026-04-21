function ResultModal({ status, message, onClose }) {
  if (!status) {
    return null;
  }

  const statusClass = status === 'WIN' ? 'kt-status-win' : status === 'LOSE' ? 'kt-status-lose' : 'kt-status-draw';

  return (
    <div className="kt-modal-backdrop" role="dialog" aria-modal="true">
      <div className="kt-modal">
        <h2 className={statusClass}>{status}</h2>
        <p>{message}</p>
                <div className="kt-modal-actions">
          <button type="button" onClick={onClose}>Close</button>
        </div>
      </div>
    </div>
  );
}

export default ResultModal;
