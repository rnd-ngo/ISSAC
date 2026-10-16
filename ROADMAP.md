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
- [x] Normalize document names consistently using `DocRead.convertName()`
- [x] Suggest document names using the original filename
- [x] Validate empty and invalid document names
- [x] Invalidate saved analyses when documents are overwritten
- [x] Generate unique temporary upload filenames
- [x] Clean up temporary uploads after processing
- [x] Restore saved documents and their analyses on startup
- [x] Prevent document uploads during active analysis
- [x] Prevent document deletion during active analysis
- [x] Improve `.gitignore` to exclude generated application data

## Remaining

- [ ] Improve `DocRead` success/failure reporting
- [ ] Clean up `DocRead` edge cases
- [ ] Improve UI error reporting
- [x] Validate document names
- [x] Restore existing documents into the registry on startup
- [ ] Improve unsupported-file handling
- [ ] Add delete confirmation
- [ ] Improve unsupported-file handling and report failed imports
- [ ] Add document deletion confirmation
- [ ] Improve document import success/failure reporting
- [ ] Prevent partial document updates if analysis invalidation fails

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
- [x] Disable repeated analysis requests while processing
- [x] Add document-level analysis status indicators and cancellation feedback
- [x] Add per-document Cancel control during active analysis
- [x] Display elapsed cancellation time and brief completion confirmation
- [x] Display live analysis elapsed time and the latest successful analysis duration
- [x] Display total analysis time alongside cancellation elapsed time
- [ ] Restore latest per-document completed-duration display across application restarts (historical timing records already persist separately)
- [x] Remove stale in-memory analysis-duration metadata when analyses or documents are cleared, deleted, or overwritten
- [x] Implement historical timing records, disk persistence, startup reload, and size/model-aware duration estimation
- [x] Capture successful analysis durations and input character counts; show labeled remaining-time estimates when sufficient history exists
- [ ] Fix status-label layout so estimated remaining time is fully visible
- [ ] Validate estimated countdown and estimate-exceeded behavior with controlled test data
- [ ] Evaluate prediction accuracy against real analysis durations and refine comparability criteria
- [ ] Rename timing history file from `.csv` to `.tsv` with a safe migration plan
- [ ] Move timing history to a configurable, portable application data location
- [ ] Improve timing-history corruption recovery, validation, and long-term retention strategy
- [ ] Derive timing model identifier from actual model configuration rather than a hardcoded string
- [ ] Evaluate cancellation-time estimates only after collecting sufficient timing data
- [x] Wait for background worker completion before releasing the analysis lock
- [x] Verify recovery from a simulated AI analysis failure
- [ ] Improve progress indicators and user-facing error messages further
- [x] Add Clear Analysis button with confirmation
- [x] Delete individual saved analyses without deleting source documents
- [x] Allow cleared documents to be analyzed again
- [x] Handle failed AI requests through JavaFX Task failure callbacks
- [x] Display AI analysis errors without saving them as successful results
- [x] Prevent simultaneous document analysis requests
- [ ] Add analysis history and multiple analyses per document
- [ ] Add customizable analysis prompts
- [ ] Improve analysis persistence error reporting

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
- [x] Implement AI readiness checks using the llama.cpp `/health` endpoint
- [x] Add AI status indicator: OFFLINE, STARTING, ONLINE
- [x] Monitor AI server health continuously
- [x] Detect unexpected termination of the managed AI process
- [x] Add Restart AI button
- [x] Restart the AI server without restarting ISSAC
- [x] Prevent duplicate startup of the same managed AI process
- [x] Run manual AI restart on a background thread
- [x] Implement AI server shutdown when ISSAC exits
- [ ] Verify shutdown and cleanup across abnormal termination scenarios
- [x] Prevent analysis requests until AI readiness is confirmed
- [ ] Add AI startup timeout and unresponsive-server handling
- [x] Detect occupied port 8080 and refuse duplicate AI startup
- [x] Display PORT CONFLICT when the managed AI process is absent and port 8080 is occupied
- [ ] Verify identity and compatibility of externally started AI servers before adopting them
- [x] Ensure health monitoring also starts if initial server launch fails
- [ ] Move initial AI startup off the JavaFX Application Thread
- [ ] Remove unused `waitForAIReady()` method after confirming replacement
- [x] Verify llama.cpp terminates after normal ISSAC shutdown
- [ ] Detect model readiness using the local `/health` endpoint
- [x] Prevent analysis requests until the model is ready
- [ ] Handle startup failures and unexpected server termination

### Session Management

- [x] Design document and analysis cleanup methods
- [x] Add Clear Session confirmation workflow
- [x] Test Clear Session with disposable documents
- [x] Block Clear Session while analysis is running
- [x] Clear saved documents and analyses during session reset
- [ ] Improve partial cleanup failure reporting
- [ ] Verify cleanup consistency after filesystem errors
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

