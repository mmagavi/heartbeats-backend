
# heartBeats!
**Team Members:** Quinn Coleman (qcoleman), Maya Magavi(mmagavi), Dalton Siminson (dsimonso), Yanchi Wang (ywang568)

**Repo:** https://github.com/cs0320-s2023/heartBeats

heartBeats is a smart exercise and relaxation playlist creation tool! Our web app gives users the option to create either of the two aforementioned playlist types each with their own set of questions to create the perfect playlist. After logging in with Spotify, users can enter their resting heart rate, age, some genres that they enjoy as well as the option to start with a warmup and/or end with a cooldown(~10 minutes each: 20-minute min for cooldown + warmup). If the user wants to create a relaxing playlist, they can enter their resting heart rate and some genres they feel will help them relax and the desired length to create a playlist to help the user wind down.
 
## Design Choices:
![alt text](https://github.com/cs0320-s2023/heartBeats/blob/main/heartBeats%20.drawio.png)
*Frontend:*
The front end of our web app was created in TypeScript using the React framework and contains 6 major pages with 6 custom-made components for UI interaction. Our front end was also made with accessibility in mind and thus has appropriate ARIA labels for each of our components as well as keyboard shortcuts for navigation. 


*Backend:*
The backend of this project uses a Spotify API wrapper that makes calling the API super simple and easy to integrate into algorithms. Our backend works by gathering the information from the front end via our server, this info is then fed into our GeneratePlaylist class which uses its sub-classes to create a RelaxPlaylist or ExercisePlaylist by making strategic calls to the Spotify API. The calls to the API include: gathering recommendations for different tempos based on the user’s top artists, songs, etc + entered genres; generating and fetching the user’s authorization token; getting the user’s top artists, songs, etc; creating/adding to playlists. After the playlist is assembled, a playlist ID is created and passed to the front end to be embedded into the page.
[Link to Pseudocode](https://docs.google.com/document/d/1qbjPI3FvR3JpWzQsDao_2zjBzG7Y9lbstrW7J64F35w/edit?usp=sharing)


## Errors/Bugs:

-Due to the specificity of the call, occasionally GenerateRelax playlists will be shorter than expected due to no/not enough songs being available that fit the constraints.

## Tests:
*Frontend:*

-basic.test.tsx:
Basic testing for frontend does not use mocking due to the limitations of Spotify API. Confirms elements are properly rendered and that buttons, navigation, and the page overall are visible and functional.
-interaction.test.tsx:
This robust test suite tests that all UI elements on all of the pages in the app are rendering properly. This suite also uses integration testing to check that all of the buttons, sliders, and navigation perform properly upon user interaction.


*Backend:*

-ExercisePlaylistTests.java:
This test suite confirms that the playlist is being successfully created without errors and uses the heart rate intervals that are within the ExercisePlaylist object to confirm that only songs within the given heart rate interval are being added to the playlist at the correct position. This test is done for all combinations of warm-up/cool-down vs no warm-up/no cooldown.

-RelaxPlaylistTests.java:
This test suite confirms that the playlist is being successfully created without errors and uses the heart rate intervals that are within the RelaxPlaylist object to confirm that only songs within the given heart rate interval are being added to the playlist at the correct position within the tempo descent at the beginning of the playlist. 

-UtilitiesTests:
This test package contains tests for all of the utilities used from the Spotify API wrapper class and confirms that they are erroring when they are supposed to.


## Setup and Running the Program:

*-Frontend: *
The user will need to install Node Package Manager(via “npm install) and load up the local-host
Using http://localhost:5173/ in the web browser, the user can log in and navigate through the webapp by making selections
After pressing the submit button, the playlist should be rendered as an embed and saved to the user’s Spotify account

*-Backend:*
The user will also need to run the  heartBeats at the same time






