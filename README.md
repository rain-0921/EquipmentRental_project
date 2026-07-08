# Campus Equipment Rental & Billing System
## Quick Setup

1. **Start MySQL** via XAMPP Control Panel
2. **Import database**: Open phpMyAdmin → Import `docs/database_setup.sql`
3. **Add MySQL Connector**: Copy `mysql-connector-j-9.7.0.jar` to project root

## Build & Run

```bash
# Compile
javac -cp ".;mysql-connector-j-9.7.0.jar" -d out -sourcepath src @file_list.txt

# Run
java -cp "out;mysql-connector-j-9.7.0.jar" com.campusrental.App
```

## Demo Accounts

| User ID    | Password | Role                | Discount |
|------------|----------|---------------------|----------|
| student001 | 123      | Student             | 0%       |
| fyp001     | 123      | Final Year Student  | 15%      |
| staff001   | 123      | Staff               | 20%      |

## Features

- **3 User Roles**: Student, Final Year Student, Staff
- **Equipment Management**: Electronics, Media, Laboratory categories
- **Rental Workflow**: Students rent → Submit return → Staff approves → Bill generated
- **Discount System**: 0% / 15% / 20% by role
- **Penalties**: Damage (RM 10/100/1000) + Late fees (RM 10-30/day)
- **CRUD Operations**: Equipment and User management (Staff only)

## Bill Calculation

```
Net Payable = (Daily Rate × Days) - Discount + Late Fee + Damage Fee
```

Damage: LIGHT=RM10, MODERATE=RM100, HEAVY=RM1000 | Late: RM10-30/day

## UML Diagrams

Located in `docs/`

## Troubleshooting

- **DB connection failed**: Ensure XAMPP MySQL is running on port 3306
- **JDBC driver not found**: Add `mysql-connector-j-9.7.0.jar` to classpath
.
