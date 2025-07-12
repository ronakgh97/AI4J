# AI4J: Java Framework for Large Language Models

AI4J (AI for Java) is a lightweight and robust Java framework designed to simplify the integration and interaction with Large Language Models (LLMs). It provides a comprehensive set of tools for building intelligent applications, supporting both low-level, stateless interactions and high-level, stateful conversational experiences. AI4J is particularly well-suited for connecting to LLMs accessible via local HTTP endpoints (e.g., LM Studio, Ollama), offering flexibility, efficient memory management, and a focus on developer experience.

**Note:** This project is currently under active development, and continuous improvements are being made.

## Features

AI4J offers a rich set of features to empower your LLM-powered applications:

### Core LLM Interaction APIs

*   **Low-Level Chat API (`ChatServices_LowLevel`):** Provides direct, stateless interaction with LLM chat completion endpoints. Ideal for fine-grained control over requests and raw JSON payload manipulation.
*   **High-Level Chat API (`ChatServices`):** A higher-level abstraction that simplifies building stateful chatbots by automatically managing conversational context and integrating with memory managers and prompt templates. It also includes functionality for generating image descriptions for multimodal messages and a `reset()` method to clear the conversation history.

### LLM Client & Connectivity

*   **Flexible LLM Client (`LLM_Client` & `DefaultHttpClient`):** An extensible interface for LLM communication, with a default HTTP client implementation supporting:
    *   Configurable base URLs and API endpoints.
    *   Customizable request timeouts.
    *   API key authentication for online LLM providers (e.g., Gemini, OpenAI).
    *   Option to use the base URL as the full endpoint URI for specific API structures (`useBaseUrlAsEndpoint`).
*   **Streaming Support:** Efficiently handles LLM responses as they are generated, providing a smooth, real-time user experience.
    *   **Synchronous and Asynchronous Streaming:** Provides both `sendStreamRequest` for blocking stream processing and `sendStreamRequestAsync` for non-blocking, asynchronous stream handling using `CompletableFuture` and `ScheduledExecutorService`.
    *   **Custom Stream Handling:** Utilizes `StreamHandler` for processing incoming content chunks.
    *   **Flexible Stream Response Parsing:** The `StreamResponseParser` interface (with `DefaultStreamResponseParser` and `WordStreamHandler`) allows for custom parsing of diverse LLM streaming response formats.
    *   **Configurable Streaming Speed:** Control the speed of streamed responses using `streamDelayMillis` for a smoother output.
    *   **Resource Management:** `DefaultHttpClient` now implements `AutoCloseable` to ensure proper shutdown of internal resources like `ScheduledExecutorService`.

### Memory Management

AI4J provides flexible and optimized memory management strategies to maintain conversational context:

*   **`MemoryManager` Interface:** Defines the contract for adding, retrieving, and clearing messages.
*   **`SlidingWindowMemory`:** Maintains a fixed-size window of the most recent messages, discarding older ones to keep context relevant.
*   **`OptimizedSlidingWindowMemory`:** An improved, `Deque`-based implementation of sliding window memory for better performance.
*   **`FileMemory`:** Persists chat history to a file, enabling conversations to be resumed across application sessions.
*   **`CachedFileMemory`:** An enhanced file-based memory solution with in-memory caching for improved performance and efficient access to chat logs. It offers:
    *   `addMessage(Message)`: Adds a message to the cache without immediate file persistence.
    *   `addMessageAndSave(Message)`: Adds a message to the cache and immediately saves to file.
    *   `flush()`: Explicitly writes cached messages to the file.
*   **`TokenCountingMemory`:** Manages memory based on the number of tokens, ensuring the conversation history does not exceed the LLM's context window.

### Caching

AI4J now supports caching of LLM responses to reduce redundant API calls and improve performance.

*   **`LLMCacheManager`:** Manages the Caffeine cache, providing methods to store, retrieve, and invalidate LLM responses.
*   **Cached LLM Calls:** The `ChatServices_LowLevel` class now includes a `generateWithCache` method that leverages the `LLMCacheManager` to serve responses from cache when available.

