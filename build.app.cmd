echo [*] Creating app image using jpackage...
jpackage ^
  --input target ^
  --name SwingChatbotApp ^
  --main-jar AI4J-1.0-SNAPSHOT-jar-with-dependencies.jar ^
  --main-class com.aiforjava.demo.ChatBotApp.SwingChatbot ^
  --type app-image ^
  --icon src/main/resources/icon.ico ^
  --app-version 0.8.8 ^
  --vendor "Ronakgh97" ^
  --description "AI Chatbot Powered by Java framework" ^
  --java-options "-Xmx1024m -Dfile.encoding=UTF-8" ^
  --dest build/image ^
  --resource-dir src/main/resources ^
  --copyright "2025 RonakGH97"


