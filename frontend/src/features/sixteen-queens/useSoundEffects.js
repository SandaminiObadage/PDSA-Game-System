import { useMemo } from 'react';

function playTone(frequency, duration, type = 'sine', volume = 0.04) {
  const AudioContextImpl = window.AudioContext || window.webkitAudioContext;
  if (!AudioContextImpl) {
    return;
  }

  const context = new AudioContextImpl();
  const oscillator = context.createOscillator();
  const gain = context.createGain();

  oscillator.type = type;
  oscillator.frequency.setValueAtTime(frequency, context.currentTime);
  gain.gain.setValueAtTime(volume, context.currentTime);
  gain.gain.exponentialRampToValueAtTime(0.0001, context.currentTime + duration);

  oscillator.connect(gain);
  gain.connect(context.destination);

  oscillator.start();
  oscillator.stop(context.currentTime + duration);

  window.setTimeout(() => context.close(), Math.ceil(duration * 1000) + 60);
}

export function useSoundEffects() {
  return useMemo(
    () => ({
      playPlace: () => playTone(620, 0.08, 'triangle', 0.03),
      playInvalid: () => {
        playTone(140, 0.12, 'sawtooth', 0.045);
        window.setTimeout(() => playTone(110, 0.09, 'square', 0.04), 80);
      },
      playSuccess: () => {
        playTone(660, 0.12, 'triangle', 0.03);
        window.setTimeout(() => playTone(880, 0.15, 'triangle', 0.035), 120);
      },
      playVictory: () => {
        playTone(523, 0.1, 'sine', 0.03);
        window.setTimeout(() => playTone(659, 0.12, 'sine', 0.03), 100);
        window.setTimeout(() => playTone(784, 0.2, 'sine', 0.04), 230);
      }
    }),
    []
  );
}
