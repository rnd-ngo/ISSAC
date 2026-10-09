# ISSAC Development Roadmap

## North Star

ISSAC (Intelligence Specialist Supporting Analysis Code) is a portable,
permissioned AI-assisted analysis environment designed to ingest information
from multiple sources, preserve evidence and provenance, analyze that
information, and produce traceable analytical products for analyst review.

Development and testing involving sensitive information must remain within
the handling requirements applicable to the environment. Portability itself
should not be treated as a security boundary.

---

# Phase 1 — Foundation

Build the basic application and document-processing infrastructure.

## Completed

- [x] Create Maven Java project
- [x] Implement TXT ingestion
- [x] Implement PDF ingestion using PDFBox
- [x] Implement DOCX ingestion using Apache POI
- [x] Store processed document text
- [x] Create basic document registry
- [x] Create JavaFX application
- [x] Implement document upload workflow
- [x] Add user-defined document names
- [x] Add overwrite confirmation
- [x] Add document ListView
- [x] Implement document deletion
- [x] Implement temporary upload staging

## Remaining

- [ ] Improve `DocRead` success/failure reporting
- [ ] Clean up `DocRead` edge cases
- [ ] Improve UI error reporting
- [ ] Validate document names
- [x] Restore existing documents into the registry on startup
- [ ] Improve unsupported-file handling
- [ ] Add delete confirmation

---

# Phase 2 — First AI

Connect ISSAC to its first local language model.

## Initial AI Architecture

ISSAC should separate the AI model from the rest of the application.

The basic relationship is:

    ISSAC
       |
       v
    AIModel
       |
       v
    AI Runtime
       |
       v
    Local Model

This allows the underlying model or runtime to be replaced later without
requiring the rest of ISSAC to be redesigned.

## Tasks

### Local AI Foundation

- [x] Create `issac.ai` package
- [x] Define the `AIModel` concept
- [x] Separate AI model capability from ISSAC functionality
- [x] Select local AI runtime — llama.cpp
- [x] Download and verify llama.cpp runtime binaries
- [x] Select the first local language model — Qwen3.5-0.8B
- [x] Download GGUF model
- [x] Run model independently
- [x] Complete first local inference

### Java Integration

- [x] Connect Java to the local AI runtime using HTTP
- [x] Implement the first local `AIModel` — `LocalModel`
- [x] Send "Hello ISSAC" to the model
- [x] Receive and parse the model response
- [x] Connect `DocRead` output to the AI
- [x] Analyze text extracted from uploaded TXT/PDF/DOCX documents
- [x] Implement background analysis using JavaFX `Task`
- [x] Display AI analysis inside the JavaFX interface

### Analysis and User Experience

- [x] Create an initial document-analysis prompt
- [x] Experiment with structured analysis instructions
- [x] Identify limitations involving unsupported inferences and source accuracy
- [ ] Support interactive questions about an uploaded document
- [x] Associate completed analyses with their source documents
- [x] Automatically display cached analyses when selecting documents
- [x] Save analyses to disk and restore them on startup
- [ ] Invalidate cached analyses when documents are replaced or deleted
- [ ] Disable repeated analysis requests while processing
- [ ] Improve progress indicators and user-facing error messages

### Reliability and Document Handling

- [ ] Detect and handle AI server connection failures
- [ ] Handle model context-window limitations
- [ ] Implement document chunking for longer inputs
- [ ] Restore the saved document registry when ISSAC starts
- [ ] Validate extracted information against supporting source text
- [ ] Distinguish document facts from AI inferences

### Local Model Lifecycle

- [x] Implement `ModelServer` using Java `ProcessBuilder`
- [x] Launch llama.cpp automatically when ISSAC starts
- [x] Successfully connect ISSAC to the automatically launched Qwen server
- [ ] Verify llama.cpp terminates when ISSAC closes
- [ ] Detect model readiness using the local `/health` endpoint
- [ ] Prevent analysis requests until the model is ready
- [ ] Handle startup failures and unexpected server termination

### Session Management

- [x] Design document and analysis cleanup methods
- [x] Add Clear Session confirmation workflow
- [ ] Verify Clear Session removes documents, uploads, analyses, and in-memory state
- [ ] Verify session reset is blocked while analysis is running
- [ ] Handle partial cleanup failures
- [ ] Prevent active or stale analysis tasks from recreating deleted data