**Last Updated:** October 13, 2026

## Current Phase

**Phase 2 — First AI**

**Status: Core AI integration complete; normal shutdown, port-conflict detection, startup recovery, and elapsed analysis timing verified.**

ISSAC has a working offline-first document-analysis prototype with persistent document storage, local AI integration, analysis persistence, AI health monitoring, and manual AI recovery.

Development remains focused on improving reliability before expanding into more advanced analytical capabilities.

---

## Current Objective

Complete the remaining reliability improvements for ISSAC's document-processing and local-AI systems.

Prepare the existing architecture for future analysis history, document grouping, multi-source analysis, and portable deployment.

Avoid introducing major new systems until the current foundation is sufficiently stable.

---

## Last Completed

### Document Management

- Prevented deletion of documents while analysis is active.
- Implemented TXT, PDF, and DOCX ingestion.
- Added document naming normalization using `DocRead.convertName()`.
- Added automatic filename suggestions during document upload.
- Improved invalid document-name handling.
- Implemented document overwrite confirmation.
- Added invalidation of saved analyses when source documents are overwritten.
- Implemented unique temporary upload filenames.
- Added temporary upload cleanup using `finally`.
- Preserved document recovery across application restarts.
- Preserved saved analysis recovery across application restarts.
- Improved Git tracking rules to exclude generated application data.

### Analysis Management

- Added document row statuses: Not Analyzed, Analyzing, Cancelling, Cancelled, Analyzed, and Failed.
- Added a Cancel button beside the active document and elapsed cancellation time for slow requests.
- Added a brief Cancelled confirmation that returns to the document's prior saved-analysis status.
- Kept the analysis lock until both task outcome processing and the worker thread finish.
- Tested simulated failure recovery: errors displayed and subsequent attempts were permitted.
- Cleaned up duplicate list refreshes and temporary cancellation diagnostics.
- Integrated Qwen3.5-0.8B with ISSAC through llama.cpp.
- Connected document ingestion to background AI analysis.
- Implemented persistent analysis storage using `AnalysisStore`.
- Associated saved analyses with their source documents.
- Added a Clear Analysis button.
- Implemented individual analysis deletion while preserving source documents.
- Enabled reanalysis after clearing saved results.
- Improved HTTP error handling in `LocalModel`.
- Added JavaFX Task failure handling for unsuccessful AI requests.
- Prevented simultaneous analysis requests.
- Improved analysis error messages.

### AI Lifecycle and Recovery

- Implemented automatic llama.cpp startup using `ModelServer`.
- Implemented application-managed AI shutdown.
- Added AI server readiness checks through the `/health` endpoint.
- Added AI status indicator with three states:
    - OFFLINE
    - STARTING
    - ONLINE
- Implemented continuous AI health monitoring.
- Added detection of unexpected AI process termination.
- Added Restart AI button.
- Implemented AI restart without restarting ISSAC.
- Added duplicate-process startup protection.
- Implemented synchronized AI server startup.
- Moved manual AI restart operations to a background thread.
- Tested recovery from unexpected Qwen termination.

### October 13 — Reliability and Timing

- Verified recovery from simulated initial AI startup failure using Restart AI; health monitoring continues after failure.
- Strengthened managed llama.cpp shutdown with a bounded graceful wait and forced termination fallback; normal close/relaunch tests passed.
- Added a port 8080 occupancy check to avoid launching a competing server; controlled conflict test passed.
- Added PORT CONFLICT status for occupied port with no running ISSAC-managed AI process; verified recovery after releasing the port.
- Added live elapsed analysis timing, most recent successful duration in the document list, and simultaneous total/cancellation timers.
- Confirmed analysis and cancellation timing through manual tests. Successful duration data currently lives only in memory.

### Session Management

- Implemented Clear Session confirmation.
- Tested session cleanup using disposable documents.
- Added protection against clearing sessions during active analysis.
- Implemented deletion of stored documents and analyses.
- Preserved application stability during normal cleanup operations.

---

## Next Steps

### Priority 1 — AI Reliability

1. Add startup timeout and unresponsive-server handling.
2. Improve Restart AI button behavior during startup.
3. Move initial AI startup off the JavaFX Application Thread.
4. Test abnormal termination and orphaned server recovery.
5. Determine safe behavior for externally started AI servers without trusting an unknown port occupant.
6. Remove obsolete readiness-monitoring code.

### Priority 2 — Document and Analysis Reliability

Completed this session: deletion protection during analysis, per-document status/cancellation UI, and simulated failure recovery. Remaining work:

1. Improve document import success/failure reporting.
2. Improve unsupported-file handling.
3. Add document deletion confirmation.
4. Verify analysis files are not saved after failed AI requests.
5. Improve analysis persistence error reporting.
6. Prevent inconsistent state during partial filesystem failures.
7. Improve error visibility in the JavaFX interface.

