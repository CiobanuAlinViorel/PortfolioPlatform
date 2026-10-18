import { TestBed } from "@angular/core/testing";
import { provideHttpClient } from "@angular/common/http";
import { provideHttpClientTesting } from "@angular/common/http/testing";
import { jsPDF } from "jspdf";
import { CvData, CvDownloadService, FONT_FAMILY, PAGE, sanitize } from "./cv-download.service";

type TextCall = { text: string; x: number; y: number; font: string; page: number };

function baseData(overrides: Partial<CvData> = {}): CvData {
    return {
        summary: {
            profile: { id: 1, firstName: "Ada", lastName: "Lovelace", description: "Engineer." },
            contact: { email: "ada@example.com", phone: "+40 700 000 000", city: "Cluj", country: "Romania", github: "github.com/ada", linkedin: "linkedin.com/in/ada" },
        } as unknown as CvData["summary"],
        skills: [{ id: 1, name: "Java", proficiency: "ADVANCED", yearsOfExperience: 3, categoryName: "Backend" }] as unknown as CvData["skills"],
        projects: [{ id: 1, title: "Portfolio", description: "A portfolio.", skills: ["Angular"], sortOrder: 1, year: 2025 }] as unknown as CvData["projects"],
        jobs: [{ id: 1, role: "Dev", companyName: "ACME", startDate: "2023-09-01", endDate: null, projectsCount: 0 }],
        volunteers: [],
        education: [],
        hobbies: [{ id: 1, name: "Chess" }] as unknown as CvData["hobbies"],
        ...overrides,
    };
}

function captureText(service: CvDownloadService): TextCall[] {
    const calls: TextCall[] = [];
    vi.spyOn(service, "createDocument").mockImplementation(() => {
        const doc = new jsPDF({ unit: "mm", format: "a4" });
        const original = doc.text.bind(doc);
        doc.text = ((text: string, x: number, y: number, options?: Parameters<jsPDF["text"]>[3]) => {
            calls.push({ text: String(text), x, y, font: doc.getFont().fontName, page: doc.getCurrentPageInfo().pageNumber });
            return original(text, x, y, options);
        }) as jsPDF["text"];
        return doc;
    });
    return calls;
}

describe("sanitize", () => {
    it("folds Romanian diacritics to plain letters", () => {
        expect(sanitize("Ștefan Țurcanu – Brașov, România")).toBe("Stefan Turcanu - Brasov, Romania");
        expect(sanitize("ăâîșț ĂÂÎȘȚ şţ")).toBe("aaist AAIST st");
    });

    it("replaces typographic punctuation and drops unsupported glyphs", () => {
        expect(sanitize("“quoted” … ‘x’ 🚀")).toBe('"quoted" ... \'x\' ');
    });
});

describe("CvDownloadService", () => {
    let service: CvDownloadService;

    beforeEach(() => {
        TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
        service = TestBed.inject(CvDownloadService);
    });

    afterEach(() => vi.restoreAllMocks());

    it("renders every string with the times font family and inside the page bounds", () => {
        const calls = captureText(service);
        service.buildDocument(baseData());

        expect(calls.length).toBeGreaterThan(0);
        for (const call of calls) {
            expect(call.font).toBe(FONT_FAMILY);
            expect(call.y).toBeGreaterThanOrEqual(PAGE.margin);
            expect(call.y).toBeLessThanOrEqual(PAGE.height - PAGE.margin);
        }
    });

    it("puts the period on its own line when the title would collide with it", () => {
        const calls = captureText(service);
        const longRole = "Senior Principal Distinguished Staff Software Architect and Platform Lead";
        service.buildDocument(baseData({ jobs: [{ id: 1, role: longRole, companyName: "Very Long Company Name International Holdings", startDate: "2023-09-01", endDate: null, projectsCount: 0 }] }));

        const period = calls.find((c) => c.text === "Sep 2023 - Present");
        const titleLines = calls.filter((c) => longRole.includes(c.text.trim()) || c.text.includes("Holdings"));
        expect(period).toBeDefined();
        expect(titleLines.length).toBeGreaterThan(0);
        for (const line of titleLines) {
            expect(period!.y).toBeGreaterThan(line.y);
        }
    });

    it("paginates long content instead of drawing past the bottom margin", () => {
        const calls = captureText(service);
        const description = Array.from({ length: 40 }, (_, i) => `Paragraph ${i} describing the project in a fair amount of detail so it wraps.`).join(" ");
        const projects = Array.from({ length: 5 }, (_, i) => ({ id: i, title: `Project ${i}`, description, skills: ["Angular", "Spring"], sortOrder: i, year: 2025 }));
        const doc = service.buildDocument(baseData({ projects: projects as unknown as CvData["projects"] }));

        expect(doc.getNumberOfPages()).toBeGreaterThan(1);
        for (const call of calls) {
            expect(call.y).toBeLessThanOrEqual(PAGE.height - PAGE.margin);
        }
        expect(calls.some((c) => c.page === 2)).toBe(true);
    });

    it("keeps sidebar and main column text in their own horizontal regions", () => {
        const calls = captureText(service);
        service.buildDocument(baseData());

        const skills = calls.find((c) => c.text === "SKILLS")!;
        const work = calls.find((c) => c.text === "WORK EXPERIENCE")!;
        expect(skills.x).toBe(PAGE.margin);
        expect(work.x).toBeGreaterThan(skills.x + 60);
    });
});
