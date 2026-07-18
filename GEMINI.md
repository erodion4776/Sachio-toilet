# SACHIO MOBILE TOILETS & SANITARY SOLUTIONS LTD — ENTERPRISE PRINCIPLES (GEMINI CONTEXT)

This project, SACHIO Mobile Toilets & Sanitary Solutions Ltd, operates on a **QUOTATION-BASED COMMERCIAL MODEL** rather than an e-commerce fixed-price scheme. Below are the definitive platform, technical, and visual requirements to guide Gemini throughout development:

### I. COMMERCE ARCHITECTURE
1. **No Fixed Listings**: Do NOT hardcode or dynamically display prices (such as ₦45,000, ₦75,000, ₦120,000, etc.) on public-facing screens. All fixed price labels must be replaced with quotation action triggers.
2. **Action Words**: Use "Request Quotation", "Request Current Price", "Contact Sales", or "Speak with Consultant".
3. **Quotation Workflow**:
   - `Quotation Requested` → `Under Review` → `Quotation Generated` → `Quotation Sent` → `Client Accepted` → `Awaiting Payment` → `Payment Confirmed` → `Booking Confirmed` → `Service Delivered` → `Completed`.
4. **Interactive Autocomplete Map**: Allow clients to search address nodes (and pin coordinates) during quotation drafts.

### II. BRANDING & VISION
- **Company Name**: SACHIO MOBILE TOILETS & SANITARY SOLUTIONS LTD
- **Taglines**: *"Prestige in Every Flush."* / *"Comfort. Hygiene. Dignity Anywhere."*
- **Visual Aesthetic**: Highly premium Material 3 theme. Dark/light compatibility. Extensive usage of informative visual cards, detailed galleries, and clean status indicators.

### III. BUILD & PACKAGING COMPLIANCE (CRITICAL)
- Whenever any build, compilation, or file edits are made, you **MUST NOT** remove, disable, or tamper with the custom Gradle task configuration that generates and copies the final APK files.
- You **MUST** ensure that any updates or code changes are correctly compiled and fully included in:
  - `.build-outputs/app-debug.apk` (Must be a valid, real installable APK greater than 1 MB)
  - `APK_DOWNLOAD/app-debug.apk` (Must be a valid, real installable APK greater than 1 MB)
- Always run the compilation and verify the generated file sizes to ensure they are real and complete.