### Message Structure

*   **`Message` & `MessageRole`:** Clearly defined classes for representing conversational messages, including roles like `SYSTEM`, `USER`, and `ASSISTANT`.
*   **Multimodal Messaging:** The `Message` class now supports multimodal content, allowing for a combination of text and images within a single message.
    *   **`MessagePart`:** An interface defining a part of a multimodal message.
    *   **`TextPart`:** Represents the text content within a multimodal message.
    *   **`ImagePart`:** Represents image content (Base64 encoded) within a multimodal message.
    *   **`ImageEncoder`:** A utility for encoding image files into Base64 strings for inclusion in multimodal messages.

### Prompt Engineering

*   **`PromptTemplate`:** A powerful utility for defining and formatting prompts with dynamic variables. It allows for flexible structuring of system and user messages, enhancing prompt reusability and consistency.

### Model Parameter Configuration

*   **`ModelParams`:** Easily configure LLM parameters such as temperature, maximum tokens, top-p, frequency penalty, and presence penalty. Includes robust input validation to ensure valid configurations.
*   **LLM Thinking Control:** For models that support it, the framework allows control over the LLM's internal 'thinking' process. By default, thinking is enabled. To disable it for a specific prompt, the `/no_think` suffix can be used (managed by the UI).

### Robust Error Handling & Logging

*   **Custom Exceptions:** Implements a more elegant and specific exception handling mechanism with custom exceptions:
    *   `LLMServiceException`: General LLM service errors.
    *   `LLMParseException`: Errors during LLM response parsing.
    *   `LLMNetworkException`: Network communication errors.
    *   `Exception_Timeout`: Request timeout errors.
    *   `LLMStreamProcessingException`: Errors during asynchronous stream processing.
    *   `MemoryAccessException`: Errors related to memory access and persistence operations.
*   **`ExceptionHandler`:** Centralized exception handling.
*   **SLF4J Integration:** Uses SLF4J for flexible and configurable logging of errors and application events.

### Graphical User Interface (Demo)

*   **`SwingChatbot`:** A high-level, Java Swing-based graphical user interface demonstrating the framework's capabilities.
    *   **Enhanced UI/UX:** Features streamlined user message display, efficient input field clearing, and improved focus handling.
    *   **Dynamic UI Elements:** UI elements like the "Attach Image" button and "Think" checkbox are dynamically enabled/disabled based on the capabilities of the currently selected LLM model, as defined in the `ModelRegistry`.
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
*   **Local LLM Server (Optional but Recommended):** To run the examples, you'll need a local LLM server like [LM Studio](https://lmstudio.ai/) or [Ollama](https://ollama.ai/) running and serving a compatible model (e.g., `google/gemma-3-1b`) on `http://localhost:1234`. The `testEndpoints.java` file can be used to verify connectivity to `http://127.0.0.1:1234/v1/models`.

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

## Project Structure

*   `src/main/java/com/aiforjava/demo`: Contains the `SwingChatbot` GUI and related UI classes.
*   `src/main/java/com/aiforjava/examples`: Includes various example classes demonstrating the framework's features.
*   `src/main/java/com/aiforjava/llm`: The core of the framework, containing the LLM client, chat services, and model-related classes.
*   `src/main/java/com/aiforjava/memory`: Provides different memory management strategies.
*   `src/main/java/com/aiforjava/message`: Defines the message structure for conversations.
*   `src/main/java/com/aiforjava/exception`: Contains custom exception classes for robust error handling.

## Security Considerations

### API Key Management

**Warning:** Do not hardcode API keys in your source code. The `DefaultHttpClient` is designed to accept an API key, but it should be provided securely.

**Recommendation:** Use environment variables or a secure configuration file to store and load your API keys.

```java
// Example of loading an API key from an environment variable
String apiKey = System.getenv("MY_LLM_API_KEY");
LLM_Client client = new DefaultHttpClient(LLM_BASE_URL, Duration.ofSeconds(90), apiKey);
```

