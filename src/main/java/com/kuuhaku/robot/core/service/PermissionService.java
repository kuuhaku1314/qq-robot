package com.kuuhaku.robot.core.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author by kuuhaku
 * @Date 2021/4/26 19:38
 * @Description
 */
@Service
public class PermissionService {
    /**
     * master权限
     */
    private final Set<Long> masterSet = Collections.newSetFromMap(new ConcurrentHashMap<>());
    /**
     * 管理员权限
     */
    private final Set<Long> adminSet = Collections.newSetFromMap(new ConcurrentHashMap<>());
    /**
     * 无权限
     */
    private final Set<Long> minusSet = Collections.newSetFromMap(new ConcurrentHashMap<>());
    /**
     * 排除哪些群不执行指令
     */
    private final Set<Long> exclusionGroup = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Value("${robot.master}")
    private Long[] masters;

    @PostConstruct
    void init() {
        masterSet.addAll(Arrays.asList(masters));
    }


    public boolean masterContains(long id) {
        return masterSet.contains(id);
    }

    public boolean adminContains(long id) {
        return adminSet.contains(id);
    }

    public boolean minusContains(long id) {
        return minusSet.contains(id);
    }

    public boolean exclusionGroupContains(long id) {
        return exclusionGroup.contains(id);
    }

    public void addMaster(long id) {
        masterSet.add(id);
    }

    public void addAdmin(long id) {
        adminSet.add(id);
    }

    public void addMinus(long id) {
        minusSet.add(id);
    }

    public void addExclusionGroup(long groupId) {
        exclusionGroup.add(groupId);
    }

    public void removeMaster(long id) {
        masterSet.remove(id);
    }

    public void removeAdmin(long id) {
        adminSet.remove(id);
    }

    public void removeMinus(long id) {
        minusSet.remove(id);
    }

    public void removeExclusionGroup(long groupId) {
        exclusionGroup.remove(groupId);
    }
}
