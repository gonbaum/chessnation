## Chess with video

This is a backend for application that should enable to play chess and have a video chat with your opponent at the same time.

It connects to lichess to keep the game running there (this dependency could be dropped).

Next tasks:

- keep status of the game on backend
- do not allow play before there are at least 2 players
- make the `my turn` logic smarter - backend should notify whose turn it is
- enable multiple games/videos at the same time (e.g. keep lists of channels together)
