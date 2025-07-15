mvn clean install

jpackage ^
  --input target ^
  --name SwingChatbotApp ^
  --main-jar AI4J-1.0-SNAPSHOT-jar-with-dependencies.jar ^
  --main-class com.aiforjava.demo.SwingChatbot ^
  --type exe ^
  --icon src/main/resources/icon.ico ^
  --app-version 1.0.0 ^
  --vendor "RonakGH97" ^
  --description "AI Chatbot Powered by Java framework" ^
  --java-options "-Xmx1024m -Dfile.encoding=UTF-8" ^
  --dest dist/app
