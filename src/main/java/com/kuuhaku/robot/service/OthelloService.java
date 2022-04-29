package com.kuuhaku.robot.service;

import com.kuuhaku.robot.entity.othello.ChessChannel;
import com.kuuhaku.robot.entity.othello.ChessOperation;
import com.kuuhaku.robot.entity.othello.OthelloBoard;
import com.kuuhaku.robot.entity.othello.OthelloConstant;
import net.mamoe.mirai.contact.Contact;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author by kuuhaku
 * @date 2022/4/16 14:25
 * @description
 */
@Service
public class OthelloService {

    private final Map<Long, Map<Long, OthelloBoard>> prepareMap = new HashMap<>();

    private final Map<String, ChessChannel> chessChannelMap = new HashMap<>();
    private final Map<String, String> playerMap = new HashMap<>();

    private final ReentrantLock lock = new ReentrantLock();

    private final Map<Long, String> usernameMap = new HashMap<>();

    public final static int OK = 0;
    public final static int NOT_FOUND_BOARD = 1;
    public final static int YOU_CREATED_BOARD = 2;
    public final static int YOU_IN_BOARD = 3;
    public final static int OTHERS_CREATED_BOARD = 4;

    public int join(Long groupId, Long userId, String username, Contact group, boolean hasAI) {
        lock.lock();
        usernameMap.put(userId, username);
        Map<Long, OthelloBoard> map = prepareMap.get(groupId);
        if (map == null || map.size() == 0) {
            lock.unlock();
            return NOT_FOUND_BOARD;
        }
        String chessChannelKey = generateChessChannelKey(groupId, userId);
        if (chessChannelMap.containsKey(chessChannelKey)) {
            lock.unlock();
            return YOU_IN_BOARD;
        }
        Long otherUserId = null;
        OthelloBoard othelloBoard = null;
        for (Map.Entry<Long, OthelloBoard> entry : map.entrySet()) {
            otherUserId = entry.getKey();
            othelloBoard = entry.getValue();
        }
        if (otherUserId.equals(userId)) {
            lock.unlock();
            return YOU_CREATED_BOARD;
        }
        int aiNum = hasAI ? 1 : 0;
        ChessChannel chessChannel = othelloBoard.start( otherUserId + "", usernameMap.get(otherUserId),userId + "", username, OthelloConstant.BLACK, aiNum);
        chessChannelMap.put(chessChannelKey, chessChannel);
        chessChannelMap.put(generateChessChannelKey(groupId, otherUserId), chessChannel);
        playerMap.put(chessChannelKey, generateChessChannelKey(groupId, otherUserId));
        playerMap.put(generateChessChannelKey(groupId, otherUserId), chessChannelKey);
        map.clear();
        Long finalOtherUserId = otherUserId;
        new Thread(() -> {
            while (true) {
                try {
                    String msg = chessChannel.take();
                    if (!msg.equals(OthelloConstant.END_MESSAGE)) {
                        group.sendMessage(msg);
                    } else {
                        lock.lock();
                        chessChannelMap.remove(generateChessChannelKey(groupId, userId));
                        chessChannelMap.remove(generateChessChannelKey(groupId, finalOtherUserId));
                        playerMap.remove(chessChannelKey);
                        playerMap.remove(generateChessChannelKey(groupId, finalOtherUserId));
                        lock.unlock();
                        return;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    lock.lock();
                    chessChannelMap.remove(generateChessChannelKey(groupId, userId));
                    chessChannelMap.remove(generateChessChannelKey(groupId, finalOtherUserId));
                    playerMap.remove(chessChannelKey);
                    playerMap.remove(generateChessChannelKey(groupId, finalOtherUserId));
                    lock.unlock();
                    return;
                }
            }
        }).start();
        lock.unlock();
        return OK;
    }

    public int create(Long groupId, Long userId, String username) {
        lock.lock();
        usernameMap.put(userId, username);
        if (chessChannelMap.get(generateChessChannelKey(groupId, userId)) != null) {
            lock.unlock();
            return YOU_IN_BOARD;
        }
        Map<Long, OthelloBoard> map = prepareMap.get(groupId);
        if (map == null) {
            HashMap<Long, OthelloBoard> m = new HashMap<>();
            m.put(userId, OthelloBoard.instant());
            prepareMap.put(groupId, m);
            lock.unlock();
            return OK;
        }
        if (map.size() == 0) {
            map.put(userId, OthelloBoard.instant());
            lock.unlock();
            return OK;
        }
        Long otherUserId = null;
        for (Map.Entry<Long, OthelloBoard> entry : map.entrySet()) {
            otherUserId = entry.getKey();
        }
        lock.unlock();
        if (otherUserId.equals(userId)) {
            return YOU_CREATED_BOARD;
        } else {
            return OTHERS_CREATED_BOARD;
        }
    }

    public int play(Long groupId, Long userId, int x, int y) {
        ChessChannel chessChannel = chessChannelMap.get(generateChessChannelKey(groupId, userId));
        if (chessChannel == null) {
            return NOT_FOUND_BOARD;
        }
        chessChannel.add(new ChessOperation(OthelloConstant.OPERATE_MESSAGE, x - 1, 8 - y, userId + ""));
        return OK;
    }

    public int give(Long groupId, Long userId) {
        lock.lock();
        ChessChannel chessChannel = chessChannelMap.get(generateChessChannelKey(groupId, userId));
        if (chessChannel == null) {
            lock.unlock();
            return NOT_FOUND_BOARD;
        }
        chessChannel.add(new ChessOperation(OthelloConstant.ADMIT_DEFEAT_MESSAGE, 0, 0, userId + ""));
        chessChannelMap.remove(generateChessChannelKey(groupId, userId));
        String otherChannelKey = playerMap.remove(generateChessChannelKey(groupId, userId));
        playerMap.remove(otherChannelKey);
        chessChannelMap.remove(otherChannelKey);
        lock.unlock();
        return OK;
    }

    public int remove(Long groupId) {
        lock.lock();
        Map<Long, OthelloBoard> map = prepareMap.get(groupId);
        if (map == null || map.size() == 0) {
            lock.unlock();
            return NOT_FOUND_BOARD;
        }
        map.clear();
        lock.unlock();
        return OK;
    }

    private String generateChessChannelKey(Long groupId, Long userId) {
        return groupId + ":" + userId;
    }
}
