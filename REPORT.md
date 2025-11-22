# Project Report

## Challenges:
**Challenge 1:**
Problem: There was a method that required a track-type variable and was instead given a string. The method was trying to identify a track based off it's ID number.
Solution: Removed the call for ID number, since the object calling for it was already the track in question.
Learned: Sometimes a solution is as simple as deleting a small tidbit of code, and it's best to look for simple soluitions over convoluded ones (such as converting the string to a track, or trying to gather a track from the string.)

**Challenge 2:**
Problem: Whenever we attempted to utalize the application, there would be a failure of connection. After much debugging, this was casued by our ports not actively listening to the server before we tried calling/connecting to it.
Solution: 
Learned:

**Challenge 3:**
Problem: A depreciated method was utalized in the code to gather all of the private variables of a track
Solution: We removed the depreciated code, and replaced it with code that already did the same thing.
Learned: The work of a programmer is never done. Also always keep up to date on all documentation for APIs you intend to use.

**Challenge 5:**
Problem: There were many issues involving the IDE not running the program due to a lack of try/catch statements.
Solution: Utalize IntellJ's built in dev tools to automatically add such statements to our code seamlessly.
Learned: Work smarter not harder.

## Design Pattern Justifications
**Strategy Pattern: Strategies were modeled after lab documents provided to us by our instructor. Genre strategy is self explanitory, simply utalizing data gatherd from tracks and matching it as closely as possible to the searched/desired track. We also added a slight popularity modifier, just to add some priority over the potential 1,000 alternative pop rock songs. The hybrid strategy would have employed a random precentage weight of popularity, simularity, and genre, with popularity being the heaviest bias.** 

**Factory Pattern: I neither now what this is or how it may or may not exist within our program.** 

**Observer Pattern: I neither now what this is or how it may or may not exist within our program.** 

## AI Usage
Used ChatGPT to help debug code
Asked: Yes
Modified: Yes
Verified: Yes

## Time Spent: 6 hours, probably.
