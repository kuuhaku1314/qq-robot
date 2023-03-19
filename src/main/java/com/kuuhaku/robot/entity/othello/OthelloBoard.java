package com.kuuhaku.robot.entity.othello;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author by kuuhaku
 * @Date 2021/2/11 14:03
 * @Description 黑白棋盘
 */
public class OthelloBoard {
    private final static int LENGTH = 8;
    private final static long DEFAULT_TIMEOUT = 30 * 1000;

    private final int[][] othelloBoard = new int[LENGTH][LENGTH];
    private final int[] eatChessRule = new int[]{VisitMethod.LEFT, VisitMethod.DOWN, VisitMethod.UP, VisitMethod.RIGHT,
            VisitMethod.LEFT_UP, VisitMethod.LEFT_DOWN, VisitMethod.RIGHT_UP, VisitMethod.RIGHT_DOWN};
    private final ReentrantLock lock = new ReentrantLock();
    private final BlockingQueue<String> produceQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<ChessOperation> consumerQueue = new LinkedBlockingQueue<>();

    private int curChessColor;
    private int curTurn;
    private long timeout;

    private String blackUserID;
    private String whiteUserID;
    private String blackUserName;
    private String whiteUserName;
    private long createTime;
    private int boardStatus;

    private OthelloBoard() {
    }

    /**
     * 创建新实例
     *
     * @return 棋盘实例对象
     */
    public static OthelloBoard instant() {
        OthelloBoard othelloBoard = new OthelloBoard();
        othelloBoard.boardStatus = OthelloConstant.BOARD_STATUS_PREPARE;
        return othelloBoard;
    }