### Priority 3 — Analysis Management

1. Clear stale in-memory duration records when analyses/documents are cleared, deleted, or overwritten.
2. Persist measured durations and analysis timestamps for future sessions.
3. Develop conservative, clearly labeled estimated remaining times using historical data.
4. Evaluate cancellation estimates separately; elapsed cancellation time remains authoritative.
5. Design support for multiple analyses per document.
6. Introduce unique analysis identifiers.
7. Add analysis history.
8. Support different analysis types and prompts.
9. Allow analysts to select previously generated analyses.

### Priority 4 — Architectural Preparation

1. Review the existing `AIModel` interface.
2. Preserve separation between AI implementation and application functionality.
3. Reduce hardcoded model and runtime paths.
4. Design configurable application storage locations.
5. Prepare analysis storage for future document grouping.
6. Preserve source-document relationships throughout analysis.

---

## Upcoming Milestones

### Milestone A — Reliable Local AI

ISSAC reliably starts, monitors, stops, and restarts its local AI runtime.

Analysis requests are only accepted when the AI is ready.

Server failures are reported clearly without corrupting saved analyses.

**Status: In progress; major capabilities implemented.**

### Milestone B — Analysis History

ISSAC supports multiple saved analyses per document.

Each analysis has an identifier, timestamp, source-document association, and analysis type.

**Status: Planned.**

### Milestone C — Document Groups

ISSAC supports creating groups of related documents.

Groups can be analyzed together while preserving individual source references.

**Status: Planned.**

### Milestone D — Self-Contained ISSAC

ISSAC can be installed and operated without separately configuring Java, llama.cpp, or the AI model.

Application paths and dependencies are managed through a portable installation structure.

**Status: Planned.**

### Milestone E — Custom ISSAC AI

Investigate and develop specialized AI capabilities for ISSAC.

Begin with model evaluation, task-specific datasets, and small-scale experiments before pursuing custom model training.

**Status: Long-term research objective.**

---

## Known Limitations

### AI Runtime

- The current AI runtime and model locations are hardcoded.
- Initial model startup occurs on the JavaFX Application Thread.
- Analysis requests check model readiness; startup and health-check race conditions still need edge-case testing.
- A running AI process may become unresponsive without terminating.
- The current status monitor may classify an unresponsive server as STARTING.
- Initial startup failure now leaves health monitoring active; automatic restart is not implemented.
- Port occupancy is detected and reported, but ISSAC does not identify or adopt externally launched servers.
- The Restart AI button is re-enabled after process launch, not confirmed model readiness.
- Normal AI shutdown has passed manual tests; forced application termination and crash recovery still need testing.

### Document Processing

- Unsupported document types require clearer error reporting.
- Document import and analysis invalidation are not transactional.
- Document deletion does not yet require confirmation.
- Long documents may exceed the model's context window.
- Document chunking has not been implemented.

### Analysis Management

- Cancellation confirms that ISSAC's task worker finished; it does not establish immediate termination of llama.cpp server-side generation.
- Only one saved analysis is supported per document.
- Successful analysis durations are held in memory only and are not yet cleared consistently when analyses or documents are removed.
- Estimated remaining times are not implemented; only elapsed times are measured.
- Analysis history is not implemented.
- Custom analysis prompts are not yet available.
- Source-level evidence validation is not implemented.
- AI-generated claims are not automatically verified against source text.
- Failed analysis persistence and partial-save behavior require further testing.

### Session and Storage

- Session cleanup is not transactional.
- Partial filesystem failures may leave inconsistent state.
- Ordinary file deletion does not guarantee secure erasure.
- Unexpected termination may leave temporary files or incomplete operations.
- Portable storage and Safe Detach are not yet implemented.

---

## Development Priority

**Finish Phase 2 reliability work before introducing major Phase 3 capabilities.**

The October 13 session verified normal AI shutdown, startup port-conflict handling, status reporting, and recovery from a simulated initial startup failure. It also added elapsed analysis timing and combined cancellation timing. Next, clean up duration metadata, persist timing measurements, and introduce cautious time estimates while continuing startup edge-case and document lifecycle testing.

Once these are stable, begin designing analysis history and multiple analyses per document.

Document grouping and combined analysis should follow the analysis-storage redesign.

---

## Do Not Forget

- Portability remains a core design requirement.
- AI implementations should remain replaceable.
- Ingestion and AI reasoning must remain separate.
- Evidence provenance is fundamental.
- Source facts and model inferences must remain distinguishable.
- Analyst validation is required for consequential analytical products.
- Sensitive information must be handled according to applicable requirements.
- Portability does not automatically provide security or isolation.
- Major changes should be implemented incrementally and tested.
- Understand each Java implementation before expanding it.
- Record future ideas without interrupting current development.
- The roadmap is a living document and should evolve with the application.

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