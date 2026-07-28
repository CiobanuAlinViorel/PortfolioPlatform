import { Component, ElementRef, EventEmitter, Input, OnChanges, OnDestroy, Output, SimpleChanges, ViewChild, signal } from "@angular/core";
import { LucideCheck, LucideX, LucideZoomIn, LucideZoomOut } from "@lucide/angular";

const VIEWPORT_SIZE = 288;
const OUTPUT_SIZE = 512;
const MIN_ZOOM = 1;
const MAX_ZOOM = 3;

@Component({
    selector: "app-image-cropper",
    imports: [LucideCheck, LucideX, LucideZoomIn, LucideZoomOut],
    templateUrl: "../ui/image-cropper.component.html",
})
export class ImageCropperComponent implements OnChanges, OnDestroy {
    @Input({ required: true }) file!: File;
    @Output() readonly cropped = new EventEmitter<File>();
    @Output() readonly cancelled = new EventEmitter<void>();

    @ViewChild("imageEl") private readonly imageElRef?: ElementRef<HTMLImageElement>;

    readonly viewportSize = VIEWPORT_SIZE;
    readonly minZoom = MIN_ZOOM;
    readonly maxZoom = MAX_ZOOM;
    readonly zoom = signal(MIN_ZOOM);
    readonly imageSrc = signal<string | null>(null);

    translateX = 0;
    translateY = 0;

    private naturalWidth = 0;
    private naturalHeight = 0;
    private baseScale = 1;
    private objectUrl: string | null = null;

    private dragging = false;
    private dragStartX = 0;
    private dragStartY = 0;
    private dragStartTranslateX = 0;
    private dragStartTranslateY = 0;

    private readonly onPointerMove = (event: PointerEvent) => this.handlePointerMove(event);
    private readonly onPointerUp = () => this.handlePointerUp();

    ngOnChanges(changes: SimpleChanges): void {
        if (changes["file"] && this.file) {
            this.loadImage(this.file);
        }
    }

    ngOnDestroy(): void {
        this.releaseObjectUrl();
        this.detachDragListeners();
    }

    get displayedWidth(): number {
        return this.naturalWidth * this.effectiveScale();
    }

    get displayedHeight(): number {
        return this.naturalHeight * this.effectiveScale();
    }

    onImageLoad(): void {
        const img = this.imageElRef?.nativeElement;
        if (!img) return;

        this.naturalWidth = img.naturalWidth;
        this.naturalHeight = img.naturalHeight;
        this.baseScale = this.viewportSize / Math.min(this.naturalWidth, this.naturalHeight);
        this.zoom.set(MIN_ZOOM);
        this.translateX = (this.viewportSize - this.displayedWidth) / 2;
        this.translateY = (this.viewportSize - this.displayedHeight) / 2;
    }

    onZoomChange(value: number): void {
        const previousScale = this.effectiveScale();
        const center = this.viewportSize / 2;
        const imagePointX = (center - this.translateX) / previousScale;
        const imagePointY = (center - this.translateY) / previousScale;

        this.zoom.set(Math.min(this.maxZoom, Math.max(this.minZoom, value)));

        const newScale = this.effectiveScale();
        this.translateX = center - imagePointX * newScale;
        this.translateY = center - imagePointY * newScale;
        this.clampTranslation();
    }

    onPointerDown(event: PointerEvent): void {
        this.dragging = true;
        this.dragStartX = event.clientX;
        this.dragStartY = event.clientY;
        this.dragStartTranslateX = this.translateX;
        this.dragStartTranslateY = this.translateY;
        window.addEventListener("pointermove", this.onPointerMove);
        window.addEventListener("pointerup", this.onPointerUp);
    }

    confirm(): void {
        const img = this.imageElRef?.nativeElement;
        if (!img) return;

        const scale = this.effectiveScale();
        const sourceSize = this.viewportSize / scale;
        const sourceX = -this.translateX / scale;
        const sourceY = -this.translateY / scale;

        const canvas = document.createElement("canvas");
        canvas.width = OUTPUT_SIZE;
        canvas.height = OUTPUT_SIZE;
        const ctx = canvas.getContext("2d");
        if (!ctx) return;

        ctx.beginPath();
        ctx.arc(OUTPUT_SIZE / 2, OUTPUT_SIZE / 2, OUTPUT_SIZE / 2, 0, Math.PI * 2);
        ctx.closePath();
        ctx.clip();
        ctx.drawImage(img, sourceX, sourceY, sourceSize, sourceSize, 0, 0, OUTPUT_SIZE, OUTPUT_SIZE);

        canvas.toBlob((blob) => {
            if (blob) {
                this.cropped.emit(new File([blob], "avatar.png", { type: "image/png" }));
            }
        }, "image/png");
    }

    cancel(): void {
        this.cancelled.emit();
    }

    private handlePointerMove(event: PointerEvent): void {
        if (!this.dragging) return;
        this.translateX = this.dragStartTranslateX + (event.clientX - this.dragStartX);
        this.translateY = this.dragStartTranslateY + (event.clientY - this.dragStartY);
        this.clampTranslation();
    }

    private handlePointerUp(): void {
        this.dragging = false;
        this.detachDragListeners();
    }

    private detachDragListeners(): void {
        window.removeEventListener("pointermove", this.onPointerMove);
        window.removeEventListener("pointerup", this.onPointerUp);
    }

    private clampTranslation(): void {
        const minX = this.viewportSize - this.displayedWidth;
        const minY = this.viewportSize - this.displayedHeight;
        this.translateX = Math.min(0, Math.max(minX, this.translateX));
        this.translateY = Math.min(0, Math.max(minY, this.translateY));
    }

    private effectiveScale(): number {
        return this.baseScale * this.zoom();
    }

    private loadImage(file: File): void {
        this.releaseObjectUrl();
        this.objectUrl = URL.createObjectURL(file);
        this.imageSrc.set(this.objectUrl);
        this.zoom.set(MIN_ZOOM);
        this.translateX = 0;
        this.translateY = 0;
    }

    private releaseObjectUrl(): void {
        if (this.objectUrl) {
            URL.revokeObjectURL(this.objectUrl);
            this.objectUrl = null;
        }
    }
}
