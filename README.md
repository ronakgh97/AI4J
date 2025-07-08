# AI4J: Java Framework for Large Language Models

AI4J (AI for Java) is a lightweight Java framework developed by Ronak, designed to simplify interaction with Large Language Models (LLMs), particularly those accessible via local HTTP endpoints (e.g., LM Studio, Ollama). It provides both low-level and high-level APIs for chat-based interactions, along with flexible memory management options to maintain conversational context.

**Note:** This project is currently under active development, and many improvements are planned.

## Features

*   **Flexible LLM Integration:** Connects to LLMs via HTTP, supporting custom endpoints.
*   **Low-Level Chat API:** Direct interaction with LLM chat completions for stateless requests.
*   **High-Level Chat API:** Manages conversational state and memory, simplifying chatbot development.
*   **Memory Management:**
    *   **Sliding Window Memory:** Keeps a configurable number of recent messages in context.
    *   **File-Based Memory:** Persists chat history to a file.
*   **Streaming Support:** Efficiently handles LLM responses as they are generated.
*   **Model Parameter Configuration:** Easily set parameters like temperature, max tokens, and top-p.
*   **Configurable GUI Settings:** The `SwingChatbot` now includes a settings dialog to dynamically adjust LLM parameters (temperature, max tokens, top-p) and select different models from a dropdown, enhancing user control and experimentation.
*   **Prompt Templating:** Utilize `PromptTemplate` to define and format prompts with dynamic variables, enhancing flexibility and reusability.
*   **Robust Error Handling:** Implements a more elegant and specific exception handling mechanism with custom exceptions (`LLMServiceException`, `LLMParseException`, `LLMNetworkException`) for clearer error identification and management.
*   **Enhanced Code Readability:** Extensive inline comments have been added to explain complex logic, class purposes, and method functionalities, making the codebase easier to understand and contribute to.

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

### Running Examples

The `src/main/java/com/aiforjava.examples` directory contains several example chatbots demonstrating different features.

#### 1. `StatelessChatbot.java` (Low-Level, No Memory)

This example shows a basic, stateless interaction with the LLM. Each turn is independent.

```bash
mvn exec:java -Dexec.mainClass="com.aiforjava.examples.StatelessChatbot"
```

#### 2. `SimpleChatbot.java` (High-Level, Sliding Window Memory)

This example uses the high-level `ChatServices` with `SlidingWindowMemory` to maintain a limited conversational history.

```bash
mvn exec:java -Dexec.mainClass="com.aiforjava.examples.SimpleChatbot"
```

#### 3. `HighLevelChatbot.java` (High-Level, Sliding Window Memory)

This example demonstrates a high-level chatbot using `ChatServices` with `SlidingWindowMemory`.

```bash
mvn exec:java -Dexec.mainClass="com.aiforjava.examples.HighLevelChatbot"
```

#### 4. `SwingChatbot.java` (High-Level, Swing GUI)

This example demonstrates a high-level chatbot with a Java Swing-based graphical user interface.

```bash
mvn exec:java -Dexec.mainClass="com.aiforjava.demo.SwingChatbot"
```

#### 5. `ChatBot.java` (Low-Level, File-Based Memory)

This example demonstrates using `FileMemory` to persist chat history across sessions. The chat log will be saved in the `chat_logs` directory relative to your project's execution.

```bash
mvn exec:java -Dexec.mainClass="com.aiforjava.examples.ChatBot"
```

#### 5. `RawChatbot.java` (Low-Level, Raw JSON Control)

This example showcases the lowest level of interaction, allowing you to send raw JSON requests directly to the LLM endpoint. This provides maximum flexibility for custom API interactions.

```bash
mvn exec:java -Dexec.mainClass="com.aiforjava.examples.RawChatbot"
```

## Usage

### LLM Client

The `LLM_Client` interface (`com.aiforjava.llm.LLM_Client`) defines how to send requests to the LLM. `DefaultHttpClient` provides a basic HTTP client implementation.

### Chat Services

*   **`ChatServices_LowLevel` (`com.aiforjava.llm.Chat.LowLevel.ChatServices_LowLevel`)**:
    Provides direct methods for generating LLM responses. It's stateless and requires you to manage the message list for each request.

    ```java
    // Example (from StatelessChatbot.java)
    LLM_Client client = new DefaultHttpClient("http://localhost:1234", Duration.ofSeconds(90));
    ChatServices_LowLevel llm = new ChatServices_LowLevel(client, "gemma-3-4b-it");

    List<Message> messages = List.of(
            new Message(MessageRole.SYSTEM, "You are a helpful assistant."),
            new Message(MessageRole.USER, "Hello, how are you?")
    );

    llm.generateStream(messages, params, System.out::print);
    ```

    **Raw API Access:**
    For ultimate control, `ChatServices_LowLevel` also provides methods to send raw JSON requests directly to the LLM endpoint. This is useful for advanced use cases or when interacting with LLMs that have non-standard API structures.

    *   `generateRaw(String endpoint, String requestJson)`: Sends a raw JSON request and returns the raw JSON response.
    *   `generateStreamRaw(String endpoint, String requestJson, StreamHandler handler)`: Sends a raw JSON request for streaming responses.

    ```java
    // Example of raw API usage (from RawChatbot.java)
    String rawRequest = "{ \"model\": \"gemma-3-4b-it\", \"messages\": [ { \"role\": \"user\", \"content\": \"Hello\" } ] }";
    String rawResponse = llm.generateRaw("v1/chat/completions", rawRequest);
    System.out.println(rawResponse);
    ```

