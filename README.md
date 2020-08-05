## Chess with video

This is a backend for application that should enable to play chess and have a video chat with your opponent at the same time.

It connects to lichess to keep the game running there (this dependency could be dropped).

Next tasks:

- keep status of the game on backend
- make the `my turn` logic smarter - backend should notify whose turn it is
- enable multiple games at the same time (e.g. keep pairs of channels together)
- deploy the application
- start working on integrating video
