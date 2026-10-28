import { useRef, useCallback } from 'react';

interface UseLongPressOptions {
  delay?: number;
  shouldPreventDefault?: boolean;
}

export function useLongPress(
  onLongPress: () => void,
  onClick?: () => void,
  { delay = 500, shouldPreventDefault = false }: UseLongPressOptions = {}
) {
  const timerRef = useRef<NodeJS.Timeout | null>(null);
  const isLongPressRef = useRef(false);
  const startCoordsRef = useRef<{ x: number; y: number } | null>(null);

  const start = useCallback(
    (e: React.TouchEvent | React.MouseEvent) => {
      isLongPressRef.current = false;
      if ('touches' in e && e.touches.length > 0) {
        startCoordsRef.current = { x: e.touches[0].clientX, y: e.touches[0].clientY };
      } else if ('clientX' in e) {
        startCoordsRef.current = { x: e.clientX, y: e.clientY };
      }

      timerRef.current = setTimeout(() => {
        isLongPressRef.current = true;
        try {
          if (typeof window !== 'undefined' && 'vibrate' in navigator) {
            navigator.vibrate(40);
          }
        } catch {
          // Ignore
        }
        onLongPress();
      }, delay);
    },
    [onLongPress, delay]
  );

  const clear = useCallback(
    (e: React.TouchEvent | React.MouseEvent, shouldTriggerClick = true) => {
      if (timerRef.current) {
        clearTimeout(timerRef.current);
        timerRef.current = null;
      }
      if (shouldTriggerClick && !isLongPressRef.current && onClick) {
        onClick();
      }
      startCoordsRef.current = null;
    },
    [onClick]
  );

  const move = useCallback((e: React.TouchEvent) => {
    if (startCoordsRef.current && e.touches.length > 0) {
      const dx = Math.abs(e.touches[0].clientX - startCoordsRef.current.x);
      const dy = Math.abs(e.touches[0].clientY - startCoordsRef.current.y);
      if (dx > 12 || dy > 12) {
        if (timerRef.current) {
          clearTimeout(timerRef.current);
          timerRef.current = null;
        }
      }
    }
  }, []);

  return {
    onMouseDown: start,
    onMouseUp: clear,
    onMouseLeave: (e: React.MouseEvent) => clear(e, false),
    onTouchStart: start,
    onTouchEnd: clear,
    onTouchMove: move,
  };
}
