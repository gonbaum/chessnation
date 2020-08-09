## Chess with video

This is a backend for application that should enable to play chess and have a video chat with your opponent at the same time.

It connects to lichess to keep the game running there (this dependency could be dropped).

Next tasks:

- fix connection of the clients to WebSocket - maybe assign the color immediately when the client joins
- simplify UI of video
- enable multiple games/videos at the same time (e.g. keep lists of channels together)
