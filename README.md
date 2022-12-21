# JerryTTS

Mary + Java = Jary -> JerryTTS

Initially started out as a prompt to ChatGPT - 'Can you give me an example Text-To-Speech application done in Java?' 
which gave a very bare-bones Hello World application using the FreeTTS library - that I used as a good starting point, added a GUI
to learn about Java GUI development and eventually switched over to MaryTTS when thinking of more features to add

- 8 different languages
- 32 different voices across all languages
- Separate Volume, Speed and Pitch control for the audio playback (major thanks to @waywardgeek's Sonic implementation https://github.com/waywardgeek/sonic)
- Quickly export currently entered text as .wav to the Clipboard

haven't thought about packaging/releasing yet, so everything is done through gradle/IntelliJ for the time being

On first run of `gradle build`, gradle is going to populate the `lib` folder with all the voices from the `mary-voice-urls` file. This can take ~20 minutes from my experience (will ofc depend on your connection speed)

![image](https://user-images.githubusercontent.com/39552449/208804492-0fc87689-9f0a-4426-81ab-0c950c3143ad.png)

Did my best to scour the web for all available voices and made them available in the app (everything I could find except luxembourgish and telugu, as those two gave problems on compilation)