## Milestone

ISSAC can ingest TXT, PDF, and DOCX documents, send their extracted text to a locally running AI model, and display AI-generated analysis directly inside its JavaFX interface without requiring an internet connection.

**Status: Core milestone achieved.**

Interactive document questioning, persistent analysis storage, evidence-backed extraction, and long-document handling remain in development.

---

# Phase 3 — Structured Analysis

Move beyond receiving arbitrary text responses from the AI.

ISSAC should begin converting source material into structured information.

Potential information categories include:

- People
- Organizations
- Locations
- Dates
- Times
- Activities
- Events
- Relationships
- Key facts
- Uncertainties
- Contradictions
- Source references

ISSAC should maintain a distinction between:

    SOURCE FACT
         |
         v
    EXTRACTED INFORMATION
         |
         v
    MODEL INFERENCE
         |
         v
    ANALYTIC ASSESSMENT

## Tasks

- [ ] Define structured analysis objects
- [ ] Extract entities from documents
- [ ] Extract events
- [ ] Extract dates and times
- [ ] Extract locations
- [ ] Extract relationships
- [ ] Identify key information
- [ ] Represent uncertainty
- [ ] Detect possible contradictions
- [ ] Connect extracted information to its original source
- [ ] Begin evidence/provenance tracking

---

# Phase 4 — Multimodal Ingestion

Expand the types of information ISSAC can process.

## Images

- [ ] Image ingestion
- [ ] OCR
- [ ] Visual analysis
- [ ] Image metadata extraction

## Scanned Documents

- [ ] Detect image-only PDFs
- [ ] Convert PDF pages into images
- [ ] Perform OCR
- [ ] Preserve page references
- [ ] Send extracted text through the normal analysis pipeline

## Audio

- [ ] Audio ingestion
- [ ] Speech-to-text
- [ ] Timestamp transcription
- [ ] Audio metadata extraction
- [ ] Feed transcription into analysis system

## Video

- [ ] Video ingestion
- [ ] Extract frames
- [ ] Extract audio
- [ ] Generate transcription
- [ ] Preserve timestamps
- [ ] Detect meaningful changes/events
- [ ] Associate observations with video timestamps
- [ ] Feed video-derived evidence into analysis system

## Structured Data

Potential future formats:

- [ ] CSV
- [ ] XLSX
- [ ] Other structured datasets as required

## Architectural Goal

Different source formats should eventually feed a common evidence system:

    PDF --------\
    DOCX --------\
    TXT ----------\
    Image ---------> Ingestion -> Evidence -> Analysis
    Audio ---------/
    Video --------/
    Dataset ------/

The AI reasoning layer should not need to understand how every individual
file format is decoded.

---

# Phase 5 — Knowledge and Memory

Develop ISSAC from a document analyzer into a persistent analytical system.

Potential knowledge structures include:

- Entities
- People
- Organizations
- Locations
- Relationships
- Events
- Timelines
- Sources
- Previous analysis
- Contradictions
- Analyst corrections
- Analyst preferences

ISSAC's memory should not automatically mean modifying the AI model itself.

Instead:

    AI Model
       +
    Knowledge Store
       +
    Previous Analysis
       +
    Analyst Corrections
       +
    Analyst Preferences
       =
    More Context-Aware ISSAC

## Tasks

- [ ] Design knowledge storage
- [ ] Preserve entities across documents
- [ ] Link related events
- [ ] Build timelines
- [ ] Store previous analysis
- [ ] Store analyst corrections
- [ ] Retrieve relevant knowledge during analysis

---

# Phase 6 — Source Acquisition

Allow ISSAC to retrieve information through explicitly authorized tools.

Potential tools could eventually include:

    searchDocuments(...)
    searchInternalSources(...)
    searchAuthorizedServers(...)
    searchExternalSources(...)

ISSAC should not receive unrestricted access to external systems.

Access should be:

- Explicit
- Permissioned
- Auditable
- Source-aware

Retrieved information should enter the same evidence/provenance system as
documents manually supplied to ISSAC.

## Tasks

- [ ] Local document search
- [ ] Internal source search
- [ ] Authorized server retrieval
- [ ] External/web source retrieval where permitted
- [ ] Source metadata preservation
- [ ] Retrieval logging
- [ ] Permission controls

---

# Phase 7 — Analysis Engine

Develop higher-level analytical capabilities across multiple sources.

