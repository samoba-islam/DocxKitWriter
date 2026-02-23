<p align="center">
  <h1 align="center">📄 DocxKit</h1>
  <p align="center">
    A lightweight Android library for generating DOCX files with coordinate-based text positioning.
    <br />
    <strong>No external dependencies</strong> — uses pure OOXML (Office Open XML) format.
  </p>
</p>

<p align="center">
  <a href="https://jitpack.io/#samoba-islam/DocxKitWriter"><img src="https://jitpack.io/v/samoba-islam/DocxKitWriter.svg" alt="JitPack" /></a>
  <a href="https://opensource.org/licenses/GPL-3.0"><img src="https://img.shields.io/badge/License-GPL_3.0-blue.svg" alt="License: GPL-3.0" /></a>
  <img src="https://img.shields.io/badge/API-21%2B-brightgreen.svg" alt="API 21+" />
</p>

---

## ✨ Features

- 📄 **Page Size Configuration** — A4, A3, A5, Letter, Legal, Tabloid, B4, B5, or custom sizes
- 📍 **Coordinate-Based Positioning** — Place text boxes at exact pixel coordinates (great for OCR output)
- 🔄 **Text Rotation Support** — Handle rotated text blocks with corner-point-based angle detection
- 📑 **Multi-Page Documents** — Create documents with multiple pages, each with independent content
- 📐 **Auto-Scaling** — Content automatically scales and centers to fit page dimensions
- 🎨 **Configurable Fonts** — Set default font family, size, bold, and italic styles
- 📱 **Android Extensions** — Write to `Uri`, convert `Rect` to bounding boxes, and more
- 🚫 **Zero Dependencies** — Pure Kotlin + Android SDK only

---

## 📦 Installation

**Step 1.** Add the JitPack repository to your root `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

**Step 2.** Add the dependency to your module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.samoba-islam:DocxKitWriter:1.0.0")
}
```

---

## 🚀 Quick Start

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
    writer.writePositioned(content, output, contentWidth = 1000, contentHeight = 1500)
}
```

### Write to Android Uri

```kotlin
import com.samoba.docxkit.ext.*

val writer = DocxWriter()
val content = DocxStructuredText.fromPlainText("Hello World!")

// Simple write
writer.writeToUri(context, content, outputUri)

// Positioned write
writer.writePositionedToUri(context, content, outputUri, contentWidth = 800, contentHeight = 1200)
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

---

## ⚙️ Configuration

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
| `PageSize.Letter` | 8.5″ × 11″ |
| `PageSize.Legal` | 8.5″ × 14″ |
| `PageSize.Tabloid` | 11″ × 17″ |
| `PageSize.B4` | 250mm × 353mm |
| `PageSize.B5` | 176mm × 250mm |
| `PageSize.Custom(w, h)` | Custom (twips) |

### Page Margins

| Preset | Description |
|--------|-------------|
| `PageMargins.DEFAULT` | 1″ all sides |
| `PageMargins.NARROW` | 0.5″ all sides |
| `PageMargins.NONE` | No margins |
| `PageMargins.MODERATE` | 0.75″ all sides |

---

## 📐 Unit Conversion

DocxKit uses EMU (English Metric Units) and Twips internally:

```kotlin
// Pixel conversions
val emu = UnitConverter.pixelsToEmu(100)
val twips = UnitConverter.pixelsToTwips(50)

// Inch conversions
val emuFromInch = UnitConverter.inchesToEmu(1.5f)
val twipsFromInch = UnitConverter.inchesToTwips(1.0f)

// Millimeter conversions
val emuFromMm = UnitConverter.mmToEmu(25.4f)
```

---

## 📱 Android Extensions

```kotlin
import com.samoba.docxkit.ext.*

// Convert Android Rect to DocxBoundingBox
val box = rect.toDocxBoundingBox()

// Write to Android Uri
writer.writeToUri(context, content, uri)
writer.writePositionedToUri(context, content, uri, width, height)
writer.writeDirectToUri(context, content, uri)
writer.writeMultiPageToUri(context, pages, uri)

// Builder helpers using Android Rect
val block = docxTextBlock("Hello", rect)
val line = docxTextLine("Hello", rect)
val element = docxTextElement("Hello", rect)
```

---

## 📋 API Reference

| Class | Description |
|-------|-------------|
| `DocxWriter` | Main entry point for generating DOCX files |
| `DocxConfig` | Configuration (page size, margins, fonts) |
| `DocxStructuredText` | Container for text content (blocks or plain text) |
| `DocxTextBlock` | A block of text with positioning, rotation, and styling |
| `DocxTextLine` | A line of text within a block |
| `DocxTextElement` | A word or symbol with individual bounding box |
| `DocxBoundingBox` | Platform-agnostic rectangle for positioning |
| `DocxPage` | Represents a single page in multi-page documents |
| `PageSize` | Predefined and custom page sizes |
| `PageMargins` | Predefined and custom margin presets |
| `UnitConverter` | Utility for EMU, twips, pixels, inches, mm conversions |

---

## 📄 License

```
Copyright 2026 Samoba

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
```

See [LICENSE](LICENSE) for the full text.
