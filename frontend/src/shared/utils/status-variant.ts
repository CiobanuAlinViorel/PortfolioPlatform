export type BadgeVariant = 'neutral' | 'success' | 'info' | 'warning' | 'danger' | 'accent';

export function statusVariant(status: string | null | undefined): BadgeVariant {
  if (!status) return 'neutral';
  
  const s = status.toUpperCase();
  
  // Success states
  if (['PRODUCTION', 'COMPLETED', 'EXPERT', 'ONGOING', 'VERIFIED'].includes(s)) {
    return 'success';
  }
  
  // Info states
  if (['DEVELOPMENT', 'TESTING', 'IN_PROGRESS', 'INTERMEDIATE', 'ADVANCED', 'REGULAR', 'FREQUENT'].includes(s)) {
    return 'info';
  }
  
  // Warning states
  if (['PLANNING', 'BEGINNER', 'PAUSED', 'MAINTENANCE', 'OCCASIONAL'].includes(s)) {
    return 'warning';
  }
  
  // Danger states
  if (['ARCHIVED', 'DROPPED', 'NOT_STARTED', 'DAILY'].includes(s)) {
    return 'danger';
  }
  
  // Difficulty mapping
  if (s === 'LOW') return 'neutral';
  if (s === 'MEDIUM') return 'warning';
  if (s === 'HIGH') return 'danger';
  
  return 'neutral';
}