Potential capabilities include:

- Entity resolution
- Timeline construction
- Relationship analysis
- Pattern detection
- Source comparison
- Contradiction detection
- Assessment generation
- Confidence and uncertainty representation

Conceptually:

    Multiple Sources
          |
          v
    Evidence Store
          |
          v
    Entity Resolution
          |
          v
    Timeline / Relationship Analysis
          |
          v
    Pattern Detection
          |
          v
    Source Comparison
          |
          v
    Assessments

## Tasks

- [ ] Compare information across sources
- [ ] Resolve references to the same entity
- [ ] Build multi-source timelines
- [ ] Identify relationships
- [ ] Identify patterns
- [ ] Identify conflicting information
- [ ] Generate assessments
- [ ] Represent confidence and uncertainty
- [ ] Preserve evidence supporting assessments

---

# Phase 8 — Product Engine

Allow ISSAC to transform analysis into useful analytical products.

Potential products include:

- Reports
- Briefs
- Summaries
- Timelines
- Matrices
- Charts
- Relationship diagrams
- Maps
- Evidence/source packages
- Other defined analytical products

The desired workflow is:

    Sources
       |
       v
    Evidence
       |
       v
    Analysis
       |
       v
    Assessment
       |
       v
    Draft Product
       |
       v
    Validation
       |
       v
    Analyst Review
       |
       v
    Final Product

ISSAC should be able to explain which evidence contributed to significant
claims or assessments.

## Tasks

- [ ] Define product templates
- [ ] Generate draft reports
- [ ] Generate summaries
- [ ] Generate timelines
- [ ] Generate matrices
- [ ] Generate charts
- [ ] Generate relationship diagrams
- [ ] Generate maps where appropriate
- [ ] Attach source references to generated content
- [ ] Add validation checks
- [ ] Create analyst review workflow

---

# Phase 9 — Learning From the Analyst

Allow ISSAC to become more consistent with the analyst's preferences and
working style.

Potential feedback includes:

- Accepted analysis
- Rejected analysis
- Corrections
- Entity corrections
- Rewritten assessments
- Preferred product structures
- Preferred presentation styles

Conceptually:

    Generic Model
         +
    ISSAC Knowledge
         +
    Analyst Preferences
         +
    Previous Corrections
         +
    Task Instructions
         |
         v
    More Consistent ISSAC Behavior

Model fine-tuning or custom machine-learning techniques should be considered
later, after enough useful examples and feedback exist to justify them.

## Tasks

- [ ] Record analyst feedback
- [ ] Record corrections
- [ ] Store appropriate preferences
- [ ] Retrieve relevant previous feedback
- [ ] Evaluate whether feedback improves future output
- [ ] Investigate fine-tuning only when sufficient data exists

---

# Phase 10 — Portability and Deployment

Formalize ISSAC as a portable analysis environment.

A future instance may resemble:

    ISSAC/
    |
    +-- application/
    |
    +-- models/
    |
    +-- data/
    |   +-- documents/
    |   +-- uploads/
    |   +-- analysis/
    |
    +-- config/
    |
    +-- logs/
    |
    +-- manifest/

## Goals

- [ ] Portable installation
- [ ] Offline operation where practical
- [ ] Local AI capability
- [ ] Controlled/configurable storage locations
- [ ] Minimize unintended persistence outside ISSAC storage
- [ ] Track ISSAC-created persistent files
- [ ] Support reproducible/clonable ISSAC instances
- [ ] Track application version
- [ ] Track model version
- [ ] Track configuration version
- [ ] Create instance manifest
- [ ] Develop auditing capabilities
- [ ] Define backup/recovery strategy
- [ ] Separate temporary session storage from explicitly retained products
- [ ] Implement manual Clear Session and Safe Detach workflows
- [ ] Support explicitly saving selected products for transfer
- [ ] Verify cleanup before removable-device ejection
- [ ] Define behavior for interrupted sessions and unexpected device removal
- [ ] Investigate encryption and limitations of file deletion on flash storage

Running ISSAC from removable storage should not by itself be considered a
security boundary. The host operating system, runtime, memory, temporary
storage, caches, logs, and other environmental factors may still affect data
handling.

---

# Future Ideas / Parking Lot

Ideas go here when they are valuable but should not interrupt the current
development milestone.

Possible future areas:

