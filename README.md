<p align="center">
  <img src="https://user-images.githubusercontent.com/39552449/209026824-8937a73f-c01c-4945-9794-0a93b6995b87.png" alt="icon" width="140" height="140">
</p>

# JerryTTS 


M**ary** + **J**ava = *Jary* -> JerryTTS

<p align="center">
  <img src="https://user-images.githubusercontent.com/39552449/209254579-d656edb3-8b73-4bf4-b05c-dbd9b440f988.png" alt="screenshot" width="550" height="500">
</p>


Initially started out as a prompt to ChatGPT - 'Can you give me an example Text-To-Speech application done in Java?' 
which gave a very bare-bones Hello World application using the FreeTTS library - that I used as a good starting point, added a GUI
to, to learn about Java GUI development, and eventually switched over to MaryTTS when thinking of more features to add

### Features

- 7 different languages (German, English (USA), English (UK), Turkish, Italian, French, Russian, Swedish)
- 32 different voices across all languages
- Separate Volume, Speed and Pitch control for the audio playback (major thanks to @waywardgeek's Sonic implementation https://github.com/waywardgeek/sonic)
- Quickly export currently entered text as .wav **(NEW!! also as mp3)** to the Clipboard
- Exported audio is affected by Volume, Speed and Pitch control
- 'Loop' and 'Reverse' functions to loop or play the text in reverse
- Automatically transcripts from cyrillic to latin letters in russian mode to enable either latin/cyrillic writing to be spoken in russian (Thanks to @kukicmilorad https://github.com/kukicmilorad/cyrlat)

haven't thought about packaging/releasing yet, so everything is done through gradle/IntelliJ for the time being

On first run of `gradle build`, gradle is going to populate the `lib` folder with all the voices from the `mary-voice-urls` file. This can take ~20 minutes from my experience (will ofc depend on your connection speed)

Did my best to scour the web for all available voices and made them available in the app (everything I could find except luxembourgish and telugu, as those two gave problems on compilation)

