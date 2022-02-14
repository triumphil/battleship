# battleship

This is a CLI rendition of the classic [battleship board game](https://en.wikipedia.org/wiki/Battleship_(game)).  In its current state, the game is meant to be played on one, shared terminal window.  The clearScreen utility method ensures the screen is cleared after each Player turn.

Board.class encapsulates board state (rows, columns, and symbol per row / column), input validation, and the collection of placed ships per Player.  Ship.class encapsulates ship state (cells occupied / hit, and is / is not sunk).  Player.class encapsulates the two instantiated Boards and their respectives states.  Play.class is the entrypoint of the program and encompasses the two stages of the game, fillBoard and fireAway.

Throughout both stages, Player 1 and Player 2 take turns placing their ships and taking shots.  Input is parsed and validated before changing board state.  

![Screen Shot 2022-02-14 at 10 37 51 AM](https://user-images.githubusercontent.com/25598690/153925563-e2c94263-6411-49db-a099-524eb709a829.png)

Player input can made left -> right, right -> left, top -> bottom, and bottom -> top.

![Screen Shot 2022-02-14 at 10 40 18 AM](https://user-images.githubusercontent.com/25598690/153926039-b3f151dc-f95c-43f0-9790-2e69c072a9ee.png)
![Screen Shot 2022-02-14 at 10 41 00 AM](https://user-images.githubusercontent.com/25598690/153926049-de94f35e-ab97-4c7d-bf6a-012902a39361.png)

After their turn, Players click Enter to pass control to the other Player.

![Screen Shot 2022-02-14 at 10 42 33 AM](https://user-images.githubusercontent.com/25598690/153926240-1b11116f-69c9-4b78-92a0-eb56dcc26aa1.png)

Once all ships have been placed, the game moves to the fireAway stage.  The Players are presented with two boards: the opponent's (top, and with fog of war applied), and their's (bottom).

![Screen Shot 2022-02-14 at 10 44 10 AM](https://user-images.githubusercontent.com/25598690/153926693-acb04209-9d5f-4492-b7a5-4183a2ff29d2.png)

Hits and misses are rendered on each board.

![Screen Shot 2022-02-14 at 10 48 11 AM](https://user-images.githubusercontent.com/25598690/153927036-c849821c-8732-4c70-a913-d79b9fc7abc3.png)

Once a Player sinks all the ships of the other Player, the game is won.  In its current state, there is no time limit, so players have as many turns as necessary.

If you'd like to play, fork the repo locally and run Play.class.  
