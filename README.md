# JerryTTS

Mary + Java = Jary -> JerryTTS

<p align="center">
  ![image](https://user-images.githubusercontent.com/39552449/209025976-cd9deaf2-7037-4ca1-9f60-2b9bb486c984.png)
</p>


Initially started out as a prompt to ChatGPT - 'Can you give me an example Text-To-Speech application done in Java?' 
which gave a very bare-bones Hello World application using the FreeTTS library - that I used as a good starting point, added a GUI
to learn about Java GUI development and eventually switched over to MaryTTS when thinking of more features to add

### Features

- 7 different languages (German, English (USA), English (UK), Turkish, Italian, French, Russian, Swedish)
- 32 different voices across all languages
- Separate Volume, Speed and Pitch control for the audio playback (major thanks to @waywardgeek's Sonic implementation https://github.com/waywardgeek/sonic)
- Quickly export currently entered text as .wav to the Clipboard
- Exported audio is affected by Volume, Speed and Pitch control
- 'Loop' and 'Reverse' functions to loop or play the text in reverse

haven't thought about packaging/releasing yet, so everything is done through gradle/IntelliJ for the time being

On first run of `gradle build`, gradle is going to populate the `lib` folder with all the voices from the `mary-voice-urls` file. This can take ~20 minutes from my experience (will ofc depend on your connection speed)

Did my best to scour the web for all available voices and made them available in the app (everything I could find except luxembourgish and telugu, as those two gave problems on compilation)

