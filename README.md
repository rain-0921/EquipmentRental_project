# Campus Equipment Rental & Billing System

A Java Swing desktop application implementing a smart equipment rental and billing system for campus use. This project is developed for CCP6224 coursework.

## Features

- **3 User Roles**: Student, Final Year Student, Staff
- **Equipment Management**: 3 categories (Electronics, Media, Laboratory)
- **Equipment Status Tracking**: AVAILABLE / RENTED / DAMAGED
- **Self-Service Rental & Return**: Students can rent and submit return requests
- **Staff Approval Workflow**: Staff approve returns and generate bills
- **Discount System**:
  - Student: 0% discount
  - Final Year Student: 15% discount
  - Staff: 20% discount
- **Damage Penalties**:
  - LIGHT: RM 10
  - MODERATE: RM 100
  - HEAVY: RM 1000
- **Late Return Fees** (per day):
  - Electronics: RM 20/day
  - Media: RM 10/day
  - Laboratory: RM 30/day
- **CRUD Operations**: Equipment and User management for Staff
- **MySQL Database**: Persistent data storage using XAMPP

## Database Setup (MySQL + XAMPP)

### Prerequisites
- XAMPP with MySQL enabled
- MySQL Connector/J library

### Step 1: Start XAMPP MySQL
1. Open XAMPP Control Panel
2. Start **MySQL** service
3. Click **Admin** to open phpMyAdmin

### Step 2: Import Database
1. Open phpMyAdmin (http://localhost/phpmyadmin)
2. Click **Import** tab
3. Select file: `docs/database_setup.sql`
4. Click **Go**

This will create:
- Database: `campus_rental`
- Tables: `users`, `equipment`, `rentals`, `bills`, `counters`
- Demo data: 3 users, 9 equipment items

### Step 3: Add MySQL Connector to Project

Download MySQL Connector/J from: https://dev.mysql.com/downloads/connector/j/

Extract the JAR file (e.g., `mysql-connector-j-8.0.33.jar`) to the project root or `lib` folder.

## Project Structure

```
src/com/campusrental/
├── App.java                 # Main entry point
├── model/                   # Domain models
├── service/                 # Business logic
├── repo/                    # Data access layer (MySQL)
│   ├── DatabaseManager.java # MySQL connection
│   ├── UserRepository.java
│   ├── EquipmentRepository.java
│   ├── RentalRepository.java
│   └── BillRepository.java
└── gui/                    # Swing UI
```

## How to Run

### Prerequisites
- Java Development Kit (JDK) 11 or higher
- XAMPP with MySQL running
- MySQL Connector/J library

### Compilation

```bash
cd d:/jiahui/Downloads/ooadpro
mkdir out
javac -cp ".;mysql-connector-j-8.0.33.jar" -d out -sourcepath src @file_list.txt
```

Or compile all files:
```bash
cd src
javac -d ../out com/campusrental/**/*.java
cd ..
```

### Running

```bash
java -cp "out;mysql-connector-j-8.0.33.jar" com.campusrental.App
```

**Note**: Make sure `mysql-connector-j-8.x.x.jar` is in the same directory or in the classpath.

## Demo Accounts

| User ID    | Password | Role                | Discount |
|------------|----------|---------------------|----------|
| student001 | 123      | Student             | 0%       |
| fyp001     | 123      | Final Year Student  | 15%      |
| staff001   | 123      | Staff               | 20%      |

## User Guide

### For Students / Final Year Students

1. **Login** with your credentials
2. **Browse Equipment** in the Equipment Catalog tab
3. **Rent Equipment** by selecting equipment and rental duration
4. **View My Rentals** to track your active rentals
5. **Submit Return** when returning equipment (select damage severity)
6. **View My Bills** to see your generated bills

### For Staff

1. **Login** with staff credentials
2. Access all student functions plus:
3. **Pending Approvals**: Review and approve/reject return requests (can modify damage severity)
4. **Equipment Admin**: Add, edit, delete equipment; mark damaged equipment as repaired
5. **User Admin**: Add, edit, delete users
6. **Billing History**: View all generated bills

## Bill Calculation

```
Net Payable = Subtotal - Discount + Late Penalty + Damage Penalty

Where:
- Subtotal = Daily Rate x Rental Days
- Discount = Discount Rate x Subtotal
- Late Penalty = Late Fee per Day x Late Days
- Damage Penalty = Fixed amount based on severity (LIGHT=10, MODERATE=100, HEAVY=1000, NONE=0)
```

## UML Diagrams

UML diagrams are available in the `docs/` folder:

- `usecase.puml` - Use Case Diagram
- `class.puml` - Class Diagram
- `seq_login.puml` - Login Sequence Diagram
- `seq_rent.puml` - Rent Equipment Sequence Diagram
- `seq_return_approval.puml` - Return Approval Sequence Diagram

To view PlantUML diagrams, you can use:
- [PlantUML Online Editor](https://www.plantuml.com/plantuml/uml/)
- VS Code with PlantUML extension
- IntelliJ IDEA with PlantUML plugin

## Extension Points

The system is designed for easy extension following the Open-Closed Principle:

- **New Equipment Type**: Add new Equipment subclass (e.g., `ToolsEquipment`)
- **New Pricing Policy**: Implement new `PricingPolicy` (e.g., `AlumniDiscountPricing`)
- **New Damage Level**: Add enum value to `DamageSeverity` and update `DamagePenalty`
- **New Rental Status**: Add enum value to `RentalStatus`

## Troubleshooting

### "Database connection failed"
- Make sure XAMPP MySQL is running
- Check if the port is 3306 (default)
- Verify the database `campus_rental` exists

### "MySQL JDBC Driver not found"
- Download MySQL Connector/J
- Add the JAR file to classpath

### Data not persisting
- Check MySQL is running
- Verify database tables exist
- Check for SQL errors in console

## License

This project is developed for educational purposes as part of CCP6224 coursework.