## Configuration

### Externalizing Configuration

**Recommendation:** Avoid hardcoding values like base URLs and model names directly in the code. Instead, use a configuration file (e.g., `config.properties`) to store these values. This makes your application more flexible and easier to configure for different environments.

## Testing

### Importance of Unit Tests

This project currently lacks a comprehensive suite of unit tests. For any serious application, it is crucial to add unit tests to ensure the reliability and maintainability of the codebase.

**Recommendation:** Use a testing framework like JUnit 5 to write unit tests for the core components of the framework, such as the `ChatServices`, `MemoryManager` implementations, and `LLM_Client`.

## Usage Examples

The `src/main/java/com.aiforjava.examples` directory contains several example chatbots demonstrating different features.

### 1. Stateless Chatbot (`StatelessChatbot.java`)

Demonstrates basic, stateless interaction with the LLM using the low-level API. Each turn is independent.

```java
// Example snippet (simplified)
LLM_Client client = new DefaultHttpClient("http://localhost:1234", Duration.ofSeconds(90), null);
ChatServices_LowLevel llm = new ChatServices_LowLevel(client, "google/gemma-3-1b");
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
    new ChatServices_LowLevel(new DefaultHttpClient("http://localhost:1234", Duration.ofSeconds(90), null), "google/gemma-3-1b"),
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

Showcases the lowest level of interaction, allowing direct sending of raw JSON requests to the LLM endpoint for maximum flexibility, including both non-streaming (`generateRaw`) and streaming (`generateStreamRaw`) requests.

```java
// Example snippet (simplified)
ChatServices_LowLevel llm = new ChatServices_LowLevel(new DefaultHttpClient("http://localhost:1234", Duration.ofSeconds(90), null), "google/gemma-3-1b");
String rawRequest = "{ "model": "google/gemma-3-1b", "messages": [ { "role": "user", "content": "Hello" } ] }";
String rawResponse = llm.generateRaw("v1/chat/completions", rawRequest);
System.out.println(rawResponse);
```

### 7. Multimodal Chatbot (`MultimodalChatbot.java`)

Demonstrates sending text messages with image attachments and controlling the LLM's 'thinking' behavior.

```bash
mvn exec:java -Dexec.mainClass="com.aiforjava.examples.MultimodalChatbot"
```

## Dependencies

The project uses the following key dependencies, managed by Maven:

*   **Jackson (`jackson-databind`, `jackson-datatype-jsr310`):** For efficient JSON processing (serialization and deserialization).
*   **Apache HttpComponents Client 5 (`httpclient5`):** For making robust HTTP requests to LLM endpoints.
*   **SLF4J (`slf4j-api`):** Simple Logging Facade for Java, providing flexible and configurable logging.
*   **Caffeine (`caffeine`):** A high-performance caching library.

## Recent Changes

*   **Caching:** Integrated Caffeine to provide caching for LLM responses, reducing latency and API costs. Introduced `LLMCacheManager` and `generateWithCache` method in `ChatServices_LowLevel`.
*   **Resource Management:** `DefaultHttpClient` now implements `AutoCloseable` to ensure proper shutdown of internal resources.
*   **Enhanced Memory Management:** `CachedFileMemory` now provides `addMessage` (non-saving), `addMessageAndSave` (immediate saving), and `flush()` methods for flexible persistence control. Clarified recommendation for `OptimizedSlidingWindowMemory` over `SlidingWindowMemory`.

## Contributing

AI4J is an open-source project, and contributions are highly encouraged! Whether you're fixing bugs, adding new features, improving documentation, or suggesting enhancements, your input is valuable. Please feel free to open issues or submit pull requests.

## License

This project is licensed under the [MIT License](LICENSE).

## Open Source

AI4J is an open-source project, and we welcome community involvement. We believe in collaborative development to build a robust and flexible framework for Java-based AI applications.