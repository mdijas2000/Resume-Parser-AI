***

# 🧠 AI-Powered Resume Parser (Microservices)

A robust, full-stack Resume Parsing application built with **Java Spring Boot**, **ReactJS**, and **Microservices architecture**. This project leverages the **Google Gemini API** to achieve near-perfect data extraction from unstructured resumes, replacing fragile regex heuristics with a dynamic Generative AI pipeline.

## ✨ Key Features

* **LLM-Powered Parsing:** Uses Google Gemini to dynamically extract structured data (Education, Experience, Projects, Skills) from unstructured PDFs.
* **Strict Schema Enforcement:** Employs prompt-engineering to guarantee strict JSON output that perfectly maps to Java DTOs using Jackson.
* **Resilient Circuit Breaker (Fallback Strategy):** If the LLM API rate-limits or fails, the system automatically falls back to a custom-built Regex/Heuristic parser, ensuring zero downtime.
* **Deterministic Experience Calculation:** Prevents LLM math hallucinations by instructing the AI to normalize dates into strict `YYYY-MM` formats, while delegating the actual total experience calculation to precise Java logic.
* **AI Candidate Summarization:** Automatically generates a concise 2-sentence professional summary of the candidate's core strengths for recruiters.
* **Microservices Architecture:** Built with API Gateway, Discovery Server (Eureka), and independent modular services.
* **Apache Tika Integration:** Reliably extracts raw text from PDF and DOCX files before handing it to the AI.

## 🏗️ Architecture Flow

1. **Upload:** User uploads a PDF via the React UI.
2. **Text Extraction:** The `Parser Service` uses Apache Tika to strip raw text from the document.
3. **AI Evaluation:** The raw text is sent to the Gemini API with a heavily engineered JSON schema prompt.
4. **Data Normalization:** Gemini normalizes dates and maps unstructured text into strict JSON.
5. **Java Post-Processing:** Jackson deserializes the JSON into Java Objects, and Java performs deterministic chronological math to calculate exact Total Experience.
6. **Persistence:** The structured candidate profile is saved to PostgreSQL.

## 💻 Tech Stack

**Backend:** Java 17, Spring Boot, Spring Cloud (Eureka, API Gateway), Apache Tika, REST APIs, Jackson 
**Frontend:** ReactJS, HTML, CSS  
**Database:** PostgreSQL  
**AI Integration:** Google Gemini REST API  

## 🚀 Getting Started

### Prerequisites
* Java 17+
* Node.js & npm
* PostgreSQL running locally
* A free [Google AI Studio API Key](https://aistudio.google.com/)

### Installation & Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/Resume-Parser-AI.git
   cd Resume-Parser-AI
   ```

2. **Configure the AI API Key**
   Navigate to the parser-service configuration file:
   `parser-service/src/main/resources/application.yml`
   Add your Gemini API key:
   ```yaml
   gemini:
     api:
       key: YOUR_GEMINI_API_KEY_HERE
   ```

3. **Start the Microservices**
   Boot up the services in the following order:
   * Discovery Server (Eureka)
   * API Gateway
   * Parser Service
   * (Any other dependent services)

4. **Start the Frontend**
   ```bash
   cd frontend
   npm install
   npm start
   ```

## 🛡️ Fallback Strategy (Circuit Breaker)
This application demonstrates resilient system design. If the Gemini API throws a `429 Too Many Requests` or `500 Internal Server Error`, the Java controller catches the exception and routes the raw text through the legacy Regex heuristic parser, returning a partially populated profile rather than crashing the request.

---
*Developed by Mohamed Ijas I*

***
