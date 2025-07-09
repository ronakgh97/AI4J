# AI4J: Java Framework for Large Language Models

AI4J (AI for Java) is a lightweight and robust Java framework designed to simplify the integration and interaction with Large Language Models (LLMs). It provides a comprehensive set of tools for building intelligent applications, supporting both low-level, stateless interactions and high-level, stateful conversational experiences. AI4J is particularly well-suited for connecting to LLMs accessible via local HTTP endpoints (e.g., LM Studio, Ollama), offering flexibility, efficient memory management, and a focus on developer experience.

**Note:** This project is currently under active development, and continuous improvements are being made.

## Features

AI4J offers a rich set of features to empower your LLM-powered applications:

### Core LLM Interaction APIs

*   **Low-Level Chat API (`ChatServices_LowLevel`):** Provides direct, stateless interaction with LLM chat completion endpoints. Ideal for fine-grained control over requests and raw JSON payload manipulation.
*   **High-Level Chat API (`ChatServices`):** A higher-level abstraction that simplifies building stateful chatbots by automatically managing conversational context and integrating with memory managers and prompt templates.

### LLM Client & Connectivity

*   **Flexible LLM Client (`LLM_Client` & `DefaultHttpClient`):** An extensible interface for LLM communication, with a default HTTP client implementation supporting:
    *   Configurable base URLs and API endpoints.
    *   Customizable request timeouts.
    *   API key authentication for online LLM providers (e.g., Gemini, OpenAI).
    *   Option to use the base URL as the full endpoint URI for specific API structures.
*   **Streaming Support:** Efficiently handles LLM responses as they are generated, providing a smooth, real-time user experience.
    *   **Custom Stream Handling:** Utilizes `StreamHandler` for processing incoming content chunks.
    *   **Flexible Stream Response Parsing:** The `StreamResponseParser` interface (with `DefaultStreamResponseParser` and `WordStreamHandler`) allows for custom parsing of diverse LLM streaming response formats.
    *   **Configurable Streaming Speed:** Control the speed of streamed responses using `streamDelayMillis` for a smoother output.

### Memory Management

AI4J provides flexible and optimized memory management strategies to maintain conversational context:

*   **`MemoryManager` Interface:** Defines the contract for adding, retrieving, and clearing messages.
*   **`SlidingWindowMemory`:** Maintains a fixed-size window of the most recent messages, discarding older ones to keep context relevant.
*   **`OptimizedSlidingWindowMemory`:** An improved, `Deque`-based implementation of sliding window memory for better performance.
*   **`FileMemory`:** Persists chat history to a file, enabling conversations to be resumed across application sessions.
*   **`CachedFileMemory`:** An enhanced file-based memory solution with in-memory caching for improved performance and efficient access to chat logs.

### Message Structure

*   **`Message` & `MessageRole`:** Clearly defined classes for representing conversational messages, including roles like `SYSTEM`, `USER`, and `ASSISTANT`.

### Prompt Engineering

*   **`PromptTemplate`:** A powerful utility for defining and formatting prompts with dynamic variables. It allows for flexible structuring of system and user messages, enhancing prompt reusability and consistency.

### Model Parameter Configuration

*   **`ModelParams`:** Easily configure LLM parameters such as temperature, maximum tokens, and top-p. Includes robust input validation to ensure valid configurations.

### Robust Error Handling & Logging

*   **Custom Exceptions:** Implements a more elegant and specific exception handling mechanism with custom exceptions:
    *   `LLMServiceException`: General LLM service errors.
    *   `LLMParseException`: Errors during LLM response parsing.
    *   `LLMNetworkException`: Network communication errors.
    *   `Exception_Timeout`: Request timeout errors.
*   **`ExceptionHandler`:** Centralized exception handling.
*   **SLF4J Integration:** Uses SLF4J for flexible and configurable logging of errors and application events.

### Graphical User Interface (Demo)

*   **`SwingChatbot`:** A high-level, Java Swing-based graphical user interface demonstrating the framework's capabilities.
    *   **Enhanced UI/UX:** Features streamlined user message display, efficient input field clearing, and improved focus handling.
*   **`SettingsDialog`:** A dynamic settings dialog integrated with `SwingChatbot` for on-the-fly adjustment of LLM parameters (temperature, max tokens, top-p) and model selection.
    *   **Visual Consistency:** Achieves a cohesive look and feel with the main chatbot UI.
    *   **Improved Validation:** Robust input validation for parameters like 'Max Tokens'.
    *   **Intuitive Sliders:** Provides clear, decimal labels for Temperature and Top P sliders.
    *   **Accurate Feedback:** Ensures "LLM settings updated!" message only appears when settings are genuinely applied.
*   **`Welcome`:** Utility for generating welcome messages.

### Code Quality

*   **Enhanced Code Readability:** Extensive inline comments explain complex logic, class purposes, and method functionalities, making the codebase easier to understand and contribute to.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

*   **Java Development Kit (JDK) 24 or higher:** Ensure you have JDK 24 installed and configured.
*   **Maven:** This project uses Maven for dependency management and building.
*   **Local LLM Server (Optional but Recommended):** To run the examples, you'll need a local LLM server like [LM Studio](https://lmstudio.ai/) or [Ollama](https://ollama.ai/) running and serving a compatible model (e.g., `gemma-3-4b-it`) on `http://localhost:1234`. The `testEndpoints.java` file can be used to verify connectivity to `http://127.0.0.1:1234/v1/models`.

