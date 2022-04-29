package com.kuuhaku.robot.entity.othello;

/**
 * @author by kuuhaku
 * @date 2022/4/17 14:30
 * @description
 */

public class MachineStep {
    final int INF = 0x3f3f3f3f;
    static final int[][] valueMap = {{500, -25, 10, 5, 5, 10, -25, 500},
            {-25, -45, 1, 1, 1, 1, -45, -25},
            {10, 1, 3, 2, 2, 3, 1, 10},
            {5, 1, 2, 1, 1, 2, 1, 5},
            {5, 1, 2, 1, 1, 2, 1, 5},
            {10, 1, 3, 2, 2, 3, 1, 10},
            {-25, -45, 1, 1, 1, 1, -45, -25},
            {500, -25, 10, 5, 5, 10, -25, 500}};
    int maxDepth;
    int color;
    int n;

    MachineStep(int depth, int n) {
        this.maxDepth = depth;
        this.n = n;
    }

    public int[] nextStep(int[][] board, int color) {
        this.color = color;
        Step res = dfs(board, color, 1, -INF, INF);
        return new int[]{res.y, res.x};
    }

    // minimax搜索
    Step dfs(int[][] board, int color, int dep, int a, int b) {
        if(dep > this.maxDepth) {
            return new Step(-1, -1, evaluate(board, 0, color));
        }
        int nextColor = -color;
        int[][] pos = legalPosition(board, color);
        if(count(pos) == 0) {
            int [][] nextPos = legalPosition(board, nextColor);
            if(count(nextPos) == 0) {
                return new Step(0, 0, evaluate(board, 0, color));
            }
            else {
                return dfs(board, nextColor, dep, a, b);
            }
        }
        int maxx = -INF;
        int minx = INF;
        Step res = new Step(-1, -1, -1);
        for(int i = 1; i <= n; i++) {
            for(int j = 1; j <= n; j++) {
                if(pos[i][j] == 0) continue;
                int[][] newBoard = nextBoard(board, i, j, color);
                Step ths = dfs(newBoard, nextColor, dep + 1, a, b);
                if(color == this.color) {
                    if(ths.evaluation > a) {
                        if(ths.evaluation > b) {
                            return new Step(i, j, ths.evaluation);
                        }
                        a = ths.evaluation;
                    }
                    if(ths.evaluation > maxx) {
                        maxx = ths.evaluation;
                        res.x = i;
                        res.y = j;
                        res.evaluation = ths.evaluation;
                    }
                }
                else {
                    if(ths.evaluation < b) {
                        if(ths.evaluation < a) {
                            return new Step(i, j, ths.evaluation);
                        }
                        b = ths.evaluation;
                    }
                    if(ths.evaluation < minx) {
                        minx = ths.evaluation;
                        res.x = i;
                        res.y = j;
                        res.evaluation = ths.evaluation;
                    }
                }
            }
        }
        return res;
    }

    // 统计01矩阵中1的个数
    int count(int[][] board) {
        int cnt = 0;
        for(int i = 1; i <= n; i++) {
            for(int j = 1; j <= n; j++) {
                if(board[i][j] == 1) cnt++;
            }
        }
        return cnt;
    }

    // 矩阵转置
    int[][] trans(int[][] board) {
        int N = board[0].length;
        int[][] newBoard = new int[N][N];
        for(int i = 1; i <= n; i++) {
            for(int j = 1; j <= n; j++) {
                newBoard[i][j] = board[j][i];
            }
        }
        return newBoard;
    }

    int getStable(int[][] board, int color) {
        int res = 0;
        int[] fx = {0, 1, 0, -1};
        int[] fy = {1, 0, -1, 0};
        for(int i = 1; i <= n; i++) {
            for(int j = 1; j <= n; j++) {
                if(board[i][j] != 0) {
                    boolean flag = true;
                    for(int k = 0; k < 4; k++) {
                        int x = i;
                        int y = j;
                        while(x > 0 && x <= 8 && y > 0 && y <= 8) {
                            if(board[x][y] == 0) {
                                flag = false;
                                break;
                            }
                            x += fx[k];
                            y += fy[k];
                        }
                        if(flag) res++;
                    }
                }
            }
        }
        fx = new int[]{1, 1, 8, 8};
        fy = new int[]{1, 8, 1, 8};
        for(int k = 0; k < 4; k++) {
            if(board[fx[k]][fy[k]] == color) res++;
        }
        return res;
    }

    // 估值函数
    int evaluate(int[][] board, int moves, int color) {
        int nextColor = -color;
        int powerRes = 0;
        for(int i = 1; i <= n; i++) {
            for(int j = 1; j <= n; j++) {
                if(board[i][j] == color) {
                    powerRes += valueMap[i-1][j-1];
                }
                else if(board[i][j] == nextColor){
                    powerRes -= valueMap[i-1][j-1];
                }
            }
        }
        int moves_ = moves - count(legalPosition(board, -color));
        int stable = getStable(board, color);
        return powerRes + 15 * moves_ + 10 * stable;
    }

