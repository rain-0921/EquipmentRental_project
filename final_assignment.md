# Final Assignment
## CCP6224 - Object Oriented Analysis and Design

**Total Marks:** 40%
**Due Date:** 8 July 2026, 11:59pm (firm deadline; no extensions)

---

## Instructions

### 1. Group Work & Evaluation

- This is a group assignment (groups of 3 or 4 students from the same tutorial section).
- There must be work division among team members. You are advised to divide the project by modules first, then each team member responsible for a particular module design. Have a team discussion on how to integrate all the modules and create a grand design that can work seamlessly. Once this is finalized, then each team member can focus on the module and provide the implementation and integrate them.
- During evaluation, you must be able to defend questions asked with regards to your module as well as overall questions. Do not give answers like "we do this together." It is not acceptable at this level of your degree path. This project is an Object-Oriented Design project where you must be able to show your skill in task division/modularization and then integration of the modules while still making the design intuitive and implementation 100% represent the design done. You also need to apply design principles taught in the course to make the work maintainable, expandable in the future.
- **Important:** Evaluation will be done individually. Every group member must be able to explain and defend the design and implementation in a follow-up Q&A session.

### 2. Plagiarism

- Any instances of plagiarism will result in a zero mark without prior notice. Ensure all work is your own and any external help is properly acknowledged.

### 3. Submission Format

- Submit one zip folder with the naming convention: `StudentNames.zip`
- Submission instruction will be given by your tutor. If you don't follow the submission instructions, you will get zero marks.
- The folder must include all source code files, UML diagrams (use case, class, and sequence diagrams).

### 4. Mandatory Interview Q&A Session

- Each student must attend a physical interview session between **10 July 2026** till **26 July 2026** (details to be announced by your tutor).
- In this session, you will be asked questions about your design principles and decisions, design pattern, and how your solution supports future enhancements. Other related questions can also be asked.
- For the interview, prepare yourself in the flow of the rubric given below (read the marking rubrics so that you know how to score your marks!).
- Failure to attend the Q&A session will result in a zero mark for the assignment even if you submitted the assignment.
- Implementation should only use the **Java Swing Framework**.

---

## Case Study: Smart Equipment Rental & Billing System

**Client:** Campus Facilities & Student Services Department

**Project Overview:** The university plans to introduce a standalone GUI application to manage the rental of equipment such as laptops, cameras, projectors, and laboratory tools. The system must handle different rental item categories, rental durations, pricing policies, and penalty rules, while being designed for future extensibility.

### Client Requirements

**Equipment Management:**
- Maintain different equipment categories such as Electronics, Media Equipment, and Laboratory Equipment.
- Each equipment item should have attributes such as equipment ID, name, category, daily rental rate, and availability status.
- Different equipment categories may apply different rental pricing or penalty rules.

**Rental & Return Management:**
- Allow users (students or staff) to rent and return equipment.
- Rental duration affects the total rental fee.
- Apply penalties for late returns or damaged equipment.
- Some users (e.g., staff or final-year students) may be eligible for special pricing or discounts.

**Billing & Payment Calculation:**
- Calculate rental charges based on equipment type and rental duration.
- Apply discounts, penalties, or promotional pricing where applicable.
- Display a detailed rental bill showing base rental fee, discounts, penalties, and net payable amount.

**Future-Proof Design:**
- The system must allow easy addition of new equipment categories, pricing strategies, or penalty rules.
- Apply good object-oriented design principles and design patterns to support scalability and maintainability.

---

## Assignment Questions

> Clearly state and justify any assumptions you make and inform the interviewer during the interview session.

### 1. UML Diagrams

- Develop a **Use Case Diagram** illustrating interactions between users and the system.
- Develop a complete **Class Diagram** demonstrating inheritance, abstraction, aggregation/composition, and polymorphism.
- Develop **Sequence Diagram(s)** for key operations such as equipment rental and billing calculation.

### 2. Design Pattern Application

Select **ONE** design pattern from the following list: Composite, Adapter, Bridge, Façade, Iterator, Observer, Builder, Prototype, or Singleton.

Apply the selected pattern in your system design, reflect it clearly in your class diagram, and justify how it improves flexibility and future enhancement.

### 3. Java Swing GUI Implementation

Implement the Smart Equipment Rental System using Java Swing. The application must include proper event handling, input validation, and accurate rental fee computation. The implementation must closely follow the UML design.

### 4. Preparation for Q&A Interview

Prepare to answer questions on the use of object-oriented principles, your design pattern choice, module integration, and how the system supports future extensions.

---

## Marking Rubrics

### Feature Fulfillment (Weight: 20%)

Features required:
- Equipment and rental management
- Rental fee, discount, and penalty calculation
- Detailed billing output
- User-friendly Java Swing interface

| Descriptor | Marks |
|---|---|
| All 4 features fulfilled | 20 |
| 3 features fulfilled | 15 |
| 2 features fulfilled | 10 |
| 1 feature fulfilled | 5 |
| None fulfilled | 0 |

### UML Diagram Fulfillment (Weight: 15%)

| Descriptor | Marks |
|---|---|
| Use Case Diagram correctly represents system interactions | 5 |
| Class Diagram accurately depicts system's structure and relationships | 5 |
| Sequence Diagrams clearly illustrate object interactions for key processes | 5 |

### Design Pattern Usage (Weight: 10%)

| Descriptor | Marks |
|---|---|
| Design pattern is correctly applied and well-justified | 10 |
| Design pattern usage shows some missing parts or weak justification | 5 |
| No design pattern used or incorrect application | 0 |

### Future-proof Application Design (Weight: 15%)

| Descriptor | Marks |
|---|---|
| Design demonstrates strong object-oriented principles and scalability (e.g., proper use of design patterns, modularity, separation of concerns) | 15 |
| Minor issues in ensuring scalability or adherence to OO principles | 10 |
| Major issues in design | 5 |
| No scalable design provided | 0 |

### Q&A with Interviewer (Weight: 40%)

| Descriptor | Marks |
|---|---|
| Answers are correct, complete, and elaborated with clear explanations and appropriate terminology | 40 |
| Answers are correct but somewhat brief or lacking depth | 30 |
| Answers are generally correct but contain significant omissions or errors | 20 |
| Answers are vague or incorrect | 0 |