*   **`ChatServices` (`com.aiforjava.llm.Chat.HighLevel.ChatServices`)**:
    A higher-level abstraction that integrates with `MemoryManager` to handle conversational context automatically.

    ```java
    // Example (from SimpleChatbot.java)
    MemoryManager memory = new SlidingWindowMemory(20); // Or new FileMemory(...)
    ChatServices chatService = new ChatServices(
            llm,
            memory,
            params,
            "You are AI Assistant. Keep responses concise (1-2 sentences max)."
    );

    chatService.chatStream("What is the capital of France?", System.out::print);
    ```

### Memory Management

The `MemoryManager` interface (`com.aiforjava.memory.MemoryManager`) defines methods for adding, retrieving, and clearing messages.

*   **`SlidingWindowMemory` (`com.aiforjava.memory.SlidingWindowMemory`)**:
    Maintains a fixed-size window of the most recent messages. When the window is full, the oldest message is removed.

    ```java
    MemoryManager memory = new SlidingWindowMemory(10); // Keep last 10 messages
    ```

*   **`FileMemory` (`com.aiforjava.memory.ChatLogs.FileMemory`)**:
    Persists chat messages to a file, allowing conversations to be resumed.

    ```java
    MemoryManager memory = new FileMemory("C:\path\to\your\chatlogs");
    ```

### Message Structure

Messages are represented by the `Message` class (`com.aiforjava.message.Message`) and have a `MessageRole` (`com.aiforjava.message.MessageRole`) (e.g., `SYSTEM`, `USER`, `ASSISTANT`).

## Project Structure

```
AI4J/
├───.idea/
├───.mvn/
├───src/
│   ├───main/
│   │   ├───java/
│   │   │   └───com/
│   │   │       └───aiforjava/
│   │   │           ├───testEndpoints.java          // Utility to test LLM server connectivity
│   │   │           ├───examples/                   // Example chatbot implementations
│   │   │           │   ├───ChatBot.java
│   │   │           │   ├───SimpleChatbot.java
│   │   │           │   └───StatelessChatbot.java
│   │   │           ├───exception/                  // Custom exception handling
│   │   │           │   ├───Exception_Timeout.java
│   │   │           │   └───ExceptionHandler.java
│   │   │           ├───llm/                        // LLM client and related classes
│   │   │           │   ├───DefaultHttpClient.java
│   │   │           │   ├───LLM_Client.java         // Interface for LLM client
│   │   │           │   ├───ModelParams.java        // LLM model parameters
│   │   │           │   ├───StreamHandler.java      // Interface for handling streaming responses
│   │   │           │   ├───Chat/
│   │   │           │   │   ├───HighLevel/
│   │   │           │   │   │   └───ChatServices.java // High-level chat service with memory
│   │   │           │   │   ├───Logs/               // Directory for file-based chat logs
│   │   │           │   │   └───LowLevel/
│   │   │           │   │       └───ChatServices_LowLevel.java // Low-level chat service
│   │   │           │   └───Prompt/
│   │   │           │       └───PromptTemplate.java // Handles dynamic prompt creation
│   │   │           ├───memory/                     // Memory management for chat history
│   │   │           │   ├───MemoryManager.java      // Interface for memory management
│   │   │           │   ├───SlidingWindowMemory.java// Sliding window memory implementation
│   │   │           │   └───ChatLogs/
│   │   │           │       └───FileMemory.java     // File-based memory implementation
│   │   │           └───message/                    // Message related classes
│   │   │               ├───Message.java            // Represents a chat message
│   │   │               └───MessageRole.java        // Enum for message roles (System, User, Assistant)
│   │   └───resources/
│   └───test/
│       └───java/
├───.gitignore
└───pom.xml                                     // Maven project configuration
```

## Dependencies

The project uses the following key dependencies, managed by Maven:

*   **Jackson (jackson-databind, jackson-datatype-jsr310):** For JSON processing (serialization and deserialization).
*   **Apache HttpComponents Client 5 (httpclient5):** For making HTTP requests to LLM endpoints.
*   **SLF4J (slf4j-api):** Simple Logging Facade for Java.

## Contributing

AI4J is an open-source project, and contributions are highly encouraged! Whether you're fixing bugs, adding new features, improving documentation, or suggesting enhancements, your input is valuable. Please feel free to open issues or submit pull requests.

## License

This project is licensed under the [MIT License](LICENSE).

## Open Source

AI4J is an open-source project, and we welcome community involvement. We believe in collaborative development to build a robust and flexible framework for Java-based AI applications.
