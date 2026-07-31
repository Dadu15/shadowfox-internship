# Library Management System (Java Swing & JDBC)

This is a Library Management System implemented in Java using Java Swing for the GUI and JDBC for database connectivity. The project is designed to manage library operations like adding users, adding books, issuing books to users, and accepting books back into the library.

## Features

- **User Management**:
  - Add new users to the library system.
  - View and edit user information.

- **Book Management**:
  - Add new books to the library inventory.
  - View and edit book details.

- **Book Issuing and Return**:
  - Issue books to registered users.
  - Accept returned books and update the inventory.

## Technologies Used

- Java Swing: For the graphical user interface (GUI) components.
- JDBC (Java Database Connectivity): For connecting to the SQLite database.
- SQLite: As the database management system to store user and book data.

## Getting Started

To get started with this project, follow these steps:

1. Clone the repository to your local machine:

   ```bash
   git clone https://github.com/Aayush-Joshi-01/Library-Management-System.git

2. Open the project in your preferred Java IDE.

3. Configure the database connection by editing the `config.properties` file with your database credentials:

   ```properties
   db.url=jdbc:sqlite:library.db
   db.username=your_username
   db.password=your_password
   ```

4. Build and run the project from your IDE.

## Usage

1. Launch the application.

2. Use the GUI to perform various library management tasks like adding users, adding books, issuing books, and accepting book returns.

## Database Schema

The project uses an SQLite database with the schema very Similar to this:

- `users` table:
  - `user_id` (Primary Key)
  - `username`
  - `full_name`
  - `email`
  - ...

- `books` table:
  - `book_id` (Primary Key)
  - `title`
  - `author`
  - `isbn`
  - ...

- `book_transactions` table:
  - `transaction_id` (Primary Key)
  - `user_id` (Foreign Key)
  - `book_id` (Foreign Key)
  - `issue_date`
  - `return_date`

## Contributing

Feel free to contribute to this project by opening issues or pull requests. Your feedback and contributions are highly appreciated.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.
