import { useState, useEffect } from 'react';
import { MIN_PC_WIDTH } from '@/utils/constants';

export function useWindowWidth() {
  const [width, setWidth] = useState(window.innerWidth);

  useEffect(() => {
    const handleResize = () => setWidth(window.innerWidth);
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  return { width, isPC: width >= MIN_PC_WIDTH };
}