    public static void main(String[] args) {
        OthelloBoard othelloBoard = OthelloBoard.instant();
        List<ChessOperation> chessOperations = new ArrayList<>();
        String bId = "123";
        String wId = "456";
        int length = 8;
        boolean bFlag = false;
        String infos = "F5 D6 C3 D3 C4 F4 F6 G5 E3 F3 E6 F7 H5 C6 G4 G3 G6 H4 F2 E2 C5 D7 H3 B3 B4 D2 D1 B6 B5 C2 E1 A3 C1 A4 F8 G1 F1 B1 B2 A1 A2 H2 C7 C8 H1 G2 D8 E8 E7 G8 A6 A5 A7 A8 B8 B7 H8 G7 H7 H6";
        String[] strings = infos.split(" ");
        for (String string : strings) {
            // 注意入参转换，棋局内坐标系在xy坐标系第一象限
            int x = string.getBytes(StandardCharsets.UTF_8)[0] - 'A';
            int y = length - (string.getBytes(StandardCharsets.UTF_8)[1] - '0');
            String user = bFlag ? bId : wId;
            chessOperations.add(new ChessOperation(OthelloConstant.OPERATE_MESSAGE, x, y, user));
            bFlag = !bFlag;
        }
        ChessChannel chessChannel = othelloBoard.start(bId, "空白", wId, "咸鱼", OthelloConstant.WHITE, 2);
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    String msg = chessChannel.take();
                    if (!msg.equals(OthelloConstant.END_MESSAGE)) {
                        System.out.println(msg);
                    } else {
                        System.out.println("对局结束");
                        return;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
        });
        thread.start();
        // 要使用下面这段代码，得将ai Num设置为0
        /*for (ChessOperation operation : chessOperations) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                chessChannel.close();
                thread.interrupt();
                return;
            }
            chessChannel.add(operation);
        }*/
    }

    /**
     * 初始化
     *
     * @param blackUser 黑方
     * @param whiteUser 白方
     */
    public ChessChannel start(String blackUser, String blackUserName, String whiteUser, String whiteUserName, int firstStep, int aiNum) {
        this.blackUserID = blackUser;
        this.whiteUserID = whiteUser;
        this.blackUserName = blackUserName;
        this.whiteUserName = whiteUserName;
        curChessColor = firstStep;
        createTime = System.currentTimeMillis();
        curTurn = 1;
        timeout = DEFAULT_TIMEOUT;
        boardStatus = OthelloConstant.BOARD_STATUS_START;
        othelloBoard[3][3] = OthelloConstant.WHITE;
        othelloBoard[4][4] = OthelloConstant.WHITE;
        othelloBoard[3][4] = OthelloConstant.BLACK;
        othelloBoard[4][3] = OthelloConstant.BLACK;
        String firstStepUser = "黑方";
        if (firstStep == OthelloConstant.WHITE) {
            firstStepUser = "白方";
        }
        String startMsg = "对局创建成功\n对局双方为黑方:" + blackUserName + "\n白方:" + whiteUserName + "\n" + firstStepUser + "先下";
        produceQueue.add(startMsg);
        Runnable task = () -> {
            // 第一轮超时任务启动
            new Timer(curTurn, curUserName()).start();
            produceQueue.add(boardInfo());
            while (true) {
                try {
                    ChessOperation chessOperation = consumerQueue.take();
                    lock.lock();
                    if (boardStatus == OthelloConstant.BOARD_STATUS_OVER) {
                        produceQueue.add("对局已经结束");
                        produceQueue.add(OthelloConstant.END_MESSAGE);
                        lock.unlock();
                        return;
                    }
                    // 1. 如果是认输，对局结束
                    if (chessOperation.getType() == OthelloConstant.ADMIT_DEFEAT_MESSAGE) {
                        String failedUser = blackUserName;
                        String winUser = whiteUserName;
                        if (chessOperation.getUserID().equals(winUser)) {
                            failedUser = whiteUserName;
                            winUser = blackUserName;
                        }
                        produceQueue.add(failedUser + "认输了，获胜者是" + winUser);
                        produceQueue.add(OthelloConstant.END_MESSAGE);
                        boardStatus = OthelloConstant.BOARD_STATUS_OVER;
                        lock.unlock();
                        return;
                    }
                    if (getUserChessColor(chessOperation.getUserID()) != curChessColor) {
                        produceQueue.add("不是你的回合");
                        lock.unlock();
                        continue;
                    }
                    // 2. 不能下子，报错
                    int result = operate(chessOperation.getX(), chessOperation.getY(), getUserChessColor(chessOperation.getUserID()));
                    if (result == OthelloConstant.DISABLE) {
                        produceQueue.add("这个地方不能落子");
                        lock.unlock();
                        continue;
                    }

                    toNextTurn();
                    produceQueue.add(boardInfo());
                    // 3. 是否下了后对局结束了
                    if (checkIsComplete()) {
                        produceQueue.add(getEndMessage());
                        produceQueue.add(OthelloConstant.END_MESSAGE);
                        boardStatus = OthelloConstant.BOARD_STATUS_OVER;
                        lock.unlock();
                        return;
                    }
                    // 4. 下家不能下，得跳过这回合
                    if (checkStatus() == OthelloConstant.DISABLE) {
                        produceQueue.add("对方无步数可下，故自动转为下一回合，请继续");
                        toNextTurn();
                        // 设置个超时器
                        new Timer(curTurn, curUserName()).start();
                        // 再check一遍，若两方都不能下，也结束
                        if (checkStatus() == OthelloConstant.DISABLE) {
                            produceQueue.add("当前双方都不可下，开始进行结算");
                            produceQueue.add(getEndMessage());
                            produceQueue.add(OthelloConstant.END_MESSAGE);
                            boardStatus = OthelloConstant.BOARD_STATUS_OVER;
                            lock.unlock();
                            return;
                        }
                        lock.unlock();
                        continue;
                    }
                    // 5. 正常流程，轮到下家下，给下家设置个超时器
                    new Timer(curTurn, curUserName()).start();
                    lock.unlock();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    lock.lock();
                    produceQueue.add(OthelloConstant.END_MESSAGE);
                    boardStatus = OthelloConstant.BOARD_STATUS_OVER;
                    lock.unlock();
                    return;
                }
            }
        };
        Thread thread = new Thread(task);
        thread.start();
        if (aiNum == 1) {
            new BoardAI(8, OthelloConstant.WHITE, whiteUser, whiteUserName).start();
        }
        if (aiNum == 2) {
            new BoardAI(8, OthelloConstant.BLACK, blackUser, blackUserName).start();
            new BoardAI(8, OthelloConstant.WHITE, whiteUser, whiteUserName).start();
        }
        return new ChessChannel(produceQueue, consumerQueue, thread);
    }

    /**
     * 检测场中是否有能下棋子
     *
     * @return 能或不能
     */
    private int checkStatus() {
        for (int x = 0; x < LENGTH; x++) {
            for (int y = 0; y < LENGTH; y++) {
                // 只有空白处可以下
                if (othelloBoard[y][x] == OthelloConstant.BLANK) {
                    if (eatChess(x, y, curChessColor, false) > 0) {
                        return OthelloConstant.ENABLE;
                    }
                }
            }
        }
        return OthelloConstant.DISABLE;
    }

    /**
     * 操作
     *
     * @param chess 操作棋子
     * @param x     x下标
     * @param y     y下标
     * @return 操作结果 成功或失败
     */
    private int operate(int x, int y, int chess) {
        if (x < 0 || x >= LENGTH || y < 0 || y >= LENGTH) {
            return OthelloConstant.DISABLE;
        }
        if (othelloBoard[y][x] == OthelloConstant.BLANK) {
            int eatChessNum = eatChess(x, y, chess, true);
            if (eatChessNum == 0) {
                return OthelloConstant.DISABLE;
            } else {
                return OthelloConstant.ENABLE;
            }
        } else {
            return OthelloConstant.DISABLE;
        }
    }

    /**
     * 获取用户棋子类型
     *
     * @param user 用户
     * @return 黑棋或白棋
     */
    private int getUserChessColor(String user) {
        if (whiteUserID.equals(user)) {
            return OthelloConstant.WHITE;
        } else {
            return OthelloConstant.BLACK;
        }
    }

    /**
     * 改变回合状态，进入下一回合
     */
    private void toNextTurn() {
        if (curChessColor == OthelloConstant.WHITE) {
            curChessColor = OthelloConstant.BLACK;
        } else {
            curChessColor = OthelloConstant.WHITE;
        }
        curTurn++;
    }

    private String curUserName() {
        if (curChessColor == OthelloConstant.WHITE) {
            return whiteUserName;
        } else {
            return blackUserName;
        }
    }

    private int eatChess(int x, int y, int curChessColor, boolean trueDo) {
        int totalEatChessNum = 0;
        BoardVisitor boardVisitor = new BoardVisitor(othelloBoard, new Point(x, y));
        List<Point> eatChessPoint = new ArrayList<>();
        for (int visitMethod : eatChessRule) {
            int eatChessNum = 0;
            boolean hasSameChess = false;
            List<Point> tempPointList = new ArrayList<>();
            // 回到初始点
            boardVisitor.reset();
            while (true) {
                VisitResult result = boardVisitor.visit(visitMethod);
                // 碰到边界，走不了原地踏步
                if (!result.isWalked()) {
                    if (result.getChessColor() == curChessColor) {
                        hasSameChess = true;
                    }
                    break;
                }
                // 碰到空棋子了
                if (result.getChessColor() == OthelloConstant.BLANK) {
                    break;
                }
                // 碰到和自己一个颜色的棋
                if (result.getChessColor() == curChessColor) {
                    hasSameChess = true;
                    break;
                }
                // 这种是碰到和自己不同颜色的棋
                eatChessNum++;
                if (trueDo) {
                    tempPointList.add(result.getPoint());
                }
            }
            // 最后也没碰到相同颜色的，不能吃子
            if (!hasSameChess) {
                continue;
            }
            totalEatChessNum += eatChessNum;
            eatChessPoint.addAll(tempPointList);
        }
        // 改变棋盘状态
        if (trueDo && totalEatChessNum > 0) {
            // 也需要把落子处颜色改变
            eatChessPoint.add(new Point(x, y));
            for (Point point : eatChessPoint) {
                othelloBoard[point.y][point.x] = curChessColor;
            }
        }
        return totalEatChessNum;
    }

    /**
     * 棋局信息
     *
     * @return 信息
     */
    private String boardInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("当前棋盘信息如下:\n");

        for (int j = LENGTH - 1; j >= 0; j--) {
            for (int i = 0; i < LENGTH; i++) {
                if (othelloBoard[j][i] == OthelloConstant.BLANK) {
                    sb.append(OthelloConstant.BLANK_NAME);
                } else if (othelloBoard[j][i] == OthelloConstant.BLACK) {
                    sb.append(OthelloConstant.BLACK_NAME);
                } else if (othelloBoard[j][i] == OthelloConstant.WHITE) {
                    sb.append(OthelloConstant.WHITE_NAME);
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * 是否完成对局
     *
     * @return 当前所剩空格子
     */
    private boolean checkIsComplete() {
        int blankNum = 0;
        for (int i = 0; i < LENGTH; i++) {
            for (int j = 0; j < LENGTH; j++) {
                if (othelloBoard[i][j] == OthelloConstant.BLANK) {
                    blankNum++;
                }
            }
        }
        return blankNum == 0;
    }

    /**
     * 获取黑白棋子数目
     *
     * @return black:white
     */
    private int[] getBlackAndWhiteNum() {
        int blackNum = 0;
        int whiteNum = 0;
        for (int i = 0; i < LENGTH; i++) {
            for (int j = 0; j < LENGTH; j++) {
                if (othelloBoard[i][j] == OthelloConstant.BLACK) {
                    blackNum++;
                } else if (othelloBoard[i][j] == OthelloConstant.WHITE) {
                    whiteNum++;
                }
            }
        }
        return new int[]{blackNum, whiteNum};
    }

    private String getEndMessage() {
        int[] blackAndWhiteNum = getBlackAndWhiteNum();
        String endMsg = "本次对局结束\n" + "黑比白比分为:" + blackAndWhiteNum[0] + ":" + blackAndWhiteNum[1] + "\n";
        if (blackAndWhiteNum[0] == blackAndWhiteNum[1]) {
            endMsg = endMsg + "平局";
        } else if (blackAndWhiteNum[0] > blackAndWhiteNum[1]) {
            endMsg = endMsg + "获胜者是:" + blackUserName;
        } else {
            endMsg = endMsg + "获胜者是:" + whiteUserName;
        }
        endMsg = endMsg + "\n总耗时:" + (System.currentTimeMillis() - createTime) / 1000 + "秒";
        return endMsg;
    }

    @Getter
    private static class BoardVisitor {
        private final int[][] othelloBoard;
        private final Point initPoint;
        private final int xLength;
        private final int yLength;
        private Point curPoint;

        BoardVisitor(int[][] othelloBoard, Point initPoint) {
            this.othelloBoard = othelloBoard;
            this.initPoint = initPoint;
            this.curPoint = initPoint;
            this.xLength = othelloBoard[0].length;
            this.yLength = othelloBoard.length;
        }

        public VisitResult visit(int visitMethod) {
            VisitResult result = new VisitResult();
            Point nextPoint = switch (visitMethod) {
                case VisitMethod.LEFT -> new Point(curPoint.x - 1, curPoint.y);
                case VisitMethod.RIGHT -> new Point(curPoint.x + 1, curPoint.y);
                case VisitMethod.UP -> new Point(curPoint.x, curPoint.y + 1);
                case VisitMethod.DOWN -> new Point(curPoint.x, curPoint.y - 1);
                case VisitMethod.LEFT_UP -> new Point(curPoint.x - 1, curPoint.y + 1);
                case VisitMethod.LEFT_DOWN -> new Point(curPoint.x - 1, curPoint.y - 1);
                case VisitMethod.RIGHT_UP -> new Point(curPoint.x + 1, curPoint.y + 1);
                case VisitMethod.RIGHT_DOWN -> new Point(curPoint.x + 1, curPoint.y - 1);
                default -> throw new RuntimeException("wrong visit method");
            };
            if (nextPoint.x < 0 || nextPoint.x >= xLength || nextPoint.y < 0 || nextPoint.y >= yLength) {
                result.setWalked(false);
                result.setPoint(curPoint);
                result.setChessColor(othelloBoard[curPoint.y][curPoint.x]);
                return result;
            }
            // 合法点
            curPoint = nextPoint;
            result.setWalked(true);
            result.setPoint(curPoint);
            result.setChessColor(othelloBoard[curPoint.y][curPoint.x]);
            return result;
        }

        public void reset() {
            curPoint = new Point(initPoint.x, initPoint.y);
        }
    }

    private static class VisitMethod {
        public static final int LEFT = 1;
        public static final int UP = 2;
        public static final int RIGHT = 3;
        public static final int DOWN = 4;
        public static final int LEFT_UP = 5;
        public static final int RIGHT_UP = 6;
        public static final int LEFT_DOWN = 7;
        public static final int RIGHT_DOWN = 8;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    private static class Point {
        int x;
        int y;
    }

    @Data
    private static class VisitResult {
        private boolean isWalked;
        private int chessColor;
        private Point point;
    }

    // 内部类定时器
    @AllArgsConstructor
    private class Timer extends Thread {
        private int curTurn;
        private String username;

        @Override
        public void run() {
            try {
                Thread.sleep(timeout);
                // 回合数变更或者结束了，说明下了直接返回
                if (OthelloBoard.this.curTurn > curTurn || OthelloBoard.this.boardStatus == OthelloConstant.BOARD_STATUS_OVER) {
                    return;
                }
                ReentrantLock lock = OthelloBoard.this.lock;
                lock.lock();
                // 获取锁后double check
                if (OthelloBoard.this.curTurn > curTurn || OthelloBoard.this.boardStatus == OthelloConstant.BOARD_STATUS_OVER) {
                    lock.unlock();
                    return;
                }
                OthelloBoard.this.produceQueue.add("[" + username + "]超过" + timeout / 1000 + "秒没有下子已超时，系统将自动下子");
                // 找一个可以下子的地方下，遍历，选最大一个
                int maxX = 0, maxY = 0, maxValue = 0;
                for (int i = 0; i < OthelloBoard.LENGTH; i++) {
                    for (int j = 0; j < OthelloBoard.LENGTH; j++) {
                        // 空格可以落子
                        if (othelloBoard[j][i] == OthelloConstant.BLANK) {
                            // 可以吃子，投递一个消息
                            int eatChessNum = OthelloBoard.this.eatChess(i, j, OthelloBoard.this.curChessColor, false);
                            if (eatChessNum > maxValue) {
                                maxX = i;
                                maxY = j;
                                maxValue = eatChessNum;
                            }
                        }
                    }
                }
                String user = OthelloBoard.this.blackUserID;
                if (OthelloBoard.this.curChessColor != OthelloConstant.BLACK) {
                    user = OthelloBoard.this.whiteUserID;
                }
                if (maxValue > 0) {
                    ChessOperation chessOperation = new ChessOperation(OthelloConstant.OPERATE_MESSAGE, maxX, maxY, user);
                    OthelloBoard.this.consumerQueue.add(chessOperation);
                    lock.unlock();
                    return;
                }
                // 理论上不存在这种情况
                lock.unlock();
                throw new RuntimeException("timer abnormal");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @AllArgsConstructor
    private class BoardAI {
        int searchDepth;
        int chessColor;
        String userID;
        String userName;

        private int[][] copy2SpecificBoard(int[][] othelloBoard) {
            int[][] board = new int[LENGTH + 2][LENGTH + 2];
            for (int i = 0; i < LENGTH; i++) {
                for (int j = 0; j < LENGTH; j++) {
                    if (othelloBoard[i][j] == OthelloConstant.BLACK) {
                        board[i + 1][j + 1] = -1;
                    } else if (othelloBoard[i][j] == OthelloConstant.WHITE) {
                        board[i + 1][j + 1] = 1;
                    }
                }
            }
            return board;
        }

        private void start() {
            Runnable runnable = () -> {
                ReentrantLock lock = OthelloBoard.this.lock;
                while (true) {
                    try {
                        Thread.sleep(8000);
                        if (boardStatus == OthelloConstant.BOARD_STATUS_OVER) {
                            return;
                        }
                        if (curChessColor != chessColor) {
                            continue;
                        }
                        lock.lock();
                        if (boardStatus == OthelloConstant.BOARD_STATUS_OVER || curChessColor != chessColor) {
                            lock.unlock();
                            continue;
                        }
                        Point point = bestPoint();
                        produceQueue.add("[AI][" + userName + "]下子 X=" + (point.x + 1) + ",Y=" + (8 - point.y));
                        consumerQueue.add(new ChessOperation(OthelloConstant.OPERATE_MESSAGE, point.x, point.y, userID));
                        lock.unlock();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            new Thread(runnable).start();
        }

        private Point bestPoint() {
            MachineStep machineStep = new MachineStep(searchDepth, LENGTH);
            int[][] board = copy2SpecificBoard(othelloBoard);
            int color = 1;
            if (curChessColor == OthelloConstant.BLACK) {
                color = -1;
            }
            int[] ints = machineStep.nextStep(board, color);
            return new Point(ints[0] - 1, ints[1] - 1);
        }
    }
}

