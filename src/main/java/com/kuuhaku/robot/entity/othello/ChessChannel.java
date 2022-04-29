package com.kuuhaku.robot.entity.othello;

import lombok.AllArgsConstructor;

import java.util.concurrent.BlockingQueue;

/**
 * @author by kuuhaku
 * @date 2022/4/16 14:36
 * @description
 */
@AllArgsConstructor
public class ChessChannel {
    private final BlockingQueue<String> produceQueue;
    private final BlockingQueue<ChessOperation> consumerQueue;
    private final Thread thread;
    public String take() throws InterruptedException {
        return produceQueue.take();
    }
    public void add(ChessOperation chessOperation) {
        consumerQueue.add(chessOperation);
    }

    public void close() {
        thread.interrupt();
    }
}
