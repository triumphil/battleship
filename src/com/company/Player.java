package com.company;

class Player {
    private final int number;
    private final Board board;

    public Player(int number, Board board) {
        this.number = number;
        this.board = board;
    }

    public int getNumber() {
        return number;
    }

    public Board getBoard() {
        return board;
    }
}