- Advanced image analysis
- Video event analysis
- Audio analysis
- Geographic visualization
- Interactive maps
- Relationship/network visualization
- Advanced timelines
- Multi-model reasoning
- Model comparison
- Specialized AI models
- Automated source discovery
- Product templates
- History and recovery
- Document version history
- Advanced logging
- Model fine-tuning
- Custom machine-learning models

This section is expected to grow.

Adding an idea here does **not** mean it must be implemented immediately.

---

# Development Principles

## 1. Understand What We Build

Avoid adding major systems that cannot be understood, maintained, or
debugged.

## 2. Build Incrementally

Each major capability should have a small working milestone before becoming
more complicated.

## 3. Maintain Modularity

Components should be replaceable where practical.

Examples:

    AIModel -> LocalModel
            -> FutureModel

Changing an AI implementation should not require rewriting unrelated
document-processing or UI code.

## 4. Separate Ingestion From Reasoning

File-format handling belongs in ingestion.

AI reasoning should operate on extracted information rather than becoming
responsible for decoding every file format.

## 5. Preserve Provenance

ISSAC should maintain the ability to determine where important information
came from.

## 6. Distinguish Evidence From Inference

Source information, extracted information, AI inference, and analyst
assessment should not silently become the same thing.

## 7. Keep Humans in the Loop

Consequential analytical products should support analyst validation and
review.

## 8. Portability Is a Design Requirement

Storage locations, model dependencies, runtimes, configuration, and
persistent state should be designed with portability in mind.

## 9. Security Must Be Explicit

Do not assume portability automatically provides isolation or security.

## 10. Do Not Let Future Features Stop Current Progress

Record future ideas, make reasonable architectural allowances for them, and
continue working on the current milestone.

---

# Current Development Position

## Current Phase

**Phase 2 — First AI**

## Current Objective

Improve reliability, session cleanup, and document lifecycle management for ISSAC's working local-AI prototype.

## Last Completed

- Integrated Qwen3.5-0.8B with ISSAC through llama.cpp and `LocalModel`.
- Connected document ingestion to background AI analysis in JavaFX.
- Associated analysis results with their source documents.
- Implemented document recovery from saved files on application startup.
- Implemented analysis persistence using `AnalysisStore`.
- Successfully restored previously generated analyses after restarting ISSAC.
- Implemented automatic startup of llama.cpp through `ModelServer`.
- Added initial session-cleanup methods and Clear Session confirmation logic.
- Began restricting concurrent analyses to avoid conflicting operations.

## Next Steps

1. Test Clear Session with disposable documents and confirm all intended session files are deleted.
2. Verify cleanup cannot occur while an analysis is active.
3. Verify `ModelServer` shuts down llama.cpp when ISSAC exits.
4. Add model-readiness detection to prevent HTTP 503 errors during startup.
5. Invalidate saved analyses when documents are overwritten or deleted.
6. Improve partial-failure reporting and error handling.
7. Revisit portable session storage and Safe Detach after the current cleanup workflow is reliable.

## Known Limitations

- A running llama.cpp process does not necessarily mean the model is ready.
- Saved analyses may become stale if a document is replaced.
- Session cleanup is not transactional and requires additional testing.
- Ordinary file deletion does not guarantee secure erasure.
- Unexpected termination or USB removal may leave temporary files behind.
- Long documents may exceed the model's context window.

## Development Priority

Complete and verify the existing reliability features before introducing larger AI capabilities or portable deployment changes.

## Do Not Forget

- Portability is a core design goal.
- AI implementations should remain replaceable.
- Ingestion and AI reasoning should remain separate.
- Evidence and source provenance are fundamental.
- The system should distinguish source facts from AI inference.
- Analyst review remains part of consequential product generation.
- New ideas should normally enter the Future Ideas / Parking Lot before
  interrupting the current milestone.
- Do not prematurely build future phases.
- The roadmap is allowed to change.

---

# Handoff Notes

If development moves to a new ChatGPT conversation or another developer,
provide:

1. The ISSAC repository.
2. This `ROADMAP.md`.
3. The current source code.
4. The following instruction:

> Read the ISSAC roadmap and current repository before recommending changes.
> Continue from the Current Development Position. I am learning while building
> this project, so help me reason through implementation decisions rather than
> simply writing the entire project for me.

The roadmap represents the current plan, not a permanent specification.
Update it as ISSAC evolves.