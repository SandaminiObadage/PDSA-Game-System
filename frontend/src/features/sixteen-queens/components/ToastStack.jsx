import { AnimatePresence, motion } from 'framer-motion';

export function ToastStack({ toasts, onDismiss }) {
  return (
    <div className="toast-stack" aria-live="polite" aria-atomic="true">
      <AnimatePresence>
        {toasts.map((toast) => (
          <motion.article
            key={toast.id}
            initial={{ opacity: 0, y: 14, scale: 0.96 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: -8, scale: 0.95 }}
            transition={{ duration: 0.2 }}
            className={`toast ${toast.tone}`}
          >
            <header>{toast.title}</header>
            <p>{toast.message}</p>
            <button type="button" onClick={() => onDismiss(toast.id)} aria-label="Dismiss notification">
              x
            </button>
          </motion.article>
        ))}
      </AnimatePresence>
    </div>
  );
}
