import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'enumLabel',
  standalone: true,
})
export class EnumLabelPipe implements PipeTransform {
  private readonly overrides: Record<string, string> = {
    // Project statuses
    PLANNING: 'Planning',
    DEVELOPMENT: 'Development',
    TESTING: 'Testing',
    PRODUCTION: 'Production',
    MAINTENANCE: 'Maintenance',
    ARCHIVED: 'Archived',
    // Proficiency & Complexity
    BEGINNER: 'Beginner',
    INTERMEDIATE: 'Intermediate',
    ADVANCED: 'Advanced',
    EXPERT: 'Expert',
    // Difficulty
    LOW: 'Low',
    MEDIUM: 'Medium',
    HIGH: 'High',
    // Volunteer type
    ASSOCIATION: 'Association',
    CLUB: 'Club',
    COMMUNITY: 'Community',
    NGO: 'NGO',
    CHARITY: 'Charity',
    // Volunteer status
    COMPLETED: 'Completed',
    ONGOING: 'Ongoing',
    // Education level
    HIGH_SCHOOL: 'High school',
    ASSOCIATE: 'Associate',
    BACHELOR: "Bachelor's",
    MASTER: "Master's",
    PHD: 'PhD',
    DIPLOMA: 'Diploma',
    CERTIFICATE: 'Certificate',
    BOOTCAMP: 'Bootcamp',
    // Achievement type / recognition level
    AWARD: 'Award',
    CERTIFICATION: 'Certification',
    COMPLETION: 'Completion',
    MILESTONE: 'Milestone',
    PUBLICATION: 'Publication',
    PRESENTATION: 'Presentation',
    CONTRIBUTION: 'Contribution',
    LOCAL: 'Local',
    REGIONAL: 'Regional',
    NATIONAL: 'National',
    INTERNATIONAL: 'International',
    INSTITUTIONAL: 'Institutional',
    // Education status
    IN_PROGRESS: 'In progress',
    DROPPED: 'Dropped',
    // Hobby category
    LEARNING: 'Learning',
    SPORTS: 'Sports',
    CREATIVE: 'Creative',
    SOCIAL: 'Social',
    TECHNOLOGY: 'Technology',
    MUSIC: 'Music',
    TRAVEL: 'Travel',
    COOKING: 'Cooking',
    GARDENING: 'Gardening',
    READING: 'Reading',
    GAMING: 'Gaming',
    // Activity level
    OCCASIONAL: 'Occasional',
    REGULAR: 'Regular',
    FREQUENT: 'Frequent',
    DAILY: 'Daily',
    // Impact level
    IMPACT: 'Impact',
  };

  transform(value: string | null | undefined): string {
    if (!value) return '';
    if (this.overrides[value]) return this.overrides[value];
    // Keep short all-caps tokens (NGO, API) as-is; otherwise "SOME_VALUE" -> "Some value"
    if (/^[A-Z0-9]{1,3}$/.test(value) || !/^[A-Z0-9_]+$/.test(value)) return value;
    const words = value.toLowerCase().split('_');
    return words[0].charAt(0).toUpperCase() + words[0].slice(1) + (words.length > 1 ? ' ' + words.slice(1).join(' ') : '');
  }
}
