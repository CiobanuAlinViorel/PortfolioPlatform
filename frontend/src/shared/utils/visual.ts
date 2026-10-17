import {
    LucideBookOpen,
    LucideChefHat,
    LucideCpu,
    LucideDumbbell,
    LucideGamepad2,
    LucideGraduationCap,
    LucideHeart,
    LucideIcon,
    LucideMusic,
    LucidePalette,
    LucidePlane,
    LucideSprout,
    LucideUsers,
} from '@lucide/angular';

/** Deterministic hue (0-359) from any string — used for provider/initials tiles. */
export function hueFromString(value: string): number {
    let hash = 0;
    for (let i = 0; i < value.length; i++) {
        hash = (hash * 31 + value.charCodeAt(i)) | 0;
    }
    return Math.abs(hash) % 360;
}

/** Inline style for a soft coloured tile derived from a string. */
export function tileStyle(value: string): Record<string, string> {
    const h = hueFromString(value);
    return {
        'background-color': `oklch(0.93 0.06 ${h} / 1)`,
        color: `oklch(0.42 0.12 ${h})`,
    };
}

export function initials(...parts: Array<string | null | undefined>): string {
    return parts
        .filter((p): p is string => !!p && p.trim().length > 0)
        .map((p) => p.trim()[0]!.toUpperCase())
        .slice(0, 2)
        .join('');
}

export const HOBBY_ICONS: Record<string, LucideIcon> = {
    LEARNING: LucideGraduationCap,
    SPORTS: LucideDumbbell,
    CREATIVE: LucidePalette,
    SOCIAL: LucideUsers,
    TECHNOLOGY: LucideCpu,
    MUSIC: LucideMusic,
    TRAVEL: LucidePlane,
    COOKING: LucideChefHat,
    GARDENING: LucideSprout,
    READING: LucideBookOpen,
    GAMING: LucideGamepad2,
};

export function hobbyIcon(category: string | null | undefined): LucideIcon {
    return (category && HOBBY_ICONS[category]) || LucideHeart;
}

export function yearsSince(dateIso: string | null | undefined): number | null {
    if (!dateIso) return null;
    const start = new Date(dateIso);
    if (Number.isNaN(start.getTime())) return null;
    const now = new Date();
    let years = now.getFullYear() - start.getFullYear();
    if (now.getMonth() < start.getMonth() || (now.getMonth() === start.getMonth() && now.getDate() < start.getDate())) years--;
    return Math.max(1, years);
}
