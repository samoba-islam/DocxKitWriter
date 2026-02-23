# DocxKit

A lightweight Android library for generating DOCX files with coordinate-based text positioning. 

**No external dependencies** - uses pure OOXML (Office Open XML) format.

## Features

- 📄 **Page Size Configuration** - A4, Letter, Legal, custom sizes
- 📍 **Coordinate-Based Positioning** - Place text boxes at exact pixel coordinates
- 🔄 **Text Rotation Support** - Handle rotated text blocks
- 📑 **Multi-Page Documents** - Create documents with multiple pages
- 📐 **Auto-Scaling** - Content automatically scales to fit page
- 🎨 **Configurable Fonts** - Set default font family and size

## Installation

Add JitPack repository to your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}
```

Add the dependency:

```kotlin
dependencies {
    implementation("com.github.samoba-islam:DocxKitWriter:1.0.0")
}
```

## Quick Start

### Simple Text Document

```kotlin
val writer = DocxWriter()
val content = DocxStructuredText.fromPlainText("Hello World!")

FileOutputStream("output.docx").use { output ->
    writer.write(content, output)
}
```

### Positioned Text (OCR-style)

```kotlin
val blocks = listOf(
    DocxTextBlock(
        lines = listOf(
            DocxTextLine(
                elements = emptyList(),
                text = "Hello",
                boundingBox = DocxBoundingBox(100, 100, 300, 150)
            )
        ),
        text = "Hello",
        boundingBox = DocxBoundingBox(100, 100, 300, 150)
    )
)

val content = DocxStructuredText.fromBlocks(blocks)
val writer = DocxWriter()

FileOutputStream("output.docx").use { output ->
    writer.writePositioned(content, output, imageWidth = 1000, imageHeight = 1500)
}
```

### With Android Uri

```kotlin
val writer = DocxWriter()
val content = DocxStructuredText.fromPlainText("Hello World!")

// Using extension function
writer.writeToUri(context, content, outputUri)
```

### Multi-Page Document

```kotlin
val pages = listOf(
    DocxPage(content1, contentWidth = 800, contentHeight = 1200),
    DocxPage(content2, contentWidth = 800, contentHeight = 1200)
)

val writer = DocxWriter()
FileOutputStream("output.docx").use { output ->
    writer.writeMultiPage(pages, output)
}
```

## Configuration

```kotlin
val config = DocxConfig.builder()
    .pageSize(PageSize.Letter)
    .margins(PageMargins.NARROW)
    .fontFamily("Times New Roman")
    .fontSize(12)
    .build()

val writer = DocxWriter(config)
```

### Page Sizes

| Size | Dimensions |
|------|------------|
| `PageSize.A4` | 210mm × 297mm |
| `PageSize.A3` | 297mm × 420mm |
| `PageSize.A5` | 148mm × 210mm |
| `PageSize.Letter` | 8.5" × 11" |
| `PageSize.Legal` | 8.5" × 14" |
| `PageSize.Custom(w, h)` | Custom twips |

### Page Margins

| Preset | Description |
|--------|-------------|
| `PageMargins.DEFAULT` | 1" all sides |
| `PageMargins.NARROW` | 0.5" all sides |
| `PageMargins.NONE` | No margins |
| `PageMargins.MODERATE` | 0.75" all sides |

## Unit Conversion

DocxKit uses EMU (English Metric Units) and Twips internally:

```kotlin
// Convert pixels to EMU
val emu = UnitConverter.pixelsToEmu(100)

// Convert inches to twips
val twips = UnitConverter.inchesToTwips(1.5f)
```

## Android Extensions

```kotlin
import com.samoba.docxkit.ext.*

// Convert Android Rect to DocxBoundingBox
val box = rect.toDocxBoundingBox()

// Write to Android Uri
writer.writeToUri(context, content, uri)
writer.writePositionedToUri(context, content, uri, width, height)
```

## License

```
Copyright 2026 Samoba

Licensed under the GNU General Public License v3.0
```