### Installation

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/your-username/AI4J.git # Replace with your actual repository URL
    cd AI4J
    ```
2.  **Build the project with Maven:**
    ```bash
    mvn clean install
    ```

## Usage Examples

The `src/main/java/com.aiforjava.examples` directory contains several example chatbots demonstrating different features.

### 1. Stateless Chatbot (`StatelessChatbot.java`)

Demonstrates basic, stateless interaction with the LLM using the low-level API. Each turn is independent.

```java
// Example snippet (simplified)
LLM_Client client = new DefaultHttpClient("http://localhost:1234", Duration.ofSeconds(90), null);
ChatServices_LowLevel llm = new ChatServices_LowLevel(client, "gemma-3-4b-it");
List<Message> messages = List.of(
    new Message(MessageRole.SYSTEM, "You are a helpful assistant."),
    new Message(MessageRole.USER, "Hello, how are you?")
);
llm.generateStream(messages, new ModelParams.Builder().build(), System.out::print);
```

### 2. Simple Chatbot (`SimpleChatbot.java`)

Illustrates the high-level `ChatServices` with `SlidingWindowMemory` to maintain a limited conversational history.

```java
// Example snippet (simplified)
MemoryManager memory = new SlidingWindowMemory(20);
PromptTemplate promptTemplate = new PromptTemplate("You are AI Assistant.", "{user_message}");
ChatServices chatService = new ChatServices(
    new ChatServices_LowLevel(new DefaultHttpClient("http://localhost:1234", Duration.ofSeconds(90), null), "gemma-3-4b-it"),
    memory,
    new ModelParams.Builder().build(),
    promptTemplate
);
chatService.chatStream("What is the capital of France?", System.out::print);
```

### 3. High-Level Chatbot (`HighLevelChatbot.java`)

Another demonstration of a high-level chatbot using `ChatServices` with `SlidingWindowMemory`.

### 4. Swing Chatbot (`SwingChatbot.java`)

A comprehensive example showcasing a Java Swing-based graphical user interface for interacting with the LLM, including dynamic settings.

```bash
mvn exec:java -Dexec.mainClass="com.aiforjava.demo.SwingChatbot"
```

### 5. File-Based Memory Chatbot (`ChatBot.java`)

Demonstrates using `FileMemory` to persist chat history across sessions. Chat logs are saved in the `chat_logs` directory.

### 6. Raw API Control (`RawChatbot.java`)

Showcases the lowest level of interaction, allowing direct sending of raw JSON requests to the LLM endpoint for maximum flexibility.

```java
// Example snippet (simplified)
ChatServices_LowLevel llm = new ChatServices_LowLevel(new DefaultHttpClient("http://localhost:1234", Duration.ofSeconds(90), null), "gemma-3-4b-it");
String rawRequest = "{ \"model\": \"gemma-3-4b-it\", \"messages\": [ { \"role\": \"user\", \"content\": \"Hello\" } ] }";
String rawResponse = llm.generateRaw("v1/chat/completions", rawRequest);
System.out.println(rawResponse);
```

## Dependencies

The project uses the following key dependencies, managed by Maven:

*   **Jackson (`jackson-databind`, `jackson-datatype-jsr310`):** For efficient JSON processing (serialization and deserialization).
*   **Apache HttpComponents Client 5 (`httpclient5`):** For making robust HTTP requests to LLM endpoints.
*   **SLF4J (`slf4j-api`):** Simple Logging Facade for Java, providing flexible and configurable logging.

## Recent Changes

*   **Optimized Memory Management:** Introduced `OptimizedSlidingWindowMemory` and `CachedFileMemory` for improved performance and flexibility in managing chat history.
*   **Configurable Streaming Output:** Added `streamDelayMillis` to `DefaultHttpClient` for controlling the smoothness of streamed LLM responses.
*   **Flexible Stream Response Parsing:** Implemented `StreamResponseParser` and `DefaultStreamResponseParser` to allow for custom parsing of LLM streaming formats.
*   **Improved Model Parameter Validation:** Added input validation to `ModelParams.Builder` for more robust configuration.
*   **Enhanced Error Logging:** Switched `ExceptionHandler` to use SLF4J for more flexible and configurable error logging.
*   **Refactored HTTP Client:** Streamlined `DefaultHttpClient` by removing redundant methods and improving exception handling for streaming.
*   **API Key Support:** The `DefaultHttpClient` now supports an `apiKey` for authenticating with online LLM providers.
*   **Prompt Templating:** The high-level `ChatServices` now uses a `PromptTemplate` class to provide more flexibility in formatting system and user messages.
*   **Improved Examples:** The examples have been updated to reflect the latest API changes and demonstrate new memory management options.
*   **Documentation Updates:** The `README.md` has been updated to provide more accurate and comprehensive information.
*   **Enhanced SwingChatbot UI/UX:**
    *   Streamlined user message display and input field clearing for a smoother chat experience.
    *   Improved focus handling in the input field after interacting with the settings dialog.
*   **Refined Settings Dialog:**
    *   Achieved visual consistency with the main chatbot UI's color scheme.
    *   Implemented robust validation for 'Max Tokens'.
    *   Provided more intuitive decimal labels (0.0-1.0) for Temperature and Top P sliders.
    *   Ensured "LLM settings updated!" message only appears when settings are actually applied.

## Contributing

AI4J is an open-source project, and contributions are highly encouraged! Whether you're fixing bugs, adding new features, improving documentation, or suggesting enhancements, your input is valuable. Please feel free to open issues or submit pull requests.

## License

This project is licensed under the [MIT License](LICENSE).

## Open Source

AI4J is an open-source project, and we welcome community involvement. We believe in collaborative development to build a robust and flexible framework for Java-based AI applications.