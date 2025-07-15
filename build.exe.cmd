mvn clean install

jpackage ^
  --input target ^
  --name SwingChatbotApp ^
  --main-jar AI4J-1.0-SNAPSHOT-jar-with-dependencies.jar ^
  --main-class com.aiforjava.demo.SwingChatbot ^
  --type exe ^
  --icon src/main/resources/icon.ico ^
  --app-version 1.0.0 ^
  --vendor "Ronakgh97" ^
  --description "AI Chatbot Powered by Java framework" ^
  --java-options "-Xmx1024m -Dfile.encoding=UTF-8" ^
  --dest build/app ^
  --win-shortcut ^
  --win-menu ^
  --win-dir-chooser ^
  --win-console ^
  --win-menu-group "AI Tools" ^
  --license-file LICENSE.txt ^
  --copyright "© 2025 Ronakgh97. All rights reserved."
