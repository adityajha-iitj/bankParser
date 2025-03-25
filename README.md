# PDF Reader API Using LLM

## Overview
The **PDF Reader API** extracts relevant information from bank statement PDFs. It supports file uploads in form-data format and can optionally generate a password if additional parameters are provided.

## Features
- Parses bank statement PDFs to extract details like name, email, account type, and opening and closing balances.
- Generates a password when `firstName` and `dateofBirth` are provided.
- Returns data in JSON format.

## Setup & Execution

### Prerequisites
Ensure you have the following installed:
- Java 17
- Maven
- Docker (for containerized execution)

### Installation & Running Locally
1. Clone the repository:
   ```bash
   git clone https://github.com/your-repo/pdf-parser-api.git
   cd pdf-parser-api
   ```
2. Build the project:
   ```bash
   mvn clean package -DskipTests
   ```
3. Run the application:
   ```bash
   java -jar target/pdf-parser-api-0.0.1-SNAPSHOT.jar
   ```
   The API will be available at `http://localhost:8080`.

### Running with Docker
1. Build the Docker image:
   ```bash
   docker build -t api-pdf-parser .
   ```
2. Run the container:
   ```bash
   docker run -p 8001:8080 -d -e GEMINI_API_KEY=your_api_key api-pdf-parser
   ```
3. Check running containers:
   ```bash
   docker ps
   ```
4. Access the API at `http://localhost:8001`.

## API Usage

### Endpoint: Parse Bank Statement
- **URL:** `POST /api/bank-statement/parse`
- **Content Type:** `multipart/form-data`
- **Request Parameters:**
  | Parameter    | Type    | Required | Description |
  |-------------|---------|----------|-------------|
  | file        | File    | Yes      | PDF file to be parsed. |
  | firstName   | String  | No       | User's first name (optional). |
  | dateofBirth | String  | No       | Date of birth in `YYYY-MM-DD` format (optional). |

- **Example Request (form-data format):**
  ```
  file: CASA_STATEMENT.pdf
  firstName: aadi
  dateofBirth: 2006-02-12
  ```

- **Example Response:**
  ```json
  {
      "name": "MR. RAMACHANTRAN",
      "email": "ramachantran_k@securebank.com",
      "openingBalance": 28744.82,
      "closingBalance": 22620.82,
      "accountType": "Savings",
      "generatedPassword": "20060212aadisb"
  }
  ```

  If `firstName` and `dateofBirth` are not provided, `generatedPassword` will be shown null in the response.

## Deployment
The API is deployed on Render and can be accessed at:
**[https://pdf-parser-api-djg2.onrender.com/api/bank-statement/parse](https://pdf-parser-api-djg2.onrender.com/api/bank-statement/parse)**

## License
This project is licensed under the MIT License.

## Author
Developed by Aditya Jha.
