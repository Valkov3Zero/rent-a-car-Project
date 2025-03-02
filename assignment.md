# Car Rental System Assignment

## Overview
This document outlines the requirements for developing a car rental system where users can register, log in, browse available cars, and rent them. The system will have two main user roles: Customer and Admin, with different permissions and capabilities.

## System Requirements

### User Management
1. **Registration**
   - New users should be able to create an account with basic information (name, email, password, etc.)
   - Email verification should be implemented
   - Default role for new users is "Customer"

2. **Authentication**
   - Users should be able to log in with email and password
   - Implement password hashing for security
   - Session management with appropriate timeout

3. **User Profiles**
   - Users should be able to view and edit their profile information
   - Profile should display rental history

4. **User Roles and Permissions**
   - **Customer Role**:
     - Browse cars
     - View car details
     - Rent available cars
     - View personal rental history
     - Cancel own rentals (if not yet started)
   - **Admin Role**:
     - All Customer permissions
     - Add new cars to the system
     - Remove cars from the system
     - Update car information
     - View all rental orders in the system
     - Change user roles (Customer ↔ Admin)
     - Activate/deactivate user accounts

### Car Management
1. **Car Properties**
   - Brand
   - Model
   - Year
   - License plate (unique identifier)
   - Status (Available, Rented, Maintenance)
   - Image
   - Price per day
   - Other relevant specifications

2. **Car Listing**
   - Display all cars with pagination
   - Filtering options (by brand, model, year, price range, availability)
   - Sorting options (by price, brand, etc.)

3. **Car Details**
   - Detailed view with all car information
   - Availability calendar
   - Rental button (if available)

### Rental System
1. **Rental Process**
   - Select rental dates (start and end)
   - Calculate total price based on number of days
   - Confirm rental details
   - Process payment (mock implementation is acceptable)
   - Generate rental confirmation

2. **Rental Management**
   - View active rentals
   - View rental history
   - Cancel upcoming rentals (if permitted)
   - Extend current rentals (if car is available for extension period)

3. **Admin Rental Overview**
   - View all current and past rentals
   - Filter rentals by user, car, date, status
   - Generate reports (optional)

### Location Management
1. **Multiple Pickup/Return Locations**
   - Maintain a list of locations
   - Allow selection of different pickup and return locations

### Payment System
1. **Payment Processing**
   - Mock payment system with credit card information
   - Payment history
   - Invoice generation

## Technical Requirements

### Backend
1. **Spring Boot Application**
   - RESTful API design
   - Proper error handling and status codes
   - Data validation
   - Secure endpoints with proper authorization

2. **Database**
   - MySQL database
   - Proper entity relationships
   - Database migration strategy

3. **Security**
   - Spring Security implementation
   - JWT authentication
   - Role-based access control
   - Input validation and sanitization
   - Protection against common vulnerabilities (XSS, CSRF, SQL Injection)

### Frontend
1. **Thymeleaf Templates**
   - Responsive design
   - User-friendly interface
   - Form validation
   - Error handling and user feedback

2. **User Experience**
   - Intuitive navigation
   - Consistent design language
   - Accessibility considerations
   - Mobile responsiveness

## Implementation Phases

### Phase 1: Core Functionality
1. User registration and authentication
2. Basic car management (CRUD operations)
3. Simple rental process
4. Admin dashboard for car management

### Phase 2: Enhanced Features
1. Advanced search and filtering
2. Rental management (extensions, cancellations)
3. Admin user management
4. Payment processing

### Phase 3: Refinement and Additional Features
1. Email notifications
2. Reporting system
3. UI/UX improvements
4. Performance optimization

## Testing Requirements
1. Unit tests for service layer
2. Integration tests for repositories and controllers
3. End-to-end testing for critical user journeys
4. Security testing

## Deliverables
1. Full source code in a Git repository
2. Database schema and migration scripts
3. Documentation:
   - API documentation
   - Setup instructions
   - User manual
4. Test reports

## Evaluation Criteria
1. Functionality and feature completeness
2. Code quality and organization
3. Security implementation
4. User experience
5. Test coverage
6. Documentation quality

## Timeline
- Phase 1: 2 weeks
- Phase 2: 2 weeks
- Phase 3: 1 week
- Testing and bug fixing: 1 week

Total project duration: 6 weeks 