    // 落子
    public int[][] nextBoard(int[][] board, int x, int y, int type) {
        int n = 1;
        int color = type;
        int state[][] = new int[10][10];
        for(int i = 0; i <= 9; i++){
            state[0][i] = 2;
            state[i][0] = 2;
            state[9][i] = 2;
            state[i][9] = 2;
        }
        for(int i = 1; i <= 8; i++){
            for(int j = 1; j <= 8; j++){
                state[i][j] = board[i][j];
            }
        }
        int fx[] = {-1, 1, 0, 0, -1, -1, 1, 1};
        int fy[] = {0, 0, -1, 1, -1, 1, -1, 1};
        while(n-- > 0){
            boolean flag = false;
            for(int i = 0; i <= 7; i++){
                if(state[x+fx[i]][y+fy[i]] != -color) continue;
                int k = 1;
                while(state[x+k*fx[i]][y+k*fy[i]] == -color){
                    k++;
                }
                if(state[x+k*fx[i]][y+k*fy[i]] == color){
                    flag = true;
                    state[x][y] = color;
                    for(int j = 1; j <= k; j++){
                        state[x+j*fx[i]][y+j*fy[i]] = color;
                    }
                }
            }
            if (flag) {
                state[x][y] = color;
            }
        }
        return state;
    }

    // 合法落子位置
    public int[][] legalPosition(int[][] board, int type){
        int [][]a =new int[8+2][8+2];
        int []kong =new int[5+2];
        boolean can=false;
        boolean isthere=false;
        int jimmy=type;
        int alpaca=-jimmy;
        int []x=new int [64];
        int []y=new int [64];
        int st=1;
        int [][]ans=new int[8+2][8+2];
        for(int j=1;j<=8;j++) {
            for (int k = 1; k <= 8; k++) {
                a[j][k] = board[j][k];
                if (a[j][k] == jimmy) {
                    x[st] = j;
                    y[st] = k;
                    st++;
                    isthere = true;
                }
            }
        }
        if(isthere) {
            for(int j=1;j<st;j++) {
                int xx=x[j];
                int yy=y[j];
                while(a[xx][yy]==jimmy)xx--;
                if(xx>=2 && a[xx][yy]==alpaca) {
                    while(a[xx][yy]==alpaca)xx--;
                    if(xx>=1 && a[xx][yy]==0)
                        ans[xx][yy]=1;
                }
                xx=x[j];
                yy=y[j];
                while(a[xx][yy]==jimmy)xx++;
                if(xx<=7 && a[xx][yy]==alpaca) {
                    while(a[xx][yy]==alpaca)xx++;
                    if(xx<=8 && a[xx][yy]==0)
                        ans[xx][yy]=1;
                }
                xx=x[j];
                yy=y[j];
                while(a[xx][yy]==jimmy)yy++;
                if(yy<=7 && a[xx][yy]==alpaca) {
                    while(a[xx][yy]==alpaca)yy++;
                    if(yy<=8 && a[xx][yy]==0)
                        ans[xx][yy]=1;
                }
                xx=x[j];
                yy=y[j];
                while(a[xx][yy]==jimmy)yy--;
                if(yy>=2 && a[xx][yy]==alpaca) {
                    while(a[xx][yy]==alpaca)yy--;
                    if(yy>=1 && a[xx][yy]==0)
                        ans[xx][yy]=1;
                }
                xx=x[j];
                yy=y[j];
                while(a[xx][yy]==jimmy) {
                    xx--;
                    yy--;
                }
                if(yy>=2 && xx>=2 && a[xx][yy]==alpaca) {
                    while(a[xx][yy]==alpaca){
                        yy--;
                        xx--;
                    }
                    if(yy>=1 && xx>=1 && a[xx][yy]==0)
                        ans[xx][yy]=1;
                }
                xx=x[j];
                yy=y[j];
                while(a[xx][yy]==jimmy) {
                    xx++;
                    yy++;
                }
                if(yy<=7 && xx<=7 && a[xx][yy]==alpaca) {
                    while(a[xx][yy]==alpaca) {
                        yy++;
                        xx++;
                    }
                    if(yy<=8 && xx<=8 && a[xx][yy]==0) {
                        ans[xx][yy] = 1;
                    }
                }
                xx=x[j];
                yy=y[j];
                while(a[xx][yy]==jimmy) {
                    xx--;
                    yy++;
                }
                if(yy<=7 && xx>=2 && a[xx][yy]==alpaca) {
                    while(a[xx][yy]==alpaca) {
                        yy++;
                        xx--;
                    }
                    if(yy<=8 && xx>=1 && a[xx][yy]==0) {
                        ans[xx][yy] = 1;
                    }
                }
                xx=x[j];
                yy=y[j];
                while(a[xx][yy]==jimmy) {
                    xx++;
                    yy--;
                }
                if(yy>=2 && xx<=7 && a[xx][yy]==alpaca) {
                    while(a[xx][yy]==alpaca) {
                        yy--;
                        xx++;
                    }
                    if(yy>=1 && xx<=8 && a[xx][yy]==0) {
                        ans[xx][yy] = 1;
                    }
                }
                xx=x[j];
                yy=y[j];
            }
        }
        for(int h=1;h<=8;h++) {
            for (int j = 1; j <= 8; j++) {
                if (ans[h][j] != 0) {
                    can = true;
                    break;
                }
            }
        }
        return ans;
    }
}

class Step {
    int x, y;
    int evaluation;
    Step(int x, int y, int e) {
        this.x = x;
        this.y = y;
        this.evaluation = e;
    }
}
