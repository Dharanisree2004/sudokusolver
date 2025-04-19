import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class SudokuSolver extends JFrame {
    private static final int SIZE = 9;
    private JTextField[][] grid;
    private JButton solveButton, stopButton, resetButton, randomFillButton;
    private volatile boolean solving;

    public SudokuSolver() {
        setTitle("Sudoku Solver");
        setSize(600, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel board = new JPanel(new GridLayout(SIZE, SIZE));
        board.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        board.setBackground(Color.BLACK);
        grid = new JTextField[SIZE][SIZE];
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                grid[row][col] = new JTextField();
                grid[row][col].setHorizontalAlignment(JTextField.CENTER);
                grid[row][col].setFont(new Font("Arial", Font.BOLD, 20));
                grid[row][col].setForeground(Color.WHITE);
                grid[row][col].setBackground(Color.BLACK);
                grid[row][col].setBorder(BorderFactory.createLineBorder(Color.GRAY));
                board.add(grid[row][col]);
            }
        }
        add(board, BorderLayout.CENTER);


        JPanel controlPanel = new JPanel();
        solveButton = new JButton("Solve");
        stopButton = new JButton("Stop");
        resetButton = new JButton("Reset");
        randomFillButton = new JButton("Random Fill");

        controlPanel.add(solveButton);
        controlPanel.add(stopButton);
        controlPanel.add(resetButton);
        controlPanel.add(randomFillButton);
        add(controlPanel, BorderLayout.SOUTH);

        solveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                solving = true;
                new Thread(new Solver()).start();
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                solving = false;
            }
        });

        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetGrid();
            }
        });

        randomFillButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                randomFillGrid();
            }
        });
    }

    private void resetGrid() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                grid[row][col].setText("");
                grid[row][col].setForeground(Color.WHITE);
            }
        }
    }

    private void randomFillGrid() {
        resetGrid();
        int[][] board = new int[SIZE][SIZE];
        fillBoard(board);
        removeCells(board, 41); // remove 41 cells to leave 40 filled

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] != 0) {
                    grid[row][col].setText(String.valueOf(board[row][col]));
                    grid[row][col].setForeground(Color.GREEN);
                }
            }
        }
    }

    private boolean fillBoard(int[][] board) {
        int[] rowCol = findEmptyLocation(board);
        if (rowCol == null) return true; // board is complete

        int row = rowCol[0];
        int col = rowCol[1];
        int[] numbers = generateRandomNumbers();

        for (int num : numbers) {
            if (isValid(board, row, col, num)) {
                board[row][col] = num;
                if (fillBoard(board)) {
                    return true;
                }
                board[row][col] = 0;
            }
        }
        return false;
    }

    private void removeCells(int[][] board, int count) {
        Random random = new Random();
        while (count > 0) {
            int row = random.nextInt(SIZE);
            int col = random.nextInt(SIZE);
            if (board[row][col] != 0) {
                board[row][col] = 0;
                count--;
            }
        }
    }

    private int[] findEmptyLocation(int[][] board) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] == 0) {
                    return new int[]{row, col};
                }
            }
        }
        return null;
    }

    private int[] generateRandomNumbers() {
        int[] numbers = new int[SIZE];
        for (int i = 0; i < SIZE; i++) {
            numbers[i] = i + 1;
        }
        Random random = new Random();
        for (int i = 0; i < SIZE; i++) {
            int j = random.nextInt(SIZE);
            int temp = numbers[i];
            numbers[i] = numbers[j];
            numbers[j] = temp;
        }
        return numbers;
    }
    private boolean isValid(int[][] board, int row, int col, int num) {
        for (int i = 0; i < SIZE; i++) {
            if (board[row][i] == num || board[i][col] == num) {
                return false;
            }
        }
        int boxRowStart = (row / 3) * 3;
        int boxColStart = (col / 3) * 3;
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (board[boxRowStart + r][boxColStart + c] == num) {
                    return false;
                }
            }
        }
        return true;
    }
    private boolean isValid(JTextField[][] grid, int row, int col, int num) {
        String numStr = String.valueOf(num);
        for (int i = 0; i < SIZE; i++) {
            if (numStr.equals(grid[row][i].getText()) || numStr.equals(grid[i][col].getText())) {
                return false;
            }
        }
        int boxRowStart = (row / 3) * 3;
        int boxColStart = (col / 3) * 3;
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (numStr.equals(grid[boxRowStart + r][boxColStart + c].getText())) {
                    return false;
                }
            }
        }
        return true;
    }

    private class Solver implements Runnable {
        @Override
        public void run() {
            solveSudoku(0, 0);
        }

        private boolean solveSudoku(int row, int col) {
            if (!solving) {
                return false;
            }
            if (row == SIZE) {
                return true;
            }
            if (col == SIZE) {
                return solveSudoku(row + 1, 0);
            }
            if (!grid[row][col].getText().isEmpty()) {
                return solveSudoku(row, col + 1);
            }
            for (int num = 1; num <= SIZE; num++) {
                if (isValid(grid, row, col, num)) {
                    grid[row][col].setText(String.valueOf(num));
                    grid[row][col].setForeground(Color.YELLOW);
                    try {
                        Thread.sleep(100); // Delay to visualize the solving process
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (solveSudoku(row, col + 1)) {
                        return true;
                    }
                    grid[row][col].setText("");
                }
            }
            return false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SudokuSolver().setVisible(true);
            }
        });
    }
}

