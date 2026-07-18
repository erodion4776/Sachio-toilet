# SACHIO MOBILE TOILETS & SANITARY SOLUTIONS LTD - MASTER PROMPT & SYSTEM INSTRUCTIONS

This document serves as the master specification and persistent system instructions for Google AI Studio and all agent interactions regarding the build of the SACHIO platform. All AI agents, model generations, and system processes must strictly adhere to the rules, business models, workflows, and styling parameters defined below.

---

## BUSINESS DESCRIPTION & PRINCIPLES
Build a world-class enterprise native mobile application (Kotlin & Jetpack Compose) for **SACHIO Mobile Toilets & Sanitary Solutions Ltd**.

* **Quotation-Based Model Only**: This application is NOT an e-commerce store with fixed pricing. SACHIO operates on a quotation-based business model. Pricing varies based on location, logistics, fuel costs, quantity required, rental duration, maintenance requirements, market conditions, import costs, and operational costs.
* **Important Pricing Rule**: 
  - **Never display fixed pricing** (e.g., ₦100,000, ₦500,000, ₦1,200,000, etc.) for products, rentals, or purchases.
  - Instead, display actionable text: **"Request Quotation"**, **"Request Current Price"**, **"Contact Sales"**, or **"Speak with Consultant"**.

---

## PLATFORM ROLES
The platform functions across the following key dimensions:
1. **Product Discovery Platform** (Showcasing premium standard to presidential luxury cabins, cleaning services, etc.)
2. **Quotation Management Platform** (Submit, review, generate, approve, and track quotes)
3. **Booking & Service Tracking Platform** (Keep track of confirmed service delivery, staff dispatch, and completion)
4. **Customer Relationship & Sales Portal** (Consultation bookings, WhatsApp callbacks, site inspection schedules, and reviews)

---

## COMPANY BRANDING & STYLE GUIDE
* **Company Name**: SACHIO MOBILE TOILETS & SANITARY SOLUTIONS LTD
* **Core Tagline**: *"Prestige in Every Flush."*
* **Alternative Tagline**: *"Comfort. Hygiene. Dignity Anywhere."*
* **Design Guidelines**: 
  - Provide a premium luxury experience utilizing high-contrast Material Design 3.
  - Use generous layout padding and negative space.
  - Implement a sleek modern Dark/Light theme, subtle translucent glassmorphism effects, elegant entry animations, and beautiful typography.
  - Incorporate Material Symbols icons extensively for fast visual scanning and clear action associations.

---

## PRODUCT CATALOG CATEGORIES
1. Standard Portable Toilets
2. Executive Portable Toilets
3. VIP Portable Toilets
4. Presidential Luxury Mobile Restrooms
5. Mobile Shower Units
6. Accessible Toilets
7. Event Sanitation Packages
8. Corporate Site Sanitation Solutions
9. Waste Evacuation Services
10. Cleaning & Maintenance Services

*Each product page should contain a Hero Image, Product Description, Features, Technical Specifications, Recommended Use Cases, Photo/Video Gallery, Customer Reviews, and action buttons: "Request Quotation", "Schedule Consultation", "WhatsApp Contact", and "Call Sales".*

---

## QUOTATION & BOOKING WORKFLOW STATUSES
When a client requests a quotation, the request passes through the following lifecycle:
```
[Quotation Requested]
         ↓
  [Under Review]
         ↓
[Quotation Generated] (Admin sets price, duration, terms and uploads/attaches digital quotation)
         ↓
  [Quotation Sent]
         ↓
  [Client Accepted]
         ↓
  [Awaiting Payment] (Integrated with secure payment simulator/Paystack)
         ↓
  [Payment Confirmed]
         ↓
  [Booking Confirmed]
         ↓
  [Service Delivered] (Handled by staff updates)
         ↓
    [Completed]
```

---

## QUOTATION REQUEST SYSTEM DETAILS
The request form must collect:
1. **Customer Information**: Full Name, Company Name, Email, Phone Number.
2. **Service Type**: Rental or Purchase.
3. **Product Selection**: Product class, category, variant, and model.
4. **Quantity & Duration**: Numeric quantity, event dates,/duration (e.g., 1 Day, 3 Days, 1 Week, 2 Weeks, 1 Month, or Custom duration).
5. **Location Coordinates & Address**: Address text, Google Maps Pin/GPS coordinates coordinate integration.
6. **Event Information**: Event Type (Wedding, Seminar, Concert, Government, etc.), Number of Guests, and Event Date.
7. **Inclusion Requirements Checklist**: Cleaning support, Dedicated on-site attendant, Waste evacuation, Maintenance support.
8. **Special Instructions**: Multi-line customized details.

---

## ADMIN QUOTATION CENTER
The administrator view must support:
* Reviewing all incoming quotation requests.
* Editing and generating custom pricing quotes based on logs and metrics.
* Sending formal quotes to clients.
* Tracking acceptance and payments.
* Simulated generation, download, and sharing of professional quotation PDFs (including logos, terms & conditions, logistics, VAT, and company signatures).

---

## WHATSAPP & CONSULTATION SYSTEM
- Seamlessly pre-populate client quotation forms into a structured WhatsApp message string before triggering the WhatsApp intent.
- Support booking physical/virtual consultations, requesting callbacks, scheduling site inspections, and organizing product demonstrations.

---

## BUILD & PACKAGING COMPLIANCE (CRITICAL)
- Whenever any build, compilation, or file edits are made, you **MUST NOT** remove, disable, or tamper with the custom Gradle task configuration that generates and copies the final APK files.
- You **MUST** ensure that any updates or code changes are correctly compiled and fully included in:
  - `.build-outputs/app-debug.apk` (Must be a valid, real installable APK greater than 1 MB)
  - `APK_DOWNLOAD/app-debug.apk` (Must be a valid, real installable APK greater than 1 MB)
- Always run the compilation and verify the generated file sizes to ensure they are real and complete.

