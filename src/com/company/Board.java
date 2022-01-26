package com.company;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/* TODO(spmartinelli): define Cell class instead of using HashMap<String, Integer>
 *   to track ship locations / hits
*/

class Board {
    private final int length;
    private final int width;
    private final String[][] grid;
    private final int minRowKey;
    private final int minColKey;
    private final int maxRowKey;
    private final int maxColKey;
    private List<Ship> ships;
    public Board foggyBoard;
    public int sunkShips;

    enum Symbols {
        EMPTY ("~"),
        SHIP ("O"),
        SHIP_PLACEHOLDER ("*"),
        SHOT_HIT ("X"),
        SHOT_MISS ("M");

        String symbol;

        Symbols(String symbol){
            this.symbol = symbol;
        }
    }

    public Board(int length, int width) {
        this.length = length;
        this.width = width;
        this.grid = new String[length + 1][width + 1];

        String[] colKeys = {" ", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        grid[0] = colKeys;

        for (int row = 1, rowKey = 'A'; row <= length; row++) {
            for (int col = 0; col <= width; col++) {
                if (col == 0) {
                    grid[row][col] = String.valueOf((char) rowKey);
                } else {
                    grid[row][col] = Symbols.EMPTY.symbol;
                }
            }
            rowKey++;
        }
        this.minRowKey = this.grid[1][0].charAt(0);
        this.minColKey = Integer.parseInt(this.grid[0][1]);
        this.maxRowKey = this.grid[length][0].charAt(0);
        this.maxColKey = Integer.parseInt(this.grid[0][width]);
        this.ships = new ArrayList<>();
        this.sunkShips = 0;
    }

    public void print(){
        for (int row = 0; row <= length; row++) {
            for (int col = 0; col <= width; col++) {
                System.out.print(grid[row][col]+(col == width ? "" : " "));
            }
            System.out.println();
        }
    }

    public void addShip(Ship ship){
        this.ships.add(ship);
    }

    public Ship getShip(int rowIndex, int colIndex){
        Ship ship = this.ships.stream().filter(s -> {
            for (HashMap<String, Integer> c: s.occupiedCells){
                if (c.get("row") == rowIndex && c.get("col") == colIndex){
                    return true;
                }
            }
            return false;
        }).collect(Collectors.toList()).get(0);
        return ship;
    }

    public void takePosition(String input, int allowedCells, Ship ship) throws IOException {
        HashMap<String, HashMap<String, String>> startAndEnd = parsePosition(input, allowedCells);
        HashMap<String, String> start = startAndEnd.get("start");
        HashMap<String, String> end = startAndEnd.get("end");
        /*
        Handle cases where the user inputs:
        1) vertical coordinates from bottom to top
        2) horizontal coordinates from right to left
        NOTE: Given the validation rules used at the parse step, there can never be both cases
         */
        if (start.get("row").charAt(0) > end.get("row").charAt(0)) {
            String endCopy = end.get("row");
            end.put("row", start.get("row"));
            start.put("row", endCopy);
        } else if (Integer.parseInt(start.get("col")) > Integer.parseInt(end.get("col"))) {
            String endCopy = end.get("col");
            end.put("col", start.get("col"));
            start.put("col", endCopy);
        }
        for (int rowIndex = 1; rowIndex <= length; rowIndex++) {
            // loop until starting row is found
            if (Objects.equals(this.grid[rowIndex][0], start.get("row"))) {
                if (Objects.equals(start.get("row"), end.get("row"))) {
                    // handle horizontal coords
                    for (int colIndex = Integer.parseInt(start.get("col")); colIndex <= Integer.parseInt(end.get("col")); colIndex++) {
                        setCell(rowIndex, colIndex, Symbols.SHIP.symbol);
                    }
                } else {
                    // handle vertical coords
                    while (this.grid[rowIndex][0].charAt(0) <= end.get("row").charAt(0)) {
                        setCell(rowIndex, Integer.parseInt(start.get("col")), Symbols.SHIP.symbol);
                        if (rowIndex < length) {
                            rowIndex++;
                        } else {
                            break;
                        }
                    }
                }
                break;
            }
        }
    }

    public void takeShot(String input) {
        HashMap<String, String> shot = parseShot(input);
        String rowKey = shot.get("row");
        int colIndex = Integer.parseInt(shot.get("col"));
        for (int rowIndex = 1; rowIndex <= length; rowIndex++) {
            // loop until row is found
            if (Objects.equals(this.grid[rowIndex][0], rowKey)) {
                String cell = this.grid[rowIndex][colIndex];
                if (Objects.equals(Symbols.SHIP.symbol,cell)){
                    Ship ship = getShip(rowIndex, colIndex);
                    setCell(rowIndex, colIndex, Symbols.SHOT_HIT.symbol );
                    ship.setHit(rowIndex, colIndex);
                    this.sunkShips = ship.isSunk() ? ++this.sunkShips : this.sunkShips;
                } else if (Objects.equals(Symbols.SHOT_HIT.symbol,cell)) {
                    System.out.println("You already hit that ship.  Try again");
                } else {
                    setCell(rowIndex, colIndex, Symbols.SHOT_MISS.symbol);
                }
                break;
            }
        }
    }

    public List<String> parseCoords(String input){
        List<String> coords = Arrays.stream(input.split(" "))
                .filter(w -> !w.isEmpty())
                .collect(Collectors.toList());
        return coords;
    }

    public HashMap<String, HashMap<String, String>> parsePosition(String input, int allowedCells) throws RuntimeException{
        List<String> coords = parseCoords(input);
        List<HashMap<String, String>> validatedPosition = validatePosition(coords, allowedCells);
        HashMap<String, HashMap<String, String>> startAndEnd = new HashMap<>();
        startAndEnd.put("start", validatedPosition.get(0));
        startAndEnd.put("end", validatedPosition.get(1));
        return startAndEnd;
    }

    public HashMap<String, String> parseShot(String input) throws RuntimeException{
        List<String> coords = parseCoords(input);
        if (coords.size() > 1) {
            throw new RuntimeException("Error: shots require one coordinate");
        }
        HashMap<String, String> validatedShot = validateShot(coords.get(0));
        return validatedShot;
    }

    private List<HashMap<String, String>>  validatePosition(List<String> input, int allowedCells){
        List<HashMap<String, String>> startAndEnd = new ArrayList<>();
        for (String i: input){
            HashMap<String, String> coords = validateCoords(i);
            startAndEnd.add(coords);
        }
        /*
        Validate for:
        1) diagonal position
        2) incorrect number of cells
         */
        char startRow = startAndEnd.get(0).get("row").charAt(0);
        int startCol = Integer.parseInt(startAndEnd.get(0).get("col"));
        char endRow = startAndEnd.get(1).get("row").charAt(0);
        int endCol = Integer.parseInt(startAndEnd.get(1).get("col"));
        boolean verticalPosition = !Objects.equals(startRow,endRow) ? true : false;
        boolean diagonalPosition = !Objects.equals(startRow, endRow) && !Objects.equals(startCol, endCol) ? true : false;

        if (diagonalPosition) {
            throw new RuntimeException(String.format("Error: coordinates must form an unbroken vertical or horizontal line"));
        } else if (verticalPosition) {
            if (Math.abs(startRow - endRow) + 1 != allowedCells){
                throw new RuntimeException(String.format("Error: wrong number of cells"));
            }
        } else {
            if (Math.abs(startCol - endCol) + 1 != allowedCells){
                throw new RuntimeException(String.format("Error: wrong number of cells"));
            }
        }
        return startAndEnd;
    }

    private HashMap<String, String> validateShot(String input){
        HashMap<String, String> coords = validateCoords(input);
        return coords;
    }

    private HashMap<String, String> validateCoords(String input) throws RuntimeException {
        HashMap<String, String> coords = new HashMap<>();
        String rowKey;
        int colKey;

        String pattern = "^([A-Z]+)([0-9]+)$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(input);

        if (m.find()) {
            rowKey = m.group(1);
            colKey = Integer.parseInt(m.group(2));
            if (rowKey.toCharArray().length > 1){
                throw new RuntimeException(String.format("Error: row key '%s' is not a single char between A-Z", rowKey));
            } else if (rowKey.charAt(0) > maxRowKey || rowKey.charAt(0) < minRowKey) {
                throw new RuntimeException(String.format("Invalid coordinates: Row key '%s' is out of bounds",rowKey));
            }
            else if (colKey > maxColKey || colKey < minColKey) {
                throw new RuntimeException(String.format("Error: column key '%s' is out of bounds",colKey));
            }
        } else {
            throw new RuntimeException(String.format("Error: coordinates '%s' do not match the {A-Z}{n} format", input));
        }
        coords.put("row", rowKey);
        coords.put("col", String.valueOf(colKey));
        return coords;
    }

    private void setCell(int rowIndex, int colIndex, String symbol) throws RuntimeException {
        if (Symbols.SHIP.symbol.equals(symbol)) {
            int above = rowIndex - 1 <= 1 ? 1 : rowIndex - 1;
            int below = rowIndex + 1 >= length ? length : rowIndex + 1;
            int right = colIndex + 1 >= width ? width : colIndex + 1;
            int left = colIndex - 1 <= 1 ? 1 : colIndex - 1;
            if (this.grid[rowIndex][colIndex] == "O"
                    || this.grid[above][colIndex] == "O"
                    || this.grid[rowIndex][left] == "O"
                    || this.grid[below][colIndex] == "O"
                    || this.grid[rowIndex][right] == "O"
            ) {
                throw new RuntimeException("Error: position is taken or too close to other ships");
            } else {
                // use placeholder symbol to prevent collision with adjacent fields of the ship being set
                this.grid[rowIndex][colIndex] = Symbols.SHIP_PLACEHOLDER.symbol;
            }
        } else {
            this.foggyBoard.grid[rowIndex][colIndex] = (Symbols.SHOT_HIT.symbol.equals(symbol) ? Symbols.SHOT_HIT.symbol : Symbols.SHOT_MISS.symbol);
            this.grid[rowIndex][colIndex] = (Symbols.SHOT_HIT.symbol.equals(symbol) ? Symbols.SHOT_HIT.symbol : Symbols.SHOT_MISS.symbol);
            if (Symbols.SHOT_MISS.symbol.equals(symbol)) {
                System.out.println("You missed!");
            }
        }
    }

    public void fillPlaceholders(Ship ship){
        for (int rowIndex = 1; rowIndex <= length; rowIndex++) {
            for (int colIndex = 0; colIndex <= width; colIndex++) {
                if (Symbols.SHIP_PLACEHOLDER.symbol.equals(grid[rowIndex][colIndex])) {
                    grid[rowIndex][colIndex] = Symbols.SHIP.symbol;
                    ship.setPosition(rowIndex, colIndex);
                }
            }
        }
    }

    public void removePlaceholders(){
        for (int rowIndex = 1; rowIndex <= length; rowIndex++) {
            for (int colIndex = 0; colIndex <= width; colIndex++) {
                grid[rowIndex][colIndex] = Symbols.SHIP_PLACEHOLDER.symbol.equals(grid[rowIndex][colIndex]) ? Symbols.EMPTY.symbol : grid[rowIndex][colIndex];
            }
        }
    }
}
