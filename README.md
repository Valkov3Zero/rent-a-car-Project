# RentACar - Car Rental Management System

A full-featured car rental management system built with Spring Boot that allows users to register, browse available cars, make reservations, and manage payments.

## Features

### User Management
- User registration with validation for unique username, email, and phone number
- Authentication and authorization with Spring Security
- User profiles with personal information management
- Role-based access control (Customer and Admin roles)

### Car Management
- Browse available cars with filtering options
- Detailed car information (brand, model, year, price, etc.)
- Car availability tracking

### Rental Management
- Create and manage rental reservations
- Select pickup and drop-off locations
- View rental history
- Cancel or modify existing rentals

### Payment System
- Credit card management
- Secure payment processing
- Payment history

### Admin Features
- Manage car inventory (add, edit, delete cars)
- View all users and rentals
- Access comprehensive system statistics

## Technology Stack

- **Backend**: Spring Boot 3.4
- **Frontend**: Thymeleaf, HTML, CSS
- **Database**: MySQL (production), H2 (testing)
- **Security**: Spring Security
- **Build Tool**: Maven
- **Java Version**: 21

## Getting Started

### Prerequisites
- JDK 21
- Maven
- MySQL Server

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/rentacar.git
   cd rentacar
   ```

2. Configure the database:
   - Create a MySQL database named `rentacar`
   - Update `application.properties` with your database credentials:
     ```properties
     spring.datasource.url=jdbc:mysql://localhost:3306/rentacar
     spring.datasource.username=your_username
     spring.datasource.password=your_password
     ```

3. Build and run the application:
   ```bash
   ./mvnw clean install
   ./mvnw spring-boot:run
   ```

4. Access the application:
   - Open your browser and navigate to: `http://localhost:8080`

### Running Tests
```bash
./mvnw test
```

## Project Structure

```
src/
├── main/
│   ├── java/org/example/rentacar/
│   │   ├── car/           # Car management
│   │   ├── config/        # Application configuration
│   │   ├── creditCard/    # Credit card management
│   │   ├── email/         # Email notifications
│   │   ├── exception/     # Exception handling
│   │   ├── location/      # Location management
│   │   ├── payment/       # Payment processing
│   │   ├── rental/        # Rental management
│   │   ├── security/      # Security configuration
│   │   ├── user/          # User management
│   │   └── web/           # Web controllers and DTOs
│   └── resources/
│       ├── static/        # Static resources (CSS, JS)
│       ├── templates/     # Thymeleaf templates
│       └── application.properties
└── test/                  # Test classes
```

## User Guide

### Registration and Login
1. Navigate to the homepage
2. Click "Register" to create a new account
3. Fill in your details (username, email, phone, password)
4. Submit the form
5. Use your credentials to log in

### Browse and Rent Cars
1. Navigate to the "Cars" section
2. Browse available cars or use filters
3. Click on a car to view details
4. Select rental dates and locations
5. Confirm rental and proceed to payment

### Manage Your Profile
1. Click on your username to access your profile
2. View your rental history
3. Update your personal information
4. Manage your credit cards

### Admin Dashboard
1. Log in with admin credentials
2. Access the admin dashboard
3. Manage cars, users, and rentals
4. View system reports

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature-name`
3. Commit your changes: `git commit -m 'Add feature'`
4. Push to the branch: `git push origin feature-name`
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgements

- Spring Boot and Spring Security documentation
- Bootstrap for responsive design
- All contributors who have helped shape this project